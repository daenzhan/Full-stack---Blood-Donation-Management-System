package org.example.analysisservice;

import org.springframework.stereotype.Controller;

@Controller
public class AnalysisController {
    private AnalysisRepository repository;
    private AnalysisService service;

    public AnalysisController(AnalysisRepository repository, AnalysisService service) {
        this.repository = repository;
        this.service = service;
    }
}
