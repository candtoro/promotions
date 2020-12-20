package com.neirodiaz.prospects.service.impl;

import com.neirodiaz.prospects.client.NationalArchivesClient;
import com.neirodiaz.prospects.client.NationalRegistryClient;
import com.neirodiaz.prospects.client.model.PersonalData;
import com.neirodiaz.prospects.client.model.PersonalRecord;
import com.neirodiaz.prospects.exception.CandidateNotFoundException;
import com.neirodiaz.prospects.exception.NationalRegistryNotFoundException;
import com.neirodiaz.prospects.model.Candidate;
import com.neirodiaz.prospects.repository.CandidateRepository;
import com.neirodiaz.prospects.service.CandidateService;
import com.neirodiaz.prospects.service.CandidateVerifier;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * This services take a list of identifications to evaluate if they are able to become a Prospect.<br/><br/>
 * <b>External services are not called in parallel.<b/>
 */
@Log4j2
@Service
@Primary
public class CandidateServiceImpl implements CandidateService {

    @Autowired
    private NationalRegistryClient nationalRegistryClient;
    @Autowired
    private NationalArchivesClient nationalArchivesClient;
    @Autowired
    private CandidateRepository repository;
    @Autowired
    private CandidateVerifier verifier;

    public CandidateServiceImpl() {
    }

    public CandidateServiceImpl(CandidateRepository repository) {
        this.repository = repository;
    }

    public CandidateServiceImpl(NationalRegistryClient nationalRegistryClient,
                                NationalArchivesClient nationalArchivesClient,
                                CandidateRepository repository,
                                CandidateVerifier verifier) {
        this.nationalRegistryClient = nationalRegistryClient;
        this.nationalArchivesClient = nationalArchivesClient;
        this.repository = repository;
        this.verifier = verifier;
    }

    @Override
    public List<Candidate> runPipeline(List<Long> identifications) {

        List<Candidate> candidates = repository.findByIdentifications(identifications);
        List<Candidate> candidatesValidated = new ArrayList<>();

        if (candidates.isEmpty()) {
            throw new CandidateNotFoundException("Candidates were not found in the data base!");
        }

        // Add a candidate in the list if the personal information is the same as PersonaData.
        BiConsumer<Candidate, PersonalData> candidateConsumer = (c, p) -> {
            try {
                Response<PersonalRecord> execute = nationalArchivesClient.getRecords(c.getIdentification()).execute();
                if (execute.isSuccessful()) {
                    PersonalRecord body = execute.body();
                    if (body != null && !body.getHasRecords() && verifier.verify(c, p)) {
                        candidatesValidated.add(c);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };


        for (Long id : identifications) {
            try {
                Response<PersonalData> response = nationalRegistryClient.getRecords(id).execute();
                if (response.isSuccessful()) {
                    PersonalData personalData = response.body();
                    if (personalData != null) {
                        candidates.stream()
                                .filter(candidate -> candidate.getIdentification().equals(id))
                                .findFirst()
                                .ifPresent(c -> candidateConsumer.accept(c, personalData));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
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
