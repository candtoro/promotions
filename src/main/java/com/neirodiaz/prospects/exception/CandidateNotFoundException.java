package com.neirodiaz.prospects.exception;

public class CandidateNotFoundException extends RuntimeException {

    public CandidateNotFoundException() {
    }

    public CandidateNotFoundException(String message) {
        super(message);
    }
}
