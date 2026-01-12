package com.example.service;

import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import java.util.HashMap;
import java.util.Map;

public class StockScanner {

    public enum ScoreMode {
        Mean_Reversion,
        Trend_Continuation,
        Both,
        None
    }

    private static final int LOOKBACK = 11; // Last 5 sessions

    // -----------------------
    // Mean-Reversion Scoring
    // -----------------------
    public static double scoreMeanReversion(
            StockPrice sp,
            double ema5,
            double ema20,
            double rsi,
            double volume,
            double volumeAvg20,
            double yearLow,
            double yearHigh) {

        double score = 0;

        // 1️⃣ Year-Low Proximity
        double proximity = (ema5 - yearLow) / (yearHigh - yearLow);
        proximity = Math.max(0, Math.min(1, proximity));
        score += 5 * (1 - proximity);

        // 2️⃣ EMA Support
        double emaDiff = Math.abs(ema5 - ema20) / ema20;
        score += emaDiff <= 0.05 ? 2 : Math.max(0, 2 - (emaDiff * 20));

        // 3️⃣ RSI Oversold
        if (rsi < 50) score += 2;

        // 4️⃣ Volume vs 20-day Avg
        double volRatio = volume / volumeAvg20;
        if (volRatio >= 0.8 && volRatio <= 2.0) score += 1;

        // 5️⃣ Volatility Compression (last 5 closes)
        double maxClose = sp.getClose(0);
        double minClose = sp.getClose(0);
        for (int i = 0; i < LOOKBACK; i++) {
            double close = sp.getClose(i);
            if (close > maxClose) maxClose = close;
            if (close < minClose) minClose = close;
        }
        double compression = (maxClose - minClose) / ema5;
        if (compression < 0.05) score += 2;
        else if (compression < 0.08) score += 1;

        // 6️⃣ Support-based reversal bonus
        // score += scoreSupportReversalBonus(sp);

        return Math.min(score, 10);
    }

    // -----------------------
    // Trend Continuation Scoring
    // -----------------------
    public static double scoreTrendContinuation(
            StockPrice sp,
            double ema5,
            double ema20,
            double ema50,
            double rsi,
            double volume,
            double volumeAvg20) {

        double score = 0;

        // 1️⃣ EMA Alignment
        if (ema5 > ema20 && ema20 > ema50) score += 3;

        // 2️⃣ EMA Pullback
        double recentHigh = sp.getHigh(0);
        double pullback = (recentHigh - ema5) / recentHigh;
        if (pullback >= 0 && pullback <= 0.08) score += 3;

        // 3️⃣ RSI Zone
        if (rsi >= 50 && rsi <= 70) score += 2;
        else score += Math.max(0, 2 - Math.abs(rsi - 60) / 5);

        // 4️⃣ Volume confirmation
        double volRatio = volume / volumeAvg20;
        if (volRatio >= 1) score += 1;

        // 5️⃣ Weighted Higher-Lows
        double weight = 0;
        double prevClose = sp.getClose(LOOKBACK - 1);
        for (int i = LOOKBACK - 2; i >= 0; i--) {
            double close = sp.getClose(i);
            if (close > prevClose) weight += 1;
            else if (close > prevClose * 0.98) weight += 0.5;
            prevClose = close;
        }
        score += Math.min(1, weight / (LOOKBACK - 1));

        // 6️⃣ Bonus: strong momentum
        if (rsi > 60 && volRatio > 1.2) score += 0.5;

        // 7️⃣ Support-based pullback confirmation (lighter weight)
        // score += scoreSupportReversalBonus(sp) * 0.5;

        return Math.min(score, 10);
    }

    // -----------------------
    // Scanner Decision
    // -----------------------
    public static Map<ScoreMode, Double> evaluateStock(
            StockPrice sp, StockTechnicals st, double yearLow, double yearHigh) {

        Map<ScoreMode, Double> scoreResult = new HashMap<>();

        double ema5 = st.getEma5();
        double ema20 = st.getEma20();
        double ema50 = st.getEma50();
        double rsi = st.getRsi();
        double volume = st.getVolume();
        double volumeAvg20 = st.getVolumeAvg20();

        double meanRevScore =
                scoreMeanReversion(sp, ema5, ema20, rsi, volume, volumeAvg20, yearLow, yearHigh);
        double trendScore =
                scoreTrendContinuation(sp, ema5, ema20, ema50, rsi, volume, volumeAvg20);

        if (meanRevScore >= 7 && trendScore >= 7) {
            scoreResult.put(ScoreMode.Both, Math.max(meanRevScore, trendScore));
        } else if (meanRevScore >= 7) {
            scoreResult.put(ScoreMode.Mean_Reversion, meanRevScore);
        } else if (trendScore >= 7) {
            scoreResult.put(ScoreMode.Trend_Continuation, trendScore);
        } else {
            scoreResult.put(ScoreMode.None, Math.max(meanRevScore, trendScore));
        }

        return scoreResult;
    }

    // -----------------------
    // Support Reversal Bonus
    // -----------------------
    private static double scoreSupportReversalBonus(StockPrice sp) {

        if (!MASupportChecker.isPreviousDowntrend(sp)) {
            return 0.0;
        }

        double support = SupportFinder.findSupport(sp, 11);
        if (support <= 0) {
            return 0.0;
        }

        boolean isSupport =
                sp.getLow(0) <= support && Math.min(sp.getOpen(0), sp.getClose(0)) > support;

        return isSupport ? 1.5 : 0.0;
    }
}
