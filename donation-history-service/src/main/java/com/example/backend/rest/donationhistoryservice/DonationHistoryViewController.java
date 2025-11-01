package com.example.backend.rest.donationhistoryservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/donation-history")
@RequiredArgsConstructor
public class DonationHistoryViewController {

    private final DonationHistoryService donationHistoryService;


    @GetMapping("/medcenter/{medcenterId}")
    public String getDonationHistoryByMedcenterId(@PathVariable Long medcenterId, Model model) {
        List<DonationHistory> donationHistory = donationHistoryService.getDonationHistoryByMedcenterId(medcenterId);

        // Вычисляем всю статистику в контроллере
        long totalDonations = donationHistory.size();

        double totalVolume = donationHistory.stream()
                .filter(d -> d.getVolume() != null)
                .mapToDouble(DonationHistory::getVolume)
                .sum();

        long completedDonations = donationHistory.stream()
                .filter(d -> "COMPLETED".equals(d.getStatus()))
                .count();

        long uniqueDonors = donationHistory.stream()
                .map(DonationHistory::getDonorId)
                .distinct()
                .count();

        model.addAttribute("donationHistory", donationHistory);
        model.addAttribute("medcenterId", medcenterId);
        model.addAttribute("pageTitle", "BloodConnect");
        model.addAttribute("totalDonations", totalDonations);
        model.addAttribute("totalVolume", totalVolume);
        model.addAttribute("completedDonations", completedDonations);
        model.addAttribute("uniqueDonors", uniqueDonors);

        return "medcenter-history";
    }

    @GetMapping("/{donation_id}/update-analysis")
    public String update_analysis_status(@PathVariable Long donation_id,
                                         @RequestParam Long analysis_id,
                                         @RequestParam Long medcenter_id) {
        donationHistoryService.update_analysis_status(donation_id, analysis_id);
        return "redirect:http://localhost:8080/donation-history/medcenter/" + medcenter_id;
    }

    @GetMapping("/{donation_id}/remove-analysis")
    public String remove_analysis(@PathVariable Long donation_id,
                                  @RequestParam Long medcenter_id) {
        donationHistoryService.remove_analysis_status(donation_id);

        return "redirect:http://localhost:8080/donation-history/medcenter/" + medcenter_id;
    }
}