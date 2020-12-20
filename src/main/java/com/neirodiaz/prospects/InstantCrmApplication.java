package com.neirodiaz.prospects;

import com.neirodiaz.prospects.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InstantCrmApplication {

    @Autowired
    private CandidateService service;

    public static void main(String[] args) {
        SpringApplication.run(InstantCrmApplication.class, args);
    }
}
