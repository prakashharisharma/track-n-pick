package com.example.service.scanner;

public enum TradingSignal {
    STRONG_BREAKOUT("Strong Breakout", 5),
    BREAKOUT("Breakout", 4),
    STRONG_SUPPORT("Strong Support", 5),
    SUPPORT_HOLD("Support Hold", 4),
    NEUTRAL_BULLISH("Neutral Bullish", 3),
    NEUTRAL("Neutral", 2),
    NEUTRAL_BEARISH("Neutral Bearish", 1);

    private final String description;
    private final int strength;

    TradingSignal(String description, int strength) {
        this.description = description;
        this.strength = strength;
    }

    public String getDescription() {
        return description;
    }

    public int getStrength() {
        return strength;
    }
}
