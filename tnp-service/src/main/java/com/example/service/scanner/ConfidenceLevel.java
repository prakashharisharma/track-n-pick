package com.example.service.scanner;

public enum ConfidenceLevel {
    VERY_HIGH("Very High", 5),
    HIGH("High", 4),
    MODERATE("Moderate", 3),
    LOW("Low", 2),
    VERY_LOW("Very Low", 1);

    private final String description;
    private final int level;

    ConfidenceLevel(String description, int level) {
        this.description = description;
        this.level = level;
    }

    public String getDescription() {
        return description;
    }

    public int getLevel() {
        return level;
    }
}
