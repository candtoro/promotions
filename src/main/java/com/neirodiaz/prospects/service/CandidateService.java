package com.neirodiaz.prospects.service;

import com.neirodiaz.prospects.model.Candidate;

import java.util.List;

public interface CandidateService {
    /**
     * Run the candidates pipeline process.
     *
     * @param identifications id list.
     * @return List of candidates as prospect or empty is candidates were rejected.
     */
    List<Candidate> runPipeline(final List<Long> identifications);
}
