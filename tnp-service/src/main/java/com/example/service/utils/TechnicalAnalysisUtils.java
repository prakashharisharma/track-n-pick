package com.example.service.utils;

import com.example.data.transactional.entities.StockPrice;
import java.util.*;

/**
 * Utility class for technical analysis of stock prices Provides resistance level detection based on
 * OHLC data
 */
public class TechnicalAnalysisUtils {

    /**
     * Main method to find resistance levels using the three-level checking logic FIXED: Now checks
     * that resistance level hasn't been broken by any session
     */
    public static List<ResistanceLevel> findResistanceLevels(StockPrice stockPrice) {
        List<PriceSession> sessions = convertToSessions(stockPrice);
        List<ResistanceLevel> resistanceLevels = new ArrayList<>();

        double currentBodyTop = Math.max(sessions.get(0).open, sessions.get(0).close);
        double currentHigh = sessions.get(0).high;

        // System.out.printf("Current body top: %.2f, Current high: %.2f%n", currentBodyTop,
        // currentHigh);

        // Level 1: Check 1% above current body top
        double level1 = currentBodyTop * 1.01;
        ResistanceLevel resistance1 = checkResistanceAtLevel(sessions, level1, "1%");
        if (resistance1 != null) {
            resistanceLevels.add(resistance1);
            return resistanceLevels;
        }

        // Level 2: Check 2% above current body top (only if current high > level1)
        if (currentHigh > level1) {
            double level2 = currentBodyTop * 1.02;
            ResistanceLevel resistance2 = checkResistanceAtLevel(sessions, level2, "2%");
            if (resistance2 != null) {
                resistanceLevels.add(resistance2);
                return resistanceLevels;
            }
        }

        // Level 3: Check at current high (only if current high > level2)
        double level2 = currentBodyTop * 1.02;
        if (currentHigh > level2) {
            ResistanceLevel resistance3 =
                    checkResistanceAtLevel(sessions, currentHigh, "Current High");
            if (resistance3 != null) {
                resistanceLevels.add(resistance3);
                return resistanceLevels;
            }
        }

        return resistanceLevels;
    }

    /**
     * Check if at least 2 sessions qualify for resistance at the given level FIXED: Now validates
     * that no session has broken through this level
     */
    private static ResistanceLevel checkResistanceAtLevel(
            List<PriceSession> sessions, double resistanceLevel, String levelType) {
        //  System.out.printf("Checking %s level: %.2f%n", levelType, resistanceLevel);

        int qualifyingSessions = 0;
        List<PriceSession> rejectingSessions = new ArrayList<>();
        boolean levelBroken = false;

        for (PriceSession session : sessions) {
            double sessionBodyTop = Math.max(session.open, session.close);

            // Check if any session has clearly broken this resistance level
            if (sessionBodyTop > resistanceLevel) {
                //  System.out.printf("  Level BROKEN by session: BodyTop=%.2f > Resistance=%.2f%n",
                // sessionBodyTop, resistanceLevel);
                levelBroken = true;
            }

            // FIXED: Check if session qualifies as rejection at this level
            // Now includes sessions where high == resistance level
            boolean highAtOrAbove = session.high >= resistanceLevel;
            boolean bodyBelow = sessionBodyTop < resistanceLevel;

            if (highAtOrAbove && bodyBelow) {
                qualifyingSessions++;
                rejectingSessions.add(session);
                //  System.out.printf("  Session qualifies: High=%.2f, BodyTop=%.2f%n",
                // session.high, sessionBodyTop);
            }
        }

        // If level has been broken by any session, it cannot be valid resistance
        if (levelBroken) {
            //  System.out.printf("✗ Level %.2f INVALID - broken by previous session(s)%n",
            // resistanceLevel);
            return null;
        }

        // If we have at least 2 qualifying sessions and level wasn't broken, create resistance
        // level
        if (qualifyingSessions >= 2) {
            // System.out.printf("✓ Resistance found at %s level: %.2f with %d sessions%n",
            // levelType, resistanceLevel, qualifyingSessions);
            ResistanceLevel level = new ResistanceLevel();
            level.setPrice(resistanceLevel);
            level.setTouchCount(qualifyingSessions);
            level.setRejectingSessions(rejectingSessions);
            level.setLevelType(levelType);
            level.setStrengthScore(calculateStrengthScore(level, sessions));
            return level;
        } else {
            // System.out.printf("✗ Not enough sessions at %s level: %d (need 2)%n", levelType,
            // qualifyingSessions);
            return null;
        }
    }

    /** Find the strongest resistance level */
    public static Optional<ResistanceLevel> findStrongestResistance(StockPrice stockPrice) {
        List<ResistanceLevel> resistances = findResistanceLevels(stockPrice);
        return resistances.isEmpty() ? Optional.empty() : Optional.of(resistances.get(0));
    }

    private static double calculateStrengthScore(
            ResistanceLevel level, List<PriceSession> allSessions) {
        double baseScore = level.getTouchCount() * 50;

        // Bonus for recent rejections
        double recencyBonus =
                level.getRejectingSessions().stream()
                        .mapToDouble(
                                session -> {
                                    int index = allSessions.indexOf(session);
                                    return Math.max(0, 30 - index * 5);
                                })
                        .sum();
        baseScore += recencyBonus;

        return baseScore;
    }

    /** Convert StockPrice entity to list of PriceSession objects */
    private static List<PriceSession> convertToSessions(StockPrice stockPrice) {
        List<PriceSession> sessions = new ArrayList<>();

        // Add current session
        sessions.add(
                new PriceSession(
                        stockPrice.getOpen(),
                        stockPrice.getHigh(),
                        stockPrice.getLow(),
                        stockPrice.getClose()));

        // Add previous sessions
        addSessionIfValid(
                sessions,
                stockPrice.getPrevOpen(),
                stockPrice.getPrevHigh(),
                stockPrice.getPrevLow(),
                stockPrice.getPrevClose());
        addSessionIfValid(
                sessions,
                stockPrice.getPrev2Open(),
                stockPrice.getPrev2High(),
                stockPrice.getPrev2Low(),
                stockPrice.getPrev2Close());
        addSessionIfValid(
                sessions,
                stockPrice.getPrev3Open(),
                stockPrice.getPrev3High(),
                stockPrice.getPrev3Low(),
                stockPrice.getPrev3Close());
        addSessionIfValid(
                sessions,
                stockPrice.getPrev4Open(),
                stockPrice.getPrev4High(),
                stockPrice.getPrev4Low(),
                stockPrice.getPrev4Close());
        addSessionIfValid(
                sessions,
                stockPrice.getPrev5Open(),
                stockPrice.getPrev5High(),
                stockPrice.getPrev5Low(),
                stockPrice.getPrev5Close());

        return sessions;
    }

    private static void addSessionIfValid(
            List<PriceSession> sessions, Double open, Double high, Double low, Double close) {
        if (open != null
                && high != null
                && low != null
                && close != null
                && open > 0
                && high > 0
                && low > 0
                && close > 0) {
            sessions.add(new PriceSession(open, high, low, close));
        }
    }

    // ==================== POJO CLASSES ====================

    /** Represents a single price session (OHLC data) */
    public static class PriceSession {
        private final double open;
        private final double high;
        private final double low;
        private final double close;

        public PriceSession(double open, double high, double low, double close) {
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
        }

        // Getters
        public double getOpen() {
            return open;
        }

        public double getHigh() {
            return high;
        }

        public double getLow() {
            return low;
        }

        public double getClose() {
            return close;
        }

        @Override
        public String toString() {
            return String.format(
                    "PriceSession{O:%.2f, H:%.2f, L:%.2f, C:%.2f}", open, high, low, close);
        }
    }

    /** Represents a detected resistance level */
    public static class ResistanceLevel {
        private double price;
        private int touchCount;
        private double strengthScore;
        private List<PriceSession> rejectingSessions;
        private String levelType;

        // Getters and Setters
        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public int getTouchCount() {
            return touchCount;
        }

        public void setTouchCount(int touchCount) {
            this.touchCount = touchCount;
        }

        public double getStrengthScore() {
            return strengthScore;
        }

        public void setStrengthScore(double strengthScore) {
            this.strengthScore = strengthScore;
        }

        public List<PriceSession> getRejectingSessions() {
            return rejectingSessions;
        }

        public void setRejectingSessions(List<PriceSession> rejectingSessions) {
            this.rejectingSessions = rejectingSessions;
        }

        public String getLevelType() {
            return levelType;
        }

        public void setLevelType(String levelType) {
            this.levelType = levelType;
        }

        @Override
        public String toString() {
            return String.format(
                    "ResistanceLevel{price=%.2f, touches=%d, strength=%.1f, type=%s}",
                    price, touchCount, strengthScore, levelType);
        }
    }

    /** Test utility for quick resistance analysis */
    public static class ResistanceTester {
        public static void testResistance(StockPrice stockPrice, String datasetName) {
            // System.out.println("\n=== Testing: " + datasetName + " ===");
            Optional<ResistanceLevel> result =
                    TechnicalAnalysisUtils.findStrongestResistance(stockPrice);
            if (result.isPresent()) {
                //  System.out.println("✓ RESULT: " + result.get());
            } else {
                //  System.out.println("✗ RESULT: No resistance level found");
            }
        }
    }
}
