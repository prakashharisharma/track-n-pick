package com.example.service;

import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;

public class StockScoreCalculator {

    public static int calculateScore(
            StockPrice dP,
            StockTechnicals dT,
            StockPrice wP,
            StockTechnicals wT,
            StockPrice mP,
            StockTechnicals mT) {
        int score = 0;

        /* ======================
        1️⃣ MONTHLY (35)
        ====================== */

        if (mP.getClose() > mT.getEma20()) score += 10;
        if (mT.getEma20() > mT.getEma50()) score += 8;
        if (mT.getEma50() > mT.getEma200()) score += 7;

        if (isRising(mT.getEma20(), mT.getPrevEma20())) score += 5;
        if (isRising(mT.getEma50(), mT.getPrevEma50())) score += 5;

        /* ======================
        2️⃣ WEEKLY (30)
        ====================== */

        if (wP.getClose() > wT.getEma20()) score += 8;
        if (wT.getEma20() > wT.getEma50()) score += 7;
        if (wT.getEma50() > wT.getEma200()) score += 5;

        if (isHigherLow(wP)) score += 5;
        if (isRising(wT.getEma20(), wT.getPrevEma20())) score += 5;

        /* ======================
        3️⃣ DAILY ENTRY (15)
        ====================== */

        if (dP.getLow() <= dT.getEma20() && dP.getClose() > dT.getEma20())
            score += 6; // support-based entry

        if (dP.getClose() > dP.getPrevClose()) score += 4;
        if (isBullishCandle(dP)) score += 3;
        if (dP.getLow() > dP.getPrevLow()) score += 2;

        /* ======================
        4️⃣ MOMENTUM (10)
        ====================== */

        if (dT.getRsi() >= 50 && dT.getRsi() <= 70) score += 4;
        if (dT.getMacd() > dT.getSignal()) score += 3;
        if (dT.getAdx() >= 20) score += 3;

        /* ======================
        5️⃣ RISK (10)
        ====================== */

        double riskPct = ((dP.getClose() - dP.getLow()) / dP.getClose()) * 100;

        if (riskPct <= 1.5) score += 10;
        else if (riskPct <= 2.5) score += 7;
        else if (riskPct <= 4) score += 4;
        else score += 2; // wide risk still allowed

        return Math.min(score, 100);
    }

    private static boolean isRising(Double curr, Double prev) {
        return curr != null && prev != null && curr > prev;
    }

    private static boolean isHigherLow(StockPrice p) {
        return p.getLow() > p.getPrevLow();
    }

    private static boolean isBullishCandle(StockPrice p) {
        return p.getClose() > p.getOpen();
    }

    public static double calculateScore(
            StockPrice dailyPrice,
            StockPrice weeklyPrice,
            StockPrice monthlyPrice,
            StockTechnicals dailyTech,
            StockTechnicals weeklyTech,
            StockTechnicals monthlyTech,
            double riskPercent,
            double marketCap) {

        int expectationScore = calculateExpectationScore(dailyPrice, dailyTech, riskPercent);
        int trendScore = calculateTrendScore(dailyTech, weeklyTech, monthlyTech);
        int supportStrength = calculateSupportStrength(dailyPrice);
        int largeCapPenalty = calculateLargeCapPenalty(marketCap);

        return expectationScore + trendScore + supportStrength - largeCapPenalty;
    }

    private static int calculateExpectationScore(
            StockPrice price, StockTechnicals tech, double riskPercent) {
        int riskScore =
                riskPercent <= 2
                        ? 15
                        : riskPercent <= 4 ? 12 : riskPercent <= 6 ? 8 : riskPercent <= 8 ? 4 : 0;

        boolean emaRising = tech.getEma20() > tech.getPrevEma20();
        double emaDistancePct =
                Math.abs(price.getClose() - tech.getEma20()) / tech.getEma20() * 100;

        int emaScore =
                emaRising && emaDistancePct <= 5
                        ? 15
                        : emaRising ? 10 : emaDistancePct <= 3 ? 6 : 0;

        int volatilityScore =
                (price.getHigh() - price.getLow()) / price.getClose() * 100 <= 2
                        ? 10
                        : (price.getHigh() - price.getLow()) / price.getClose() * 100 <= 4 ? 6 : 2;

        return riskScore + emaScore + volatilityScore; // max 40
    }

    private static int calculateTrendScore(
            StockTechnicals daily, StockTechnicals weekly, StockTechnicals monthly) {
        boolean dailyUp = daily.getEma20() > daily.getPrevEma20();
        boolean weeklyUp = weekly.getEma20() > weekly.getPrevEma20();
        boolean monthlyUp = monthly.getEma20() > monthly.getPrevEma20();

        if (dailyUp && weeklyUp && monthlyUp) return 20;
        if (weeklyUp && monthlyUp) return 16;
        if (dailyUp && weeklyUp) return 14;
        if (dailyUp) return 10;

        return 0;
    }

    private static int calculateSupportStrength(StockPrice sp) {

        double[] lows = {
            sp.getLow(),
            sp.getPrevLow(),
            sp.getPrev2Low(),
            sp.getPrev3Low(),
            sp.getPrev4Low(),
            sp.getPrev5Low(),
            sp.getPrev6Low()
        };

        double tolerancePct = 0.4;
        int touches = 0;

        for (int i = 0; i < lows.length; i++) {
            for (int j = i + 1; j < lows.length; j++) {
                double diffPct = Math.abs(lows[i] - lows[j]) / lows[j] * 100;
                if (diffPct <= tolerancePct) {
                    touches++;
                }
            }
        }

        if (touches >= 3) return 20;
        if (touches == 2) return 15;
        if (touches == 1) return 8;
        return 0;
    }

    private static int calculateLargeCapPenalty(double marketCap) {
        if (marketCap > 200_000) return 10;
        if (marketCap > 100_000) return 7;
        if (marketCap > 50_000) return 4;
        return 0;
    }
}
