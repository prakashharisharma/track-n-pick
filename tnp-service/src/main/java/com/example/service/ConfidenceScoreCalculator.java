package com.example.service;

import static com.example.data.common.type.MarketCapCategory.*;

import com.example.data.common.type.MarketCapCategory;

public class ConfidenceScoreCalculator {

    /**
     * Calculates the confidence score out of 10 based on strategy score, raw risk (2–20), research
     * price, and market cap.
     *
     * @param strategyScore value between 0 and 10 (higher is better)
     * @param rawRisk value between 2 and 20 (higher is worse)
     * @param marketCapInCrores market cap in crores
     * @param researchPrice current research price of the stock
     * @return confidence score between 0 and 10
     */
    public static double calculateConfidenceScore(
            double strategyScore,
            double rawRisk,
            double marketCapInCrores,
            double researchPrice,
            double volumeScore,
            double macdScore) {

        double riskWeight = 0.40;
        double volumeWeight = 0.22;
        double strategyWeight = 0.18;
        double macdWeight = 0.10;
        double mcapWeight = 0.05;
        double priceWeight = 0.05;

        // Clamp scores to [0–10]
        strategyScore = clamp(strategyScore);
        volumeScore = clamp(volumeScore);
        macdScore = clamp(macdScore);

        // Convert and calculate sub-scores
        double riskScore = calculateRiskScore(rawRisk);
        double marketCapScore = getMarketCapScore(marketCapInCrores);
        double researchPriceScore = getResearchPriceScore(researchPrice);

        return (strategyScore * strategyWeight)
                + (riskScore * riskWeight)
                + (marketCapScore * mcapWeight)
                + (researchPriceScore * priceWeight)
                + (volumeScore * volumeWeight)
                + (macdScore * macdWeight);
    }

    /**
     * Converts raw risk value in range [2, 20] to a score between 0 (high risk) to 10 (low risk).
     */
    public static double calculateRiskScore(double rawRisk) {
        // Clamp rawRisk to the range [2, 20]
        rawRisk = Math.max(2, Math.min(20, rawRisk));

        // Map 2 → 10, 20 → 0
        return (1.0 - ((rawRisk - 2) / 18.0)) * 10.0;
    }

    /** Clamps a value to the 0–10 range. */
    private static double clamp(double value) {
        return Math.max(0, Math.min(10, value));
    }

    /** Returns a score based on market cap category. */
    private static double getMarketCapScore(double marketCapInCrores) {
        MarketCapCategory category = MarketCapCategory.classify(marketCapInCrores);
        switch (category) {
            case SMALLCAP:
                return 10;
            case MIDCAP:
                return 9;
            case LARGECAP:
                return 7;
            case MEGACAP:
                return 5;
            default:
                return 1; // fallback
        }
    }

    /** Returns a score based on research price. */
    private static double getResearchPriceScore(double researchPrice) {
        if (researchPrice < 500) return 10;
        if (researchPrice < 750) return 9.5;
        if (researchPrice < 1000) return 9;
        if (researchPrice < 1500) return 8.5;
        if (researchPrice < 2000) return 8;
        if (researchPrice < 2500) return 7.5;
        if (researchPrice < 3000) return 7;
        if (researchPrice < 3500) return 6.5;
        if (researchPrice < 4000) return 6;
        if (researchPrice < 5000) return 5.5;
        if (researchPrice < 7500) return 5;
        if (researchPrice < 10000) return 4;
        return 1;
    }

    public static double calculateVolumeScore(
            long currentVolume, long previousVolume, long averageVolume, long prevAverageVolume) {
        if (currentVolume <= 0
                || previousVolume <= 0
                || averageVolume <= 0
                || prevAverageVolume <= 0) {
            return 2;
        }

        boolean volumeIncreasing = currentVolume > previousVolume;
        boolean avgIncreasing = averageVolume > prevAverageVolume;
        boolean volAboveAvg = currentVolume > averageVolume;
        boolean prevVolAbovePrevAvg = previousVolume > prevAverageVolume;
        boolean vol2xPrevVol = currentVolume > 2 * previousVolume;

        // Rule 1: 10 - avg increasing && vol increasing && vol > avg && prevVol > prevAvg
        if (avgIncreasing && volumeIncreasing && volAboveAvg && prevVolAbovePrevAvg) {
            return 10;
        }

        // Rule 2: 8 - avg increasing && vol > 2X prevVol && vol > avg
        if (avgIncreasing && vol2xPrevVol && volAboveAvg) {
            return 8;
        }

        // Rule 3: 6 - avg increasing && vol increasing
        if (avgIncreasing && volumeIncreasing) {
            return 6;
        }

        // Rule 4: 4 - avg increasing
        if (avgIncreasing) {
            return 4;
        }

        // Rule 5: else 2
        return 2;
    }

    public static double calculateMacdScore(double macd, double signal, double previousHistogram) {
        double histogram = macd - signal;

        // Case: Strong bullish crossover (MACD crossed above signal, rising histogram)
        if (macd > signal && histogram > previousHistogram && macd > 0) {
            return 10;
        }

        // Case: Mild bullish (MACD > signal and MACD rising but not strong)
        if (macd > signal && histogram > 0) {
            return 8;
        }

        // Case: MACD near signal (neutral)
        if (Math.abs(macd - signal) < 0.1) {
            return 5;
        }

        // Case: Bearish crossover (MACD < signal, falling histogram)
        if (macd < signal && histogram < previousHistogram) {
            return 2;
        }

        // Case: Strongly negative MACD
        if (macd < 0 && macd < signal) {
            return 1;
        }

        // Default mild bearish
        return 3;
    }
}
