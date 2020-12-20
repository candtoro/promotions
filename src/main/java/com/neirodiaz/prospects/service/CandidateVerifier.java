package com.neirodiaz.prospects.service;

import com.neirodiaz.prospects.client.model.PersonalData;
import com.neirodiaz.prospects.model.Candidate;

public interface CandidateVerifier {

    /**
     * Verify if the Candidate contains the same personal information as Data
     *
     * @param candidate candidate to be verified
     * @param data      data with the information expected
     * @return True is the personal information is the same otherwise false
     */
    boolean verify(Candidate candidate, PersonalData data);

    /**
     * Set the candidate as prospect if the qualification system gives a satisfactory score.
     * (This method was created thinking about having the qualification system inside this application,
     * not as External System).
     *
     * @param candidate candidate to be verified
     * @return True if the candidate achieves the minimum passing score.
     */
    boolean verifyAndApplyPromotion(final Candidate candidate);
}
