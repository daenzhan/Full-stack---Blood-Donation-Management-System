package org.example.analysisservice;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AnalysisService {
    private AnalysisRepository analysisRepository;
    private RecommendationService recommendationService;

    public AnalysisService(AnalysisRepository analysisRepository,RecommendationService recommendationService) {
        this.analysisRepository = analysisRepository;
        this.recommendationService = recommendationService;}

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

    public DonorRecommendation getRecommendationsForAnalysis(Long analysisId) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new RuntimeException("Analysis not found with id: " + analysisId));
        return recommendationService.generateRecommendations(analysis);
    }
}
