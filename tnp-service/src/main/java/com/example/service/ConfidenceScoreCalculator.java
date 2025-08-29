package com.example.service;

import static com.example.data.common.type.MarketCapCategory.*;

import com.example.data.common.type.MarketCapCategory;
import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockTechnicals;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfidenceScoreCalculator {

    /**
     * Calculates the confidence score (0 to 10) based on strategy score, raw risk, market cap,
     * research price, volume, and MACD.
     *
     * @param subStrategyScore value between 0 and 10 (higher is better)
     * @param rawRisk value between 2 and 20 (higher is worse)
     * @param marketCapInCrores market capitalization in crores
     * @param researchPrice current research price of the stock
     * @param volumeScore volume score (0 to 10)
     * @param macdScore MACD score (0 to 10)
     * @return confidence score between 0 and 10
     */
    public static double calculateConfidenceScore(
            double strategyScore,
            double subStrategyScore,
            double rawRisk,
            double marketCapInCrores,
            double researchPrice,
            double volumeScore,
            double macdScore,
            double valuationScore) {

        double riskWeight = 0.25;
        double strategyWeight = 0.25;
        double subStrategyWeight = 0.20;
        double valuationWeight = 0.10;

        double macdWeight = 0.05;
        double volumeWeight = 0.05;
        double mcapWeight = 0.05;
        double priceWeight = 0.05;

        if (macdScore == 10.0 && volumeScore == 10.0) {
            riskWeight = 0.20;
            strategyWeight = 0.30;
            if (subStrategyScore >= 8.0) {
                riskWeight = 0.15;
                subStrategyWeight = 0.25;
            }

        } else if (volumeScore == 10.0) {
            riskWeight = 0.175;
            valuationWeight = 0.175;
            macdWeight = 0.0;
            volumeWeight = 0.15;
            mcapWeight = 0.10;
            priceWeight = 0.0;
        }

        /*
        double riskWeight = 0.40;
        double strategyWeight = 0.15;
        double subStrategyWeight = 0.10;
        double macdWeight = 0.10;
        double volumeWeight = 0.0;
        double mcapWeight = 0.10;
        double priceWeight = 0.05;
        double valuationWeight = 0.10;
         */

        /*
        if (subStrategyScore >= 9) {
            macdWeight = 0.05;
            volumeWeight = 0.20;
        }*/

        // Clamp scores to [0–10]
        strategyScore = clamp(strategyScore);
        subStrategyScore = clamp(subStrategyScore);
        volumeScore = clamp(volumeScore);
        macdScore = clamp(macdScore);

        double riskScore =
                calculateRiskScore(Math.abs(rawRisk)); // Convert raw risk (2–20) to score (0–10)
        double marketCapScore = getMarketCapScore(marketCapInCrores); // Score based on market cap
        double researchPriceScore =
                getResearchPriceScore(researchPrice); // Score based on price deviation
        double valuationScoreClamped = clamp(valuationScore / 10.0); // Normalize 0–100 to 0–10

        // Logging each component

        log.info(
                "Strategy Score: {} (Weight: {}) => {}",
                strategyScore,
                strategyWeight,
                strategyScore * strategyWeight);
        log.info(
                "Sub Strategy Score: {} (Weight: {}) => {}",
                subStrategyScore,
                subStrategyWeight,
                subStrategyScore * subStrategyWeight);
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
        log.info(
                "Valuation Score: {} (Weight: {}) => {}",
                valuationScoreClamped,
                valuationWeight,
                valuationScoreClamped * valuationWeight);

        double score =
                (strategyScore * strategyWeight)
                        + (subStrategyScore * subStrategyWeight)
                        + (riskScore * riskWeight)
                        + (marketCapScore * mcapWeight)
                        + (researchPriceScore * priceWeight)
                        + (volumeScore * volumeWeight)
                        + (macdScore * macdWeight)
                        + (valuationScoreClamped * valuationWeight);

        /*
        System.out.println(score);
        System.out.println(roundToTwoDecimals(score));
        System.out.println(ceilBeyondOneDecimal(score));
        score = roundToTwoDecimals(score);
        System.out.println(ceilBeyondOneDecimal(score));
        */

        return ceilBeyondOneDecimal(roundToTwoDecimals(score));
        // return score;
    }

    public static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public static double ceilBeyondOneDecimal(double value) {
        double rounded = Math.floor(value * 10) / 10.0;
        return value > rounded ? rounded + 0.1 : rounded;
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

    public static double calculateVolumeScore(StockTechnicals stockTechnicals) {

        long currentVolume = stockTechnicals.getVolume();
        long prevVolume = stockTechnicals.getPrevVolume();
        long prevPrevVolume = stockTechnicals.getPrev2Volume();
        long avgVolume = stockTechnicals.getVolumeAvg20();
        long prevAvgVolume = stockTechnicals.getPrevVolumeAvg20();
        long prevPrevAvgVolume = stockTechnicals.getPrev2VolumeAvg20();

        if (stockTechnicals.getTimeframe() == Timeframe.WEEKLY) {
            avgVolume = stockTechnicals.getVolumeAvg10();
            prevAvgVolume = stockTechnicals.getPrevVolumeAvg10();
            prevPrevAvgVolume = stockTechnicals.getPrev2VolumeAvg10();
        }

        if (stockTechnicals.getTimeframe() == Timeframe.MONTHLY) {
            avgVolume = stockTechnicals.getVolumeAvg5();
            prevAvgVolume = stockTechnicals.getPrevVolumeAvg5();
            prevPrevAvgVolume = stockTechnicals.getPrev2VolumeAvg5();
        }

        boolean isAvgIncreasing = avgVolume > prevAvgVolume;
        boolean isVolumeIncreasing = currentVolume > prevVolume;
        boolean isVolumeAboveAverage = currentVolume > avgVolume;

        double score = 5.0;

        if (isAvgIncreasing && currentVolume > 1.5 * avgVolume) {
            score = 10;
        } else if (isAvgIncreasing && currentVolume > 2 * prevVolume) {
            score = 9;
        } else if (isAvgIncreasing
                && (currentVolume > 1.25 * avgVolume)
                && (prevVolume > 1.25 * prevAvgVolume)) {
            score = 8;
        } else if (isAvgIncreasing && isVolumeIncreasing && isVolumeAboveAverage) {
            score = 7;
        } else if (isAvgIncreasing
                && (prevVolume > 1.25 * prevAvgVolume)
                && (prevPrevVolume > 1.25 * prevPrevAvgVolume)) {
            score = 6;
        }

        return score;
    }

    public static double calculateMacdScore(
            StockTechnicals stockTechnicals, MacdIndicatorService macdIndicatorService) {

        if (macdIndicatorService.isMacdCrossedSignal(stockTechnicals)) {
            return 10;
        } else if (stockTechnicals.getMacd() < stockTechnicals.getSignal()
                || stockTechnicals.getMacd() < 0.0) {

            if (macdIndicatorService.isHistogramBelowZero(stockTechnicals)
                    && macdIndicatorService.isMacdIncreased(stockTechnicals)
                    && macdIndicatorService.isSignalDecreased(stockTechnicals)
                    && macdIndicatorService.isHistogramIncreased(stockTechnicals)) {
                return 9;
            }
        } else if (macdIndicatorService.isMacdIncreased(stockTechnicals)
                && macdIndicatorService.isSignalIncreased(stockTechnicals)
                && macdIndicatorService.isHistogramIncreased(stockTechnicals)) {
            return 8;
        }

        // Default mild bearish
        return 7;
    }
}
