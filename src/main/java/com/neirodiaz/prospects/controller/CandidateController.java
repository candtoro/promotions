package com.neirodiaz.prospects.controller;

import com.neirodiaz.prospects.model.Candidate;
import com.neirodiaz.prospects.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class CandidateController {

    @Autowired
    private CandidateService taskService;

    @GetMapping(value = "/parallel/{ids}")
    @ResponseStatus(value = HttpStatus.OK)
    public List<Candidate> getProspectsWithTask(@PathVariable List<Long> ids) {
        List<Candidate> result = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            result = taskService.runPipeline(ids);
        }
        return result;
    }
}
