package org.example.analysisservice;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/analysis")
public class AnalysisController {
    private AnalysisRepository repository;
    private AnalysisService service;

    public AnalysisController(AnalysisRepository repository, AnalysisService service) {
        this.repository = repository;
        this.service = service;
    }

    @GetMapping
    public String show_all (Model model) {
        List<Analysis> analyses = service.get_all_analysis();
        model.addAttribute("analyses", analyses);
        model.addAttribute("analysis", new Analysis());
        return "analysis-list";
    }

    @GetMapping("/{id}")
    public String show_by_id (@PathVariable Long id, Model model,
                              @RequestParam(required = false) Long medcenter_id) {
        Optional<Analysis> analysis = service.get_by_id(id);
        if (analysis.isPresent()) {
            model.addAttribute("analysis", analysis.get());
            model.addAttribute("medcenter_id",medcenter_id);
            return "analysis-detail";
        } else {
            return "redirect:/analysis";
        }
    }

    @GetMapping("/new")
    public String create_form (@RequestParam(required = false) Long donation_id,
                               @RequestParam(required = false) Long donor_id,
                               @RequestParam( required = false) Long medcenter_id,
                               Model model) {
        Analysis analysis = new Analysis();
        if (donation_id != null) {
            analysis.setDonation_id(donation_id);
        }
        if (donor_id != null) {
            analysis.setDonor_id(donor_id);
        }
        model.addAttribute("analysis", analysis);
        model.addAttribute("blood_groups", new String[]{"A", "B", "AB", "O"});
        model.addAttribute("rhesus_factors", new String[]{"+", "-"});
        model.addAttribute("medcenter_id", medcenter_id);
        return "analysis-create-form";
    }

    @PostMapping("/new")
    public String create(@RequestParam(required = false) Long medcenter_id,
                         @ModelAttribute Analysis analysis) {
        Analysis saved_analysis = service.save(analysis);

        if (medcenter_id != null && saved_analysis.getDonation_id() != null) {
            return "redirect:http://localhost:8080/donation-history/" + saved_analysis.getDonation_id() +
                    "/update-analysis?analysis_id=" + saved_analysis.getAnalysis_id() +
                    "&medcenter_id=" + medcenter_id;
        }

        return "redirect:/analysis";
    }

    @GetMapping("/edit/{id}")
    public String edit_form(@PathVariable Long id, Model model,
                            @RequestParam(required = false) Long medcenter_id) {
        Optional<Analysis> analysis = service.get_by_id(id);
        if (analysis.isPresent()) {
            model.addAttribute("analysis", analysis.get());
            model.addAttribute("blood_groups", new String[]{"A", "B", "AB", "O"});
            model.addAttribute("rhesus_factors", new String[]{"+", "-"});
            model.addAttribute("medcenter_id", medcenter_id);
            return "analysis-edit-form";
        } else {
            return "redirect:/analysis";
        }
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @ModelAttribute Analysis analysis,
                       @RequestParam(required = false) Long medcenter_id) {
        Optional<Analysis> exist_analysis = service.get_by_id(id);

        if (exist_analysis.isPresent()) {
            Analysis update_analysis = exist_analysis.get();

            update_analysis.setDonor_id(analysis.getDonor_id());
            update_analysis.setDonation_id(analysis.getDonation_id());
            update_analysis.setHiv(analysis.getHiv());
            update_analysis.setHepatitisB(analysis.getHepatitisB());
            update_analysis.setHepatitisC(analysis.getHepatitisC());
            update_analysis.setSyphilis(analysis.getSyphilis());
            update_analysis.setBrucellosis(analysis.getBrucellosis());
            update_analysis.setAlt_level(analysis.getAlt_level());
            update_analysis.setHemoglobin(analysis.getHemoglobin());
            update_analysis.setBlood_group(analysis.getBlood_group());
            update_analysis.setRhesus_factor(analysis.getRhesus_factor());
            update_analysis.setTechnician_notes(analysis.getTechnician_notes());

            service.save(update_analysis);
        }

        return "redirect:http://localhost:8080/donation-history/medcenter/" + medcenter_id;
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         @RequestParam(required = false) Long medcenter_id) {
        Optional<Analysis> analysis = service.get_by_id(id);

        if (analysis.isPresent()) {
            Long donation_id = analysis.get().getDonation_id();
            service.delete_by_id(id);

            if (donation_id != null && medcenter_id != null) {
                return "redirect:http://localhost:8080/donation-history/" + donation_id +
                        "/remove-analysis?medcenter_id=" + medcenter_id;
            }
        }

        return "redirect:/analysis";
    }

    @GetMapping("/{id}/recommendations")
    public String showRecommendations(@PathVariable Long id, Model model) {
        try {
            Analysis analysis = service.get_by_id(id)
                    .orElseThrow(() -> new RuntimeException("Analysis not found"));

            DonorRecommendation recommendations = service.getRecommendationsForAnalysis(id);

            model.addAttribute("analysis", analysis);
            model.addAttribute("recommendations", recommendations);
            return "analysis-recommendations.html";

        } catch (RuntimeException e) {
            return "redirect:/analysis?error=Analysis+not+found";
        }
    }

    @GetMapping("/donor/{donor_id}")
    public String show_analysis_for_donor (@PathVariable Long donor_id, Model model) {
        List<Analysis> donor_analyses = service.get_analysis_by_donor_id(donor_id);
        model.addAttribute("analyses", donor_analyses);
        model.addAttribute("donorId", donor_id);
        return "analysis-donor-list";
    }
}
