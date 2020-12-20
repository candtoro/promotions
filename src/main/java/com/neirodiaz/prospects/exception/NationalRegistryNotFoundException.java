package com.neirodiaz.prospects.exception;

public class NationalRegistryNotFoundException extends RuntimeException {

    public NationalRegistryNotFoundException() {
    }

    public NationalRegistryNotFoundException(String message) {
        super(message);
    }
}
