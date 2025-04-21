package com.example.util;

public class ConfidenceScoreCalculator {

    /**
     * Calculates the confidence score out of 10 based on strategy score, risk, and EMA score.
     * Assumes all inputs are on a scale of 0 to 10.
     *
     * @param strategyScore value between 0 and 10 (higher is better)
     * @param risk value between 0 and 10 (higher is worse)
     * @param macdScore value between 0 and 10 (higher is better)
     * @return confidence score between 0 and 10
     */
    public static double calculateConfidenceScore(
            double strategyScore, double risk, double macdScore) {
        // Define weights
        double strategyWeight = 0.5;
        double riskWeight = 0.3;
        double macdWeight = 0.2;

        // Ensure values are clamped between 0 and 10
        strategyScore = clamp(strategyScore);
        risk = clamp(risk);
        macdScore = clamp(macdScore);

        // Invert risk so that lower risk gives higher contribution
        double invertedRisk = 10.0 - risk;

        // Compute final confidence score
        return (strategyScore * strategyWeight)
                + (invertedRisk * riskWeight)
                + (macdScore * macdWeight);
    }

    private static double clamp(double value) {
        return Math.max(0, Math.min(10, value));
    }

    /**
     * Converts a raw risk value (0 to 20) to a normalized risk score (0 to 10), where 0 risk =
     * score 10 (best), and 20 risk = score 0 (worst).
     *
     * @param rawRisk risk value between 0 and 20
     * @return normalized risk score between 0 and 10
     */
    public static double calculateRiskScore(double rawRisk) {
        rawRisk = Math.max(0, Math.min(20, rawRisk)); // clamp to 0–20
        return (1.0 - (rawRisk / 20.0)) * 10.0;
    }
}
