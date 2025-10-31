package org.example.analysisservice;

public enum EligibilityStatus {
    ELIGIBLE("Годен к донорству"),
    TEMPORARILY_DEFERRED("Временно отстранен"),
    PERMANENTLY_DEFERRED("Постоянно отстранен");

    private final String description;

    EligibilityStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}