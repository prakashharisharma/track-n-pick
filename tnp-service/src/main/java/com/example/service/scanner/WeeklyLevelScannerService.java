package com.example.service.scanner;

import com.example.data.transactional.entities.StockPrice;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class WeeklyLevelScannerService {

    private static final double LEVEL_TOLERANCE = 0.02; // 2% tolerance for level proximity
    private static final double BREAKOUT_CONFIRMATION = 0.03; // 3% for confirmed breakout
    private static final double STRONG_HOLD_TOLERANCE = 0.01; // 1% for strong hold

    // Scoring weights
    private static final double YEARLY_WEIGHT = 3.0;
    private static final double QUARTERLY_WEIGHT = 2.0;
    private static final double MONTHLY_WEIGHT = 1.0;
    private static final double WEEKLY_WEIGHT = 0.5;

    /** Main analysis method */
    public WeeklyAnalysisResult analyzeWeeklyCandle(
            StockPrice stockPriceYearly,
            StockPrice stockPriceQuarterly,
            StockPrice stockPriceMonthly,
            StockPrice stockPriceWeekly) {

        WeeklyAnalysisResult result = new WeeklyAnalysisResult();
        result.setWeeklyCandle(stockPriceWeekly);

        // Extract all levels from each timeframe
        List<PriceLevel> allLevels =
                extractAllLevels(
                        stockPriceYearly, stockPriceQuarterly, stockPriceMonthly, stockPriceWeekly);

        // Analyze the weekly candle against all levels
        analyzeAgainstLevels(stockPriceWeekly, allLevels, result);

        // Calculate overall scores
        calculateOverallScores(result);

        // Generate trading signals
        generateTradingSignals(result);

        return result;
    }

    /** Extract all significant price levels from each timeframe */
    private List<PriceLevel> extractAllLevels(
            StockPrice yearly, StockPrice quarterly, StockPrice monthly, StockPrice weekly) {

        List<PriceLevel> levels = new ArrayList<>();

        // Extract from Yearly (current and previous yearly candles)
        extractYearlyLevels(yearly, levels);

        // Extract from Quarterly (current and previous quarterly candles)
        extractQuarterlyLevels(quarterly, levels);

        // Extract from Monthly (current and previous monthly candles)
        extractMonthlyLevels(monthly, levels);

        // Extract from Weekly (current and previous weekly candles)
        extractWeeklyLevels(weekly, levels);

        return levels;
    }

    /** Extract yearly levels (current year + previous years) */
    private void extractYearlyLevels(StockPrice yearly, List<PriceLevel> levels) {
        if (yearly == null) return;

        // Current yearly candle
        levels.add(
                createLevel(
                        yearly.getHigh(),
                        LevelType.YEARLY_HIGH,
                        yearly.getSessionDate(),
                        YEARLY_WEIGHT));
        levels.add(
                createLevel(
                        yearly.getLow(),
                        LevelType.YEARLY_LOW,
                        yearly.getSessionDate(),
                        YEARLY_WEIGHT));
        levels.add(
                createLevel(
                        yearly.getClose(),
                        LevelType.YEARLY_CLOSE,
                        yearly.getSessionDate(),
                        YEARLY_WEIGHT * 0.8));

        // Yearly mid-point
        double yearlyMid = (yearly.getHigh() + yearly.getLow()) / 2;
        levels.add(
                createLevel(
                        yearlyMid,
                        LevelType.YEARLY_MID,
                        yearly.getSessionDate(),
                        YEARLY_WEIGHT * 0.6));

        // Previous yearly candles (up to 3 years back)
        for (int i = 1; i <= 3; i++) {
            double prevHigh = yearly.getHigh(i);
            double prevLow = yearly.getLow(i);
            double prevClose = yearly.getClose(i);

            if (prevHigh > 0 && prevLow > 0) {
                levels.add(
                        createLevel(
                                prevHigh,
                                LevelType.YEARLY_HIGH,
                                yearly.getSessionDate().minusYears(i),
                                YEARLY_WEIGHT * 0.7));
                levels.add(
                        createLevel(
                                prevLow,
                                LevelType.YEARLY_LOW,
                                yearly.getSessionDate().minusYears(i),
                                YEARLY_WEIGHT * 0.7));
                levels.add(
                        createLevel(
                                prevClose,
                                LevelType.YEARLY_CLOSE,
                                yearly.getSessionDate().minusYears(i),
                                YEARLY_WEIGHT * 0.5));

                double prevMid = (prevHigh + prevLow) / 2;
                levels.add(
                        createLevel(
                                prevMid,
                                LevelType.YEARLY_MID,
                                yearly.getSessionDate().minusYears(i),
                                YEARLY_WEIGHT * 0.4));
            }
        }
    }

    /** Extract quarterly levels (current quarter + previous quarters) */
    private void extractQuarterlyLevels(StockPrice quarterly, List<PriceLevel> levels) {
        if (quarterly == null) return;

        // Current quarter
        levels.add(
                createLevel(
                        quarterly.getHigh(),
                        LevelType.QUARTERLY_HIGH,
                        quarterly.getSessionDate(),
                        QUARTERLY_WEIGHT));
        levels.add(
                createLevel(
                        quarterly.getLow(),
                        LevelType.QUARTERLY_LOW,
                        quarterly.getSessionDate(),
                        QUARTERLY_WEIGHT));
        levels.add(
                createLevel(
                        quarterly.getClose(),
                        LevelType.QUARTERLY_CLOSE,
                        quarterly.getSessionDate(),
                        QUARTERLY_WEIGHT * 0.8));

        // Quarterly mid-point
        double quarterlyMid = (quarterly.getHigh() + quarterly.getLow()) / 2;
        levels.add(
                createLevel(
                        quarterlyMid,
                        LevelType.QUARTERLY_MID,
                        quarterly.getSessionDate(),
                        QUARTERLY_WEIGHT * 0.6));

        // Previous quarters (up to 4 quarters back)
        for (int i = 1; i <= 4; i++) {
            double prevHigh = quarterly.getHigh(i);
            double prevLow = quarterly.getLow(i);
            double prevClose = quarterly.getClose(i);

            if (prevHigh > 0 && prevLow > 0) {
                levels.add(
                        createLevel(
                                prevHigh,
                                LevelType.QUARTERLY_HIGH,
                                quarterly.getSessionDate().minusMonths(i * 3),
                                QUARTERLY_WEIGHT * 0.7));
                levels.add(
                        createLevel(
                                prevLow,
                                LevelType.QUARTERLY_LOW,
                                quarterly.getSessionDate().minusMonths(i * 3),
                                QUARTERLY_WEIGHT * 0.7));
                levels.add(
                        createLevel(
                                prevClose,
                                LevelType.QUARTERLY_CLOSE,
                                quarterly.getSessionDate().minusMonths(i * 3),
                                QUARTERLY_WEIGHT * 0.5));

                double prevMid = (prevHigh + prevLow) / 2;
                levels.add(
                        createLevel(
                                prevMid,
                                LevelType.QUARTERLY_MID,
                                quarterly.getSessionDate().minusMonths(i * 3),
                                QUARTERLY_WEIGHT * 0.4));
            }
        }
    }

    /** Extract monthly levels (current month + previous months) */
    private void extractMonthlyLevels(StockPrice monthly, List<PriceLevel> levels) {
        if (monthly == null) return;

        // Current month
        levels.add(
                createLevel(
                        monthly.getHigh(),
                        LevelType.MONTHLY_HIGH,
                        monthly.getSessionDate(),
                        MONTHLY_WEIGHT));
        levels.add(
                createLevel(
                        monthly.getLow(),
                        LevelType.MONTHLY_LOW,
                        monthly.getSessionDate(),
                        MONTHLY_WEIGHT));
        levels.add(
                createLevel(
                        monthly.getClose(),
                        LevelType.MONTHLY_CLOSE,
                        monthly.getSessionDate(),
                        MONTHLY_WEIGHT * 0.8));

        // Monthly mid-point
        double monthlyMid = (monthly.getHigh() + monthly.getLow()) / 2;
        levels.add(
                createLevel(
                        monthlyMid,
                        LevelType.MONTHLY_MID,
                        monthly.getSessionDate(),
                        MONTHLY_WEIGHT * 0.6));

        // Previous months (up to 6 months back)
        for (int i = 1; i <= 6; i++) {
            double prevHigh = monthly.getHigh(i);
            double prevLow = monthly.getLow(i);
            double prevClose = monthly.getClose(i);

            if (prevHigh > 0 && prevLow > 0) {
                levels.add(
                        createLevel(
                                prevHigh,
                                LevelType.MONTHLY_HIGH,
                                monthly.getSessionDate().minusMonths(i),
                                MONTHLY_WEIGHT * 0.7));
                levels.add(
                        createLevel(
                                prevLow,
                                LevelType.MONTHLY_LOW,
                                monthly.getSessionDate().minusMonths(i),
                                MONTHLY_WEIGHT * 0.7));
                levels.add(
                        createLevel(
                                prevClose,
                                LevelType.MONTHLY_CLOSE,
                                monthly.getSessionDate().minusMonths(i),
                                MONTHLY_WEIGHT * 0.5));

                double prevMid = (prevHigh + prevLow) / 2;
                levels.add(
                        createLevel(
                                prevMid,
                                LevelType.MONTHLY_MID,
                                monthly.getSessionDate().minusMonths(i),
                                MONTHLY_WEIGHT * 0.4));
            }
        }
    }

    /** Extract weekly levels (current week + previous weeks) */
    private void extractWeeklyLevels(StockPrice weekly, List<PriceLevel> levels) {
        if (weekly == null) return;

        // Current week
        levels.add(
                createLevel(
                        weekly.getHigh(),
                        LevelType.WEEKLY_HIGH,
                        weekly.getSessionDate(),
                        WEEKLY_WEIGHT));
        levels.add(
                createLevel(
                        weekly.getLow(),
                        LevelType.WEEKLY_LOW,
                        weekly.getSessionDate(),
                        WEEKLY_WEIGHT));
        levels.add(
                createLevel(
                        weekly.getClose(),
                        LevelType.WEEKLY_CLOSE,
                        weekly.getSessionDate(),
                        WEEKLY_WEIGHT * 0.8));

        // Weekly mid-point
        double weeklyMid = (weekly.getHigh() + weekly.getLow()) / 2;
        levels.add(
                createLevel(
                        weeklyMid,
                        LevelType.WEEKLY_MID,
                        weekly.getSessionDate(),
                        WEEKLY_WEIGHT * 0.6));

        // Previous weeks (up to 4 weeks back)
        for (int i = 1; i <= 4; i++) {
            double prevHigh = weekly.getHigh(i);
            double prevLow = weekly.getLow(i);
            double prevClose = weekly.getClose(i);

            if (prevHigh > 0 && prevLow > 0) {
                levels.add(
                        createLevel(
                                prevHigh,
                                LevelType.WEEKLY_HIGH,
                                weekly.getSessionDate().minusWeeks(i),
                                WEEKLY_WEIGHT * 0.7));
                levels.add(
                        createLevel(
                                prevLow,
                                LevelType.WEEKLY_LOW,
                                weekly.getSessionDate().minusWeeks(i),
                                WEEKLY_WEIGHT * 0.7));
                levels.add(
                        createLevel(
                                prevClose,
                                LevelType.WEEKLY_CLOSE,
                                weekly.getSessionDate().minusWeeks(i),
                                WEEKLY_WEIGHT * 0.5));

                double prevMid = (prevHigh + prevLow) / 2;
                levels.add(
                        createLevel(
                                prevMid,
                                LevelType.WEEKLY_MID,
                                weekly.getSessionDate().minusWeeks(i),
                                WEEKLY_WEIGHT * 0.4));
            }
        }
    }

    /** Analyze weekly candle against all extracted levels */
    private void analyzeAgainstLevels(
            StockPrice weekly, List<PriceLevel> levels, WeeklyAnalysisResult result) {
        double weeklyClose = weekly.getClose();
        double weeklyHigh = weekly.getHigh();
        double weeklyLow = weekly.getLow();

        List<PriceLevel> supports = new ArrayList<>();
        List<PriceLevel> resistances = new ArrayList<>();
        List<LevelInteraction> interactions = new ArrayList<>();

        for (PriceLevel level : levels) {
            LevelInteraction interaction = analyzeSingleLevel(weekly, level);
            interactions.add(interaction);

            // Categorize as support or resistance
            if (interaction.getDistanceFromClose() > 0) {
                resistances.add(level);
            } else {
                supports.add(level);
            }

            // Store strong interactions
            if (interaction.getStrength() >= 3) {
                result.getStrongInteractions().add(interaction);
            }

            // Check for breakouts
            if (interaction.isBreakout()
                    && interaction.getBreakoutPercent() >= BREAKOUT_CONFIRMATION) {
                result.getConfirmedBreakouts().add(interaction);
            }

            // Check for strong holds
            if (interaction.isStrongHold()
                    && Math.abs(interaction.getDistanceFromClose()) <= STRONG_HOLD_TOLERANCE) {
                result.getStrongHolds().add(interaction);
            }
        }

        // Sort supports (closest first)
        supports.sort(Comparator.comparing(l -> Math.abs(l.getPrice() - weeklyClose)));
        resistances.sort(Comparator.comparing(l -> Math.abs(l.getPrice() - weeklyClose)));

        result.setSupportLevels(supports);
        result.setResistanceLevels(resistances);
        result.setAllInteractions(interactions);

        // Find nearest support and resistance
        if (!supports.isEmpty()) {
            result.setNearestSupport(supports.get(0));
            result.setSupportDistance(weeklyClose - supports.get(0).getPrice());
        }

        if (!resistances.isEmpty()) {
            result.setNearestResistance(resistances.get(0));
            result.setResistanceDistance(resistances.get(0).getPrice() - weeklyClose);
        }
    }

    /** Analyze interaction between weekly candle and a single price level */
    private LevelInteraction analyzeSingleLevel(StockPrice weekly, PriceLevel level) {
        LevelInteraction interaction = new LevelInteraction();
        interaction.setLevel(level);

        double weeklyClose = weekly.getClose();
        double weeklyHigh = weekly.getHigh();
        double weeklyLow = weekly.getLow();
        double levelPrice = level.getPrice();

        // Calculate distances
        double distanceFromClose = weeklyClose - levelPrice;
        double distancePercent = Math.abs(distanceFromClose) / weeklyClose;

        interaction.setDistanceFromClose(distanceFromClose);
        interaction.setDistancePercent(distancePercent);

        // Check if level was tested this week
        boolean highTested = Math.abs(weeklyHigh - levelPrice) / weeklyHigh <= LEVEL_TOLERANCE;
        boolean lowTested = Math.abs(weeklyLow - levelPrice) / weeklyLow <= LEVEL_TOLERANCE;
        interaction.setTested(highTested || lowTested);
        interaction.setHighTested(highTested);
        interaction.setLowTested(lowTested);

        // Check for breakout/breakdown
        boolean isBreakout =
                (level.getLevelType().name().contains("HIGH") && weeklyClose > levelPrice)
                        || (level.getLevelType().name().contains("LOW")
                                && weeklyClose < levelPrice);
        interaction.setBreakout(isBreakout);

        if (isBreakout) {
            double breakoutPercent = Math.abs(distanceFromClose) / levelPrice;
            interaction.setBreakoutPercent(breakoutPercent);
        }

        // Check for strong hold
        boolean isStrongHold =
                distancePercent <= STRONG_HOLD_TOLERANCE
                        && (weeklyClose > weeklyLow * 1.01)
                        && // Not at extreme low
                        (weeklyClose < weeklyHigh * 0.99); // Not at extreme high
        interaction.setStrongHold(isStrongHold);

        // Calculate interaction strength
        double strength = calculateInteractionStrength(weekly, level, interaction);
        interaction.setStrength(strength);

        return interaction;
    }

    /** Calculate strength of interaction (0-5) */
    private double calculateInteractionStrength(
            StockPrice weekly, PriceLevel level, LevelInteraction interaction) {
        double strength = level.getWeight(); // Base from timeframe

        // Add for testing
        if (interaction.isTested()) {
            strength += 1.0;

            // Extra strength if both high and low tested (range bound)
            if (interaction.isHighTested() && interaction.isLowTested()) {
                strength += 0.5;
            }
        }

        // Add for strong hold
        if (interaction.isStrongHold()) {
            strength += 2.0;
        }

        // Add for confirmed breakout
        if (interaction.isBreakout() && interaction.getBreakoutPercent() >= BREAKOUT_CONFIRMATION) {
            strength += interaction.getBreakoutPercent() * 100; // 3% breakout = +3
        }

        // Add for candle closing away from extreme
        double weeklyRange = weekly.getHigh() - weekly.getLow();
        double closePosition = (weekly.getClose() - weekly.getLow()) / weeklyRange;

        if (closePosition > 0.3 && closePosition < 0.7) {
            strength += 0.5; // Closed in middle third of range
        }

        return Math.min(5.0, strength); // Cap at 5
    }

    /** Calculate overall scores */
    private void calculateOverallScores(WeeklyAnalysisResult result) {
        List<LevelInteraction> interactions = result.getAllInteractions();

        if (interactions.isEmpty()) {
            result.setOverallScore(0);
            result.setBreakoutScore(0);
            result.setSupportScore(0);
            return;
        }

        double totalScore = 0;
        double breakoutScore = 0;
        double supportScore = 0;
        int breakoutCount = 0;
        int supportCount = 0;

        for (LevelInteraction interaction : interactions) {
            totalScore += interaction.getStrength() * interaction.getLevel().getWeight();

            if (interaction.isBreakout() && interaction.getBreakoutPercent() >= 0.01) {
                breakoutScore += interaction.getStrength() * interaction.getLevel().getWeight();
                breakoutCount++;
            }

            if (interaction.isStrongHold()) {
                supportScore += interaction.getStrength() * interaction.getLevel().getWeight();
                supportCount++;
            }
        }

        // Overall score (0-100)
        double maxPossibleScore = interactions.size() * 5 * YEARLY_WEIGHT; // Max weight
        result.setOverallScore((totalScore / maxPossibleScore) * 100);

        // Breakout score (0-100)
        result.setBreakoutScore(
                breakoutCount > 0
                        ? (breakoutScore / (breakoutCount * 5 * YEARLY_WEIGHT)) * 100
                        : 0);

        // Support score (0-100)
        result.setSupportScore(
                supportCount > 0 ? (supportScore / (supportCount * 5 * YEARLY_WEIGHT)) * 100 : 0);

        // Calculate risk-reward if we have both support and resistance
        if (result.getNearestSupport() != null && result.getNearestResistance() != null) {
            double risk = result.getSupportDistance();
            double reward = result.getResistanceDistance();

            if (risk > 0) {
                result.setRiskRewardRatio(reward / risk);
            }
        }
    }

    /** Generate trading signals */
    private void generateTradingSignals(WeeklyAnalysisResult result) {
        double overallScore = result.getOverallScore();
        double breakoutScore = result.getBreakoutScore();
        double supportScore = result.getSupportScore();

        // Determine primary signal
        if (breakoutScore >= 70 && overallScore >= 65) {
            result.setPrimarySignal(TradingSignal.STRONG_BREAKOUT);
        } else if (breakoutScore >= 50 && overallScore >= 50) {
            result.setPrimarySignal(TradingSignal.BREAKOUT);
        } else if (supportScore >= 70 && overallScore >= 65) {
            result.setPrimarySignal(TradingSignal.STRONG_SUPPORT);
        } else if (supportScore >= 50 && overallScore >= 50) {
            result.setPrimarySignal(TradingSignal.SUPPORT_HOLD);
        } else if (overallScore >= 60) {
            result.setPrimarySignal(TradingSignal.NEUTRAL_BULLISH);
        } else if (overallScore >= 40) {
            result.setPrimarySignal(TradingSignal.NEUTRAL);
        } else {
            result.setPrimarySignal(TradingSignal.NEUTRAL_BEARISH);
        }

        // Determine confidence level
        if (overallScore >= 80) result.setConfidence(ConfidenceLevel.VERY_HIGH);
        else if (overallScore >= 65) result.setConfidence(ConfidenceLevel.HIGH);
        else if (overallScore >= 50) result.setConfidence(ConfidenceLevel.MODERATE);
        else if (overallScore >= 35) result.setConfidence(ConfidenceLevel.LOW);
        else result.setConfidence(ConfidenceLevel.VERY_LOW);

        // Generate summary
        generateSummary(result);
    }

    /** Generate analysis summary */
    private void generateSummary(WeeklyAnalysisResult result) {
        StringBuilder summary = new StringBuilder();

        summary.append(
                String.format(
                        "Weekly Analysis - Overall Score: %.1f/100\n", result.getOverallScore()));
        summary.append(
                String.format(
                        "Signal: %s (Confidence: %s)\n",
                        result.getPrimarySignal().getDescription(),
                        result.getConfidence().getDescription()));

        summary.append(
                String.format(
                        "Breakout Score: %.1f/100 | Support Score: %.1f/100\n",
                        result.getBreakoutScore(), result.getSupportScore()));

        if (result.getNearestSupport() != null) {
            summary.append(
                    String.format(
                            "Nearest Support: %.2f (%s, Distance: %.2f%%)\n",
                            result.getNearestSupport().getPrice(),
                            result.getNearestSupport().getLevelType(),
                            result.getSupportDistance()
                                    / result.getWeeklyCandle().getClose()
                                    * 100));
        }

        if (result.getNearestResistance() != null) {
            summary.append(
                    String.format(
                            "Nearest Resistance: %.2f (%s, Distance: %.2f%%)\n",
                            result.getNearestResistance().getPrice(),
                            result.getNearestResistance().getLevelType(),
                            result.getResistanceDistance()
                                    / result.getWeeklyCandle().getClose()
                                    * 100));
        }

        if (result.getRiskRewardRatio() > 0) {
            summary.append(
                    String.format("Risk/Reward Ratio: 1:%.1f\n", result.getRiskRewardRatio()));
        }

        summary.append(
                String.format(
                        "Strong Interactions: %d | Breakouts: %d | Strong Holds: %d\n",
                        result.getStrongInteractions().size(),
                        result.getConfirmedBreakouts().size(),
                        result.getStrongHolds().size()));

        result.setSummary(summary.toString());
    }

    private PriceLevel createLevel(double price, LevelType type, LocalDate date, double weight) {
        PriceLevel level = new PriceLevel();
        level.setPrice(price);
        level.setLevelType(type);
        level.setDate(date);
        level.setWeight(weight);
        return level;
    }
}
