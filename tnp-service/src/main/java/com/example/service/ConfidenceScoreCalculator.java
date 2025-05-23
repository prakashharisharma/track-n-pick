package com.example.service;

import static com.example.data.common.type.MarketCapCategory.*;

import com.example.data.common.type.MarketCapCategory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfidenceScoreCalculator {

    /**
     * Calculates the confidence score (0 to 10) based on strategy score, raw risk, market cap,
     * research price, volume, and MACD.
     *
     * @param strategyScore value between 0 and 10 (higher is better)
     * @param rawRisk value between 2 and 20 (higher is worse)
     * @param marketCapInCrores market capitalization in crores
     * @param researchPrice current research price of the stock
     * @param volumeScore volume score (0 to 10)
     * @param macdScore MACD score (0 to 10)
     * @return confidence score between 0 and 10
     */
    public static double calculateConfidenceScore(
            double strategyScore,
            double rawRisk,
            double marketCapInCrores,
            double researchPrice,
            double volumeScore,
            double macdScore) {

        double riskWeight = 0.50;
        double strategyWeight = 0.10;
        double macdWeight = 0.15;
        double volumeWeight = 0.10;
        double mcapWeight = 0.10;
        double priceWeight = 0.05;

        if (strategyScore >= 9) {
            macdWeight = 0.10;
            volumeWeight = 0.15;
        }

        // Clamp scores to [0–10]
        strategyScore = clamp(strategyScore);
        volumeScore = clamp(volumeScore);
        macdScore = clamp(macdScore);

        double riskScore =
                calculateRiskScore(Math.abs(rawRisk)); // Convert raw risk (2–20) to score (0–10)
        double marketCapScore = getMarketCapScore(marketCapInCrores); // Score based on market cap
        double researchPriceScore =
                getResearchPriceScore(researchPrice); // Score based on price deviation

        // Logging each component
        log.info(
                "Strategy Score: {} (Weight: {}) => {}",
                strategyScore,
                strategyWeight,
                strategyScore * strategyWeight);
        log.info(
                "Risk Score: {} (Weight: {}) => {}", riskScore, riskWeight, riskScore * riskWeight);
        log.info(
                "Market Cap Score: {} (Weight: {}) => {}",
                marketCapScore,
                mcapWeight,
                marketCapScore * mcapWeight);
        log.info(
                "Research Price Score: {} (Weight: {}) => {}",
                researchPriceScore,
                priceWeight,
                researchPriceScore * priceWeight);
        log.info(
                "Volume Score: {} (Weight: {}) => {}",
                volumeScore,
                volumeWeight,
                volumeScore * volumeWeight);
        log.info(
                "MACD Score: {} (Weight: {}) => {}", macdScore, macdWeight, macdScore * macdWeight);

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
        // System.out.println("Risk : "+ rawRisk);
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
            return 9;
        }

        // Rule 3: 6 - avg increasing && vol increasing
        if (avgIncreasing && volumeIncreasing) {
            return 8;
        }

        // Rule 4: 4 - avg increasing
        if (avgIncreasing) {
            return 7;
        }

        // Rule 5: else 2
        return 5;
    }

    public static double calculateMacdScore(
            double macd,
            double signal,
            double previousHistogram,
            double previousMacd,
            double previousSignal) {
        double histogram = macd - signal;

        boolean macdIncreasing = macd > previousMacd;
        boolean signalDecreasing = signal < previousSignal;
        boolean histogramRising = histogram > previousHistogram;

        // Case: Strong bullish crossover (MACD crossed above signal, rising histogram, MACD > 0)
        if (macd > signal && histogramRising && macd > 0) {
            return 10;
        }

        // Case: Moderately strong bullish (histogram rising and MACD increasing or signal
        // decreasing)
        if (macd > signal && histogramRising && (macdIncreasing || signalDecreasing)) {
            return 9;
        }

        // Case: Mild bullish (MACD > signal and histogram positive)
        if (macd > signal && histogram > 0) {
            return 8;
        }

        // Case: Neutral (MACD near signal)
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
