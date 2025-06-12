package com.example.dto.type;

public enum SentimentColor {
    POSITIVE,
    NEUTRAL,
    NEGATIVE;

    public static SentimentColor fromString(String value) {
        if (value == null) return null;
        return switch (value.toLowerCase()) {
            case "positive" -> POSITIVE;
            case "neutral" -> NEUTRAL;
            case "negative" -> NEGATIVE;
            default -> throw new IllegalArgumentException("Unknown color: " + value);
        };
    }

    public int getWeight() {
        return switch (this) {
            case NEGATIVE -> 1;
            case NEUTRAL -> 2;
            case POSITIVE -> 3;
        };
    }
}

