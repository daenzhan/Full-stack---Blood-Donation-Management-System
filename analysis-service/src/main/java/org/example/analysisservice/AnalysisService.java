package org.example.analysisservice;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AnalysisService {
    private AnalysisRepository analysisRepository;

    public AnalysisService(AnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

    public List<Analysis> get_all_analysis(){
        return analysisRepository.findAll();
    }

    public Optional<Analysis> get_by_id (Long analysis_id){
        return analysisRepository.findById(analysis_id);
    }

    public void delete_by_id (Long analysis_id){
        analysisRepository.deleteById(analysis_id);
    }

    public Analysis save (Analysis a){
        return analysisRepository.save(a);
    }

}
