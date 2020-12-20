package com.neirodiaz.prospects.service.impl;

import com.neirodiaz.prospects.client.model.PersonalData;
import com.neirodiaz.prospects.model.Candidate;
import com.neirodiaz.prospects.service.CandidateVerifier;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Random;

@Component
@Getter
public class CandidateVerifierImpl implements CandidateVerifier {


    private final int minimumScore; // Minimum score (Exclusive)
    private final int maximumScore; // Maximum score (Inclusive)
    private final Random random = new Random();

    public CandidateVerifierImpl() {
        this.minimumScore = 60;
        this.maximumScore = 100;
    }

    public CandidateVerifierImpl(final int minimumScore, final int maximumScore) {
        this.minimumScore = minimumScore;
        this.maximumScore = maximumScore;
    }

    @Override
    public boolean verify(Candidate candidate, PersonalData personalData) {
        Objects.requireNonNull(candidate, "Candidate is required");
        Objects.requireNonNull(candidate, "Personal Data is required");
        return personalData.getIdentification().equals(candidate.getIdentification())
                && personalData.getFirstName().equalsIgnoreCase(candidate.getFirstName())
                && personalData.getLastName().equalsIgnoreCase(candidate.getLastName());
    }

    @Override
    public boolean verifyAndApplyPromotion(final Candidate candidate) {
        int randomScore = calculateScore(candidate);
        candidate.setScore(randomScore);
        candidate.setEvaluated(true);
        candidate.setIsProspect(false);
        boolean scoreApproved = candidate.getScore() > getMinimumScore();
        candidate.setIsProspect(scoreApproved);
        return scoreApproved;
    }

    /**
     * This method return a random score for the candidate.
     * Possible rule: If the candidate doesn't have identification this method will return -1.
     *
     * @param candidate Candidate to evaluate
     * @return Return the score for the candidate.
     */
    public int calculateScore(final Candidate candidate) {
        int score = -1;
        if (candidate != null && candidate.getIdentification() != null) {
            random.nextInt(getMaximumScore() + 1);
        }
        return score;
    }
}
