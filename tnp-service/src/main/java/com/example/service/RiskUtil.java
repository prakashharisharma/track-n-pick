package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;

public class RiskUtil {

    public static final double MIN_RISK = 6.0;
    public static final double MAX_RISK = 8.0;
    public static final double MIN_RISK_WEEKLY = 8.0;
    public static final double MAX_RISK_WEEKLY = 12.0;
    public static final double MIN_RISK_MONTHLY = 12.0;
    public static final double MAX_RISK_MONTHLY = 18.0;

    public static double minRisk(Timeframe timeframe) {
        if (timeframe == Timeframe.WEEKLY) {
            return MIN_RISK_WEEKLY;
        }
        if (timeframe == Timeframe.MONTHLY) {
            return MIN_RISK_MONTHLY;
        }

        return MIN_RISK;
    }

    public static double maxRisk(Timeframe timeframe) {
        if (timeframe == Timeframe.WEEKLY) {
            return MAX_RISK_WEEKLY;
        }
        if (timeframe == Timeframe.MONTHLY) {
            return MAX_RISK_MONTHLY;
        }

        return MAX_RISK;
    }

    /**
     * Calculate risk % based on entry price and stoploss
     *
     * @param researchTechnical The trade info containing entryPrice and stopLoss
     * @param isLong True if trade is long, false if short
     * @return risk in percentage
     */
    public static double calculateRiskPercent(ResearchTechnical researchTechnical, boolean isLong) {

        double entryPrice = researchTechnical.getEntryPrice();
        double stopLoss = researchTechnical.getStopLoss();

        if (entryPrice <= 0 || stopLoss <= 0) {
            throw new IllegalArgumentException("Entry price and stoploss must be > 0");
        }

        double risk;

        if (isLong) {
            risk = entryPrice - stopLoss;
        } else {
            risk = stopLoss - entryPrice;
        }

        return (risk / entryPrice) * 100.0;
    }
}
