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
    public String show_by_id (@PathVariable Long id, Model model) {
        Optional<Analysis> analysis = service.get_by_id(id);
        if (analysis.isPresent()) {
            model.addAttribute("analysis", analysis.get());
            return "analysis-detail";
        } else {
            return "redirect:/analysis";
        }
    }

    @GetMapping("/new")
    public String create_form (Model model) {
        model.addAttribute("analysis", new Analysis());
        model.addAttribute("blood_groups", new String[]{"A", "B", "AB", "O"});
        model.addAttribute("rhesus_factors", new String[]{"+", "-"});
        return "analysis-create-form";
    }

    @PostMapping
    public String create (@ModelAttribute Analysis analysis) {
        service.save(analysis);
        return "redirect:/analysis";
    }

    @GetMapping("/edit/{id}")
    public String edit_form(@PathVariable Long id, Model model) {
        Optional<Analysis> analysis = service.get_by_id(id);
        if (analysis.isPresent()) {
            model.addAttribute("analysis", analysis.get());
            model.addAttribute("blood_groups", new String[]{"A", "B", "AB", "O"});
            model.addAttribute("rhesusFactors", new String[]{"+", "-"});
            return "analysis-edit-form";
        } else {
            return "redirect:/analysis";
        }
    }

    @PostMapping("/edit/{id}")
    public String edit (@PathVariable Long id, @ModelAttribute Analysis analysis) {
        analysis.setAnalysis_id(id);
        service.save(analysis);
        return "redirect:/analysis";
    }

    @GetMapping("/delete/{id}")
    public String delete (@PathVariable Long id) {
        service.delete_by_id(id);
        return "redirect:/analysis";
    }
}
