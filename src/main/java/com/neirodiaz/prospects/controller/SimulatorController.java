package com.neirodiaz.prospects.controller;

import com.neirodiaz.prospects.client.model.PersonalData;
import com.neirodiaz.prospects.client.model.PersonalRecord;
import com.neirodiaz.prospects.model.Candidate;
import com.neirodiaz.prospects.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>Simulation purposes (not for testing purpose)</h1>
 * <li>Simulate external services when the application is running and it's not being testing.</li>
 * <li>Best practices and Structure were excluded.</li>
 */
@RestController
public class SimulatorController {

    @Autowired
    private CandidateRepository repository;

    /**
     * National Registry Identification endpoint simulator
     *
     * @param id Identification number.
     * @return PersonalRecord entity with information if the id was found.
     */
    @GetMapping("/v1/archive/{id}")
    public PersonalRecord getRecord(@PathVariable Long id) {

        PersonalRecord record = PersonalRecord.builder().build();
        if (id != null) {
            record.setId(id);
            record.setHasRecords(false);
        }
        return record;
    }

    /**
     * National Judicial Records endpoint simulator.
     *
     * @param id Identification number.
     * @return PersonalData entity with information if the id was found.
     */
    @GetMapping("/v1/data/{id}")
    public PersonalData getData(@PathVariable Long id) {

        PersonalData record = PersonalData.builder().build();
        if (id != null) {
            Candidate candidate = repository.findByIdentification(id);
            record.setIdentification(candidate.getIdentification());
            record.setFirstName(candidate.getFirstName());
            record.setLastName(candidate.getLastName());
        }
        return record;
    }
}
