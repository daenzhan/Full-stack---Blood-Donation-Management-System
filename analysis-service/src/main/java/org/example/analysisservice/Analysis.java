package org.example.analysisservice;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis")
public class Analysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long analysis_id;

    private Long donation_id;
    private Long donor_id;

    private String hiv; // ВИЧ
    private String brucellosis;
    private String  hepatitisB;
    private String  hepatitisC;
    private String syphilis;
    private Double alt_level;
    private String blood_group;
    private String rhesus_factor;
    private Double hemoglobin;
    private LocalDateTime analysis_date;
    private String technician_notes;

    public Analysis(Long analysis_id, Long donation_id, Long donor_id, String hiv, String brucellosis, String hepatitisB, String hepatitisC, String syphilis, Double alt_level, String blood_group, String rhesus_factor, Double hemoglobin, LocalDateTime analysis_date, String technician_notes) {
        this.analysis_id = analysis_id;
        this.donation_id = donation_id;
        this.donor_id = donor_id;
        this.hiv = hiv;
        this.brucellosis = brucellosis;
        this.hepatitisB = hepatitisB;
        this.hepatitisC = hepatitisC;
        this.syphilis = syphilis;
        this.alt_level = alt_level;
        this.blood_group = blood_group;
        this.rhesus_factor = rhesus_factor;
        this.hemoglobin = hemoglobin;
        this.analysis_date = analysis_date;
        this.technician_notes = technician_notes;
    }

    public Analysis(){}

    public Long getAnalysis_id() {
        return analysis_id;
    }

    public void setAnalysis_id(Long analysis_id) {
        this.analysis_id = analysis_id;
    }

    public Long getDonation_id() {
        return donation_id;
    }

    public void setDonation_id(Long donation_id) {
        this.donation_id = donation_id;
    }

    public Long getDonor_id() {
        return donor_id;
    }

    public void setDonor_id(Long donor_id) {
        this.donor_id = donor_id;
    }

    public String getHiv() {
        return hiv;
    }

    public void setHiv(String hiv) {
        this.hiv = hiv;
    }

    public String getBrucellosis() {
        return brucellosis;
    }

    public void setBrucellosis(String brucellosis) {
        this.brucellosis = brucellosis;
    }

    public String getHepatitisB() {
        return hepatitisB;
    }

    public void setHepatitisB(String hepatitisB) {
        this.hepatitisB = hepatitisB;
    }

    public String getHepatitisC() {
        return hepatitisC;
    }

    public void setHepatitisC(String hepatitisC) {
        this.hepatitisC = hepatitisC;
    }

    public String getSyphilis() {
        return syphilis;
    }

    public void setSyphilis(String syphilis) {
        this.syphilis = syphilis;
    }

    public Double getAlt_level() {
        return alt_level;
    }

    public void setAlt_level(Double alt_level) {
        this.alt_level = alt_level;
    }

    public String getBlood_group() {
        return blood_group;
    }

    public void setBlood_group(String blood_group) {
        this.blood_group = blood_group;
    }

    public String getRhesus_factor() {
        return rhesus_factor;
    }

    public void setRhesus_factor(String rhesus_factor) {
        this.rhesus_factor = rhesus_factor;
    }

    public Double getHemoglobin() {
        return hemoglobin;
    }

    public void setHemoglobin(Double hemoglobin) {
        this.hemoglobin = hemoglobin;
    }

    public LocalDateTime getAnalysis_date() {
        return analysis_date;
    }

    public void setAnalysis_date(LocalDateTime analysis_date) {
        this.analysis_date = analysis_date;
    }

    public String getTechnician_notes() {
        return technician_notes;
    }

    public void setTechnician_notes(String technician_notes) {
        this.technician_notes = technician_notes;
    }
}
