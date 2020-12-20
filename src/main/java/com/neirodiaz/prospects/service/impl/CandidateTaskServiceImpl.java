package com.neirodiaz.prospects.service.impl;

import com.neirodiaz.prospects.client.NationalArchivesClient;
import com.neirodiaz.prospects.client.NationalRegistryClient;
import com.neirodiaz.prospects.client.model.PersonalData;
import com.neirodiaz.prospects.exception.CandidateNotFoundException;
import com.neirodiaz.prospects.exception.NationalRegistryNotFoundException;
import com.neirodiaz.prospects.model.Candidate;
import com.neirodiaz.prospects.repository.CandidateRepository;
import com.neirodiaz.prospects.service.CandidateService;
import com.neirodiaz.prospects.service.CandidateVerifier;
import com.neirodiaz.prospects.task.NationalArchivesTask;
import com.neirodiaz.prospects.task.NationalRegistryTask;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This services take a list of identifications to evaluate if they are able to become a Prospect.<br/><br/>
 * <b>External services are called using Threads.<b/>
 */
@Log4j2
@Service
public class CandidateTaskServiceImpl implements CandidateService {

    @Autowired
    private NationalRegistryClient nationalRegistryClient;
    @Autowired
    private NationalArchivesClient nationalArchivesClient;
    @Autowired
    private CandidateRepository repository;
    @Autowired
    private CandidateVerifier verifier;

    public CandidateTaskServiceImpl() {
    }

    public CandidateTaskServiceImpl(CandidateRepository repository) {
        this.repository = repository;
    }

    public CandidateTaskServiceImpl(NationalRegistryClient nationalRegistryClient,
            NationalArchivesClient nationalArchivesClient,
            CandidateRepository repository,
            CandidateVerifier verifier) {
        this.nationalRegistryClient = nationalRegistryClient;
        this.nationalArchivesClient = nationalArchivesClient;
        this.repository = repository;
        this.verifier = verifier;
    }

    @Override
    public List<Candidate> runPipeline(final List<Long> identifications) {

        List<Candidate> candidates = repository.findByIdentifications(identifications);
        List<Candidate> candidatesValidated = new ArrayList<>();
        Map<Long, Boolean> archivesMap = new HashMap<>();
        Map<Long, PersonalData> dataMap = new HashMap<>();

        if (candidates.isEmpty()) {
            throw new CandidateNotFoundException("Candidates were not found in the data base!");
        }

        Thread archiveThread = new Thread(
                new NationalArchivesTask(nationalArchivesClient, identifications, archivesMap)
        );

        Thread registryThread = new Thread(
                new NationalRegistryTask(nationalRegistryClient, identifications, dataMap));

        registryThread.start();
        archiveThread.start();

        try {
            archiveThread.join();
            registryThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            log.error("Threads were interrupted!");
        }

        List<PersonalData> personalDataList = dataMap.entrySet()
                .stream()
                .filter(kv -> archivesMap.get(kv.getKey()) != null && !archivesMap.get(kv.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        for (PersonalData data : personalDataList) {
            Optional<Candidate> candidate = candidates.stream()
                    .filter(c -> c.getIdentification().equals(data.getIdentification()))
                    .findFirst();
            if (candidate.isPresent() && verifier.verify(candidate.get(), data)) {
                candidatesValidated.add(candidate.get());
            }
        }

        if (candidatesValidated.isEmpty()) {
            throw new NationalRegistryNotFoundException("Candidates didn't pass the validations!");
        }

        log.info("Candidate verification...");

        candidatesValidated.stream()
                .filter(verifier::verifyAndApplyPromotion)
                .forEach(log::info);

        return candidatesValidated.stream()
                .filter(Candidate::getIsProspect)
                .collect(Collectors.toList());
    }
}
