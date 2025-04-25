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
            double strategyScore, double rawRisk, double marketCapInCrores, double researchPrice) {

        double strategyWeight = 0.40;
        double riskWeight = 0.30;
        double mcapWeight = 0.20;
        double priceWeight = 0.10;

        // Clamp strategy score
        strategyScore = clamp(strategyScore);

        // Convert raw risk (2–20) into score (0–10), higher risk → lower score
        double riskScore = calculateRiskScore(rawRisk);

        double marketCapScore = getMarketCapScore(marketCapInCrores);
        double researchPriceScore = getResearchPriceScore(researchPrice);

        return (strategyScore * strategyWeight)
                + (riskScore * riskWeight)
                + (marketCapScore * mcapWeight)
                + (researchPriceScore * priceWeight);
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
}
