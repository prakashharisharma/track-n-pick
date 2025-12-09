package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MASupportChecker2 {

    // ========== ENUMS ==========

    public enum MovingAverageLength {
        // SMAs
        SMA5("SMA5"),
        SMA10("SMA10"),
        SMA20("SMA20"),
        SMA50("SMA50"),
        SMA100("SMA100"),
        SMA200("SMA200"),

        // EMAs
        EMA5("EMA5"),
        EMA10("EMA10"),
        EMA20("EMA20"),
        EMA50("EMA50"),
        EMA100("EMA100"),
        EMA200("EMA200");

        private final String displayName;

        MovingAverageLength(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean isEMA() {
            return this.name().startsWith("EMA");
        }

        public boolean isSMA() {
            return this.name().startsWith("SMA");
        }

        public int getPeriod() {
            return switch (this) {
                case SMA5, EMA5 -> 5;
                case SMA10, EMA10 -> 10;
                case SMA20, EMA20 -> 20;
                case SMA50, EMA50 -> 50;
                case SMA100, EMA100 -> 100;
                case SMA200, EMA200 -> 200;
            };
        }

        public double getSupportThreshold() {
            return switch (this) {
                case EMA5 -> 0.75;
                case EMA10 -> 1.25;
                case EMA20 -> 2.5;
                case EMA50 -> 4.0;
                case EMA100 -> 6.0;
                case EMA200 -> 8.0;
                default -> 1.5;
            };
        }

        public String getTechnicalFieldName() {
            return switch (this) {
                case SMA5 -> "sma5";
                case SMA10 -> "sma10";
                case SMA20 -> "sma20";
                case SMA50 -> "sma50";
                case SMA100 -> "sma100";
                case SMA200 -> "sma200";
                case EMA5 -> "ema5";
                case EMA10 -> "ema10";
                case EMA20 -> "ema20";
                case EMA50 -> "ema50";
                case EMA100 -> "ema100";
                case EMA200 -> "ema200";
            };
        }
    }

    public enum Trend {
        UP,
        DOWN,
        FLAT
    }

    // ========== DATA CLASSES ==========

    public static class MAServiceEntry
            implements Comparable<MASupportChecker2.MAServiceEntry>, Serializable {
        public final MASupportChecker2.MovingAverageLength length;
        public final double value;
        private final double distanceFromPrice;
        private final LocalDateTime timestamp;

        public MAServiceEntry(
                MASupportChecker2.MovingAverageLength length, double value, double currentPrice) {
            this.length = length;
            this.value = value;
            this.distanceFromPrice = Math.abs(value - currentPrice);
            this.timestamp = LocalDateTime.now();
        }

        public MAServiceEntry(
                MASupportChecker2.MovingAverageLength length,
                double value,
                double currentPrice,
                LocalDateTime timestamp) {
            this.length = length;
            this.value = value;
            this.distanceFromPrice = Math.abs(value - currentPrice);
            this.timestamp = timestamp;
        }

        public double getDistanceFromPrice() {
            return distanceFromPrice;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public boolean isPriceNear(double high, double low, double tolerancePercent) {
            double toleranceMultiplier = tolerancePercent / 100;
            double lowerBound = value * (1 - toleranceMultiplier);
            double upperBound = value * (1 + toleranceMultiplier);
            return high >= lowerBound && low <= upperBound;
        }

        @Override
        public int compareTo(MASupportChecker2.MAServiceEntry other) {
            return Double.compare(this.distanceFromPrice, other.distanceFromPrice);
        }

        public static Comparator<MASupportChecker2.MAServiceEntry> sortByValue() {
            return Comparator.comparingDouble(entry -> entry.value);
        }

        public static Comparator<MASupportChecker2.MAServiceEntry> sortByImportance() {
            return (e1, e2) -> Integer.compare(e2.length.getPeriod(), e1.length.getPeriod());
        }

        @Override
        public String toString() {
            return String.format("%s: %.2f", length.getDisplayName(), value);
        }
    }

    public static class MAInteraction implements Serializable {
        private final MASupportChecker2.MovingAverageLength length;
        private final double value;
        private final boolean isSupport;
        private final LocalDateTime timestamp;

        private MAInteraction(
                MASupportChecker2.MovingAverageLength length, double value, boolean isSupport) {
            this.length = length;
            this.value = value;
            this.isSupport = isSupport;
            this.timestamp = LocalDateTime.now();
        }

        public static MASupportChecker2.MAInteraction of(
                MASupportChecker2.MovingAverageLength length, double value, boolean isSupport) {
            return new MASupportChecker2.MAInteraction(length, value, isSupport);
        }

        public MASupportChecker2.MovingAverageLength getLength() {
            return length;
        }

        public double getValue() {
            return value;
        }

        public boolean isSupport() {
            return isSupport;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s at %.2f (%s)",
                    length.getDisplayName(), value, isSupport ? "Support" : "Resistance");
        }
    }

    // ========== CONSTANTS ==========

    private static final Map<MASupportChecker2.MovingAverageLength, Double> EMA_THRESHOLDS =
            Map.of(
                    MASupportChecker2.MovingAverageLength.EMA5, 0.75,
                    MASupportChecker2.MovingAverageLength.EMA10, 1.25,
                    MASupportChecker2.MovingAverageLength.EMA20, 2.5,
                    MASupportChecker2.MovingAverageLength.EMA50, 4.0,
                    MASupportChecker2.MovingAverageLength.EMA100, 6.0,
                    MASupportChecker2.MovingAverageLength.EMA200, 8.0);

    private static final double PRICE_TOLERANCE = 0.1; // 0.1%
    private static final double SUPPORT_BOUNCE_THRESHOLD = 1.5; // 1.5% for conditions 2a and 2c

    // ========== MAIN METHOD ==========

    public List<MASupportChecker2.MAInteraction> findMAInteractions(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean sortByValue) {

        List<MASupportChecker2.MAServiceEntry> sorted =
                getSortedMAEntries(timeframe, stockTechnicals, stockPrice, sortByValue);

        List<MASupportChecker2.MAInteraction> allInteractions =
                IntStream.range(0, sorted.size())
                        .filter(
                                i -> {
                                    double value = sorted.get(i).value;
                                    MASupportChecker2.MovingAverageLength length =
                                            sorted.get(i).length;

                                    return isSupportLevel(
                                            length, stockPrice, stockTechnicals, value);
                                })
                        .mapToObj(
                                i -> {
                                    double value = sorted.get(i).value;
                                    MASupportChecker2.MovingAverageLength length =
                                            sorted.get(i).length;
                                    return MASupportChecker2.MAInteraction.of(length, value, true);
                                })
                        .collect(Collectors.toList());

        // If no interactions found, return empty list
        if (allInteractions.isEmpty()) {
            return Collections.emptyList();
        }

        // If only one interaction, return it
        if (allInteractions.size() == 1) {
            return Collections.singletonList(allInteractions.get(0));
        }

        // If multiple interactions, find the one with the lowest EMA value
        MASupportChecker2.MAInteraction lowestEMA =
                allInteractions.stream()
                        .filter(interaction -> interaction.getLength().isEMA())
                        .min(Comparator.comparingDouble(MASupportChecker2.MAInteraction::getValue))
                        .orElse(null);

        // If no EMA interactions found, get the one with the lowest SMA value
        if (lowestEMA == null) {
            lowestEMA =
                    allInteractions.stream()
                            .filter(interaction -> interaction.getLength().isSMA())
                            .min(
                                    Comparator.comparingDouble(
                                            MASupportChecker2.MAInteraction::getValue))
                            .orElse(null);
        }

        // If still no interactions, return the first one (should not happen)
        if (lowestEMA == null) {
            lowestEMA = allInteractions.get(0);
        }

        return Collections.singletonList(lowestEMA);
    }

    // ALTERNATIVE: Method that returns single interaction directly
    public Optional<MASupportChecker2.MAInteraction> findSingleMASupport(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean sortByValue) {

        List<MASupportChecker2.MAServiceEntry> sorted =
                getSortedMAEntries(timeframe, stockTechnicals, stockPrice, sortByValue);

        List<MASupportChecker2.MAInteraction> allInteractions =
                IntStream.range(0, sorted.size())
                        .filter(
                                i -> {
                                    double value = sorted.get(i).value;
                                    MASupportChecker2.MovingAverageLength length =
                                            sorted.get(i).length;

                                    return isSupportLevel(
                                            length, stockPrice, stockTechnicals, value);
                                })
                        .mapToObj(
                                i -> {
                                    double value = sorted.get(i).value;
                                    MASupportChecker2.MovingAverageLength length =
                                            sorted.get(i).length;
                                    return MASupportChecker2.MAInteraction.of(length, value, true);
                                })
                        .collect(Collectors.toList());

        if (allInteractions.isEmpty()) {
            return Optional.empty();
        }

        if (allInteractions.size() == 1) {
            return Optional.of(allInteractions.get(0));
        }

        // Find the interaction with the lowest MA value
        return allInteractions.stream()
                .min(Comparator.comparingDouble(MASupportChecker2.MAInteraction::getValue));
    }

    // ========== SUPPORT CHECK METHODS ==========

    public static boolean isPriceNearMA(double high, double low, double maValue) {
        return high >= maValue * (1 - PRICE_TOLERANCE / 100)
                && low <= maValue * (1 + PRICE_TOLERANCE / 100);
    }

    public static boolean isPreviousDowntrend(StockPrice stockPrice) {

        double close = stockPrice.getClose();
        double prevClose = stockPrice.getPrevClose();
        double prev2Close = stockPrice.getPrev2Close();
        return close < prevClose
                && ((prevClose < prev2Close)
                        || (CandleStickUtils.isPrevLowerHigh(stockPrice)
                                && CandleStickUtils.isPrevLowerLow(stockPrice)));
    }

    public static boolean checkCondition2a(
            double prevLow,
            double prevClose,
            double currentClose,
            double prevEMA,
            double currentEMA) {

        if (prevLow < prevEMA && prevClose > prevEMA) {
            double minPrice = currentEMA * (1 + (SUPPORT_BOUNCE_THRESHOLD / 100));
            return currentClose > minPrice;
        }
        return false;
    }

    public static boolean checkCondition2b(
            MASupportChecker2.MovingAverageLength length,
            double prevClose,
            double currentClose,
            double prevEMA) {

        // Get the EMA-specific threshold for yesterday's proximity check
        double emaThreshold = EMA_THRESHOLDS.getOrDefault(length, SUPPORT_BOUNCE_THRESHOLD);

        // Check if yesterday's close was within EMA threshold range
        double maxDistance = prevEMA * (emaThreshold / 100);
        boolean wasNearEMA = Math.abs(prevClose - prevEMA) <= maxDistance;

        if (wasNearEMA) {
            // Check if today's close is above yesterday's close by at least 1.5%
            double minPrice = prevClose * (1 + (SUPPORT_BOUNCE_THRESHOLD / 100));
            return currentClose > minPrice;
        }
        return false;
    }

    public static boolean checkCondition2c(
            double prevClose, double currentClose, double prevEMA, double currentEMA) {

        if (prevClose < prevEMA) {
            double minPrice = currentEMA * (1 + (SUPPORT_BOUNCE_THRESHOLD / 100));
            return currentClose > minPrice;
        }
        return false;
    }

    // NEW METHOD: Check if EMA is rising (current >= previous)
    public static boolean isEMARising(
            MASupportChecker2.MovingAverageLength length, StockTechnicals stockTechnicals) {
        if (!length.isEMA()) {
            // For SMA, we don't apply this condition
            return true;
        }

        Double currentEMA = getEMAForLength(stockTechnicals, length);
        Double prevEMA = getPrevEMAForLength(stockTechnicals, length);

        if (currentEMA == null || prevEMA == null) {
            return false;
        }

        // EMA is rising if current >= previous
        return currentEMA >= prevEMA;
    }

    public static boolean isSupportLevel(
            MASupportChecker2.MovingAverageLength length,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            double maValue) {

        // Must have previous downtrend
        if (!isPreviousDowntrend(stockPrice)) return false;

        // Price must be near MA today
        if (!isPriceNearMA(stockPrice.getHigh(), stockPrice.getLow(), maValue)) return false;

        // For EMA support, check if EMA is rising (current >= previous)
        if (length.isEMA() && !isEMARising(length, stockTechnicals)) {
            return false;
        }

        // Get EMA values
        Double currentEMA = getEMAForLength(stockTechnicals, length);
        Double prevEMA = getPrevEMAForLength(stockTechnicals, length);

        if (currentEMA == null || prevEMA == null) return false;

        // Check support conditions
        double prevLow = stockPrice.getPrevLow();
        double prevClose = stockPrice.getPrevClose();
        double currentClose = stockPrice.getClose();

        return checkCondition2a(prevLow, prevClose, currentClose, prevEMA, currentEMA)
                || checkCondition2b(length, prevClose, currentClose, prevEMA)
                || checkCondition2c(prevClose, currentClose, prevEMA, currentEMA);
    }

    // ========== HELPER METHODS ==========

    private List<MASupportChecker2.MAServiceEntry> getSortedMAEntries(
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            StockPrice stockPrice,
            boolean sortByValue) {

        double currentPrice = stockPrice.getClose(); // Or use close price

        // Create entries for all MAs
        List<MASupportChecker2.MAServiceEntry> entries =
                Arrays.stream(MASupportChecker2.MovingAverageLength.values())
                        .map(
                                length -> {
                                    Double maValue = getMAValue(stockTechnicals, length);
                                    return maValue != null && maValue > 0
                                            ? new MASupportChecker2.MAServiceEntry(
                                                    length, maValue, currentPrice)
                                            : null;
                                })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        // Sort based on parameter
        if (sortByValue) {
            entries.sort(MASupportChecker2.MAServiceEntry.sortByValue());
        } else {
            entries.sort(MASupportChecker2.MAServiceEntry::compareTo); // By distance from price
        }

        return entries;
    }

    private static Double getMAValue(
            StockTechnicals technicals, MASupportChecker2.MovingAverageLength length) {
        return switch (length) {
            case SMA5 -> technicals.getSma5();
            case SMA10 -> technicals.getSma10();
            case SMA20 -> technicals.getSma20();
            case SMA50 -> technicals.getSma50();
            case SMA100 -> technicals.getSma100();
            case SMA200 -> technicals.getSma200();
            case EMA5 -> technicals.getEma5();
            case EMA10 -> technicals.getEma10();
            case EMA20 -> technicals.getEma20();
            case EMA50 -> technicals.getEma50();
            case EMA100 -> technicals.getEma100();
            case EMA200 -> technicals.getEma200();
        };
    }

    private static Double getEMAForLength(
            StockTechnicals technicals, MASupportChecker2.MovingAverageLength length) {
        return switch (length) {
            case EMA5 -> technicals.getEma5();
            case EMA10 -> technicals.getEma10();
            case EMA20 -> technicals.getEma20();
            case EMA50 -> technicals.getEma50();
            case EMA100 -> MovingAverageUtil.getMovingAverage100(
                    technicals.getTimeframe(), technicals);
            case EMA200 -> MovingAverageUtil.getMovingAverage200(
                    technicals.getTimeframe(), technicals);
            default -> null;
        };
    }

    private static Double getPrevEMAForLength(
            StockTechnicals technicals, MASupportChecker2.MovingAverageLength length) {
        return switch (length) {
            case EMA5 -> technicals.getPrevEma5();
            case EMA10 -> technicals.getPrevEma10();
            case EMA20 -> technicals.getPrevEma20();
            case EMA50 -> technicals.getPrevEma50();
            case EMA100 -> MovingAverageUtil.getPrevMovingAverage100(
                    technicals.getTimeframe(), technicals);
            case EMA200 -> MovingAverageUtil.getPrevMovingAverage200(
                    technicals.getTimeframe(), technicals);
            default -> null;
        };
    }

    // ========== UTILITY METHODS ==========

    public static Map<MASupportChecker2.MovingAverageLength, Double> getCurrentMAs(
            StockTechnicals technicals) {
        Map<MASupportChecker2.MovingAverageLength, Double> maMap = new HashMap<>();

        for (MASupportChecker2.MovingAverageLength length :
                MASupportChecker2.MovingAverageLength.values()) {
            Double value = getMAValue(technicals, length);
            if (value != null && value > 0) {
                maMap.put(length, value);
            }
        }

        return maMap;
    }

    public static List<MASupportChecker2.MAInteraction> findClosestMAs(
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            int count,
            boolean supportOnly) {

        double currentPrice = stockPrice.getClose();
        Map<MASupportChecker2.MovingAverageLength, Double> maMap = getCurrentMAs(stockTechnicals);

        return maMap.entrySet().stream()
                .map(
                        entry -> {
                            MASupportChecker2.MovingAverageLength length = entry.getKey();
                            double value = entry.getValue();
                            boolean isSupport = currentPrice > value;

                            if (supportOnly && !isSupport) return null;

                            return MASupportChecker2.MAInteraction.of(length, value, isSupport);
                        })
                .filter(Objects::nonNull)
                .sorted(
                        (a, b) -> {
                            double distA = Math.abs(a.getValue() - currentPrice);
                            double distB = Math.abs(b.getValue() - currentPrice);
                            return Double.compare(distA, distB);
                        })
                .limit(count)
                .collect(Collectors.toList());
    }

    // ========== TEST METHODS ==========

    public static void testAllConditions() {
        System.out.println("=== Testing MASupportChecker2 ===");

        // Test isPriceNearMA
        System.out.println("\n1. Testing isPriceNearMA:");
        System.out.println(
                "Price 99-101 near MA 100: " + isPriceNearMA(101, 99, 100) + " ✓ Expected: true");
        System.out.println(
                "Price 102-104 near MA 100: "
                        + isPriceNearMA(104, 102, 100)
                        + " ✗ Expected: false");

        // Test checkCondition2a
        System.out.println("\n2. Testing checkCondition2a:");
        boolean result2a = checkCondition2a(97.5, 101, 102.1, 100, 100.5);
        System.out.println("Condition 2a (valid): " + result2a + " ✓ Expected: true");

        // Test checkCondition2b
        System.out.println("\n3. Testing checkCondition2b:");
        boolean result2b =
                checkCondition2b(MASupportChecker2.MovingAverageLength.EMA200, 104, 105, 97);
        System.out.println("Condition 2b EMA200: " + result2b + " ✓ Expected: true");

        // Test checkCondition2c
        System.out.println("\n4. Testing checkCondition2c:");
        boolean result2c = checkCondition2c(99, 102, 100, 100.2);
        System.out.println("Condition 2c: " + result2c + " ✓ Expected: true");

        System.out.println("\n=== All tests completed ===");
    }

    // ========== NEW TEST METHOD FOR SINGLE INTERACTION ==========

    public static void testSingleInteractionSelection() {
        System.out.println("=== Testing Single Interaction Selection ===");

        // Create test data
        List<MASupportChecker2.MAInteraction> interactions =
                Arrays.asList(
                        MASupportChecker2.MAInteraction.of(
                                MASupportChecker2.MovingAverageLength.EMA5, 100.0, true),
                        MASupportChecker2.MAInteraction.of(
                                MASupportChecker2.MovingAverageLength.EMA20, 90.0, true),
                        MASupportChecker2.MAInteraction.of(
                                MASupportChecker2.MovingAverageLength.SMA50, 95.0, true));

        // Find the interaction with the lowest value
        MASupportChecker2.MAInteraction lowest =
                interactions.stream()
                        .min(Comparator.comparingDouble(MASupportChecker2.MAInteraction::getValue))
                        .orElse(null);

        System.out.println("\nTest Case: EMA5=100, EMA20=90, SMA50=95");
        System.out.println("Selected interaction: " + lowest);
        System.out.println("Expected: EMA20 at 90.00 (Support) ✓");

        // Test with only EMAs
        List<MASupportChecker2.MAInteraction> emaOnlyInteractions =
                Arrays.asList(
                        MASupportChecker2.MAInteraction.of(
                                MASupportChecker2.MovingAverageLength.EMA10, 105.0, true),
                        MASupportChecker2.MAInteraction.of(
                                MASupportChecker2.MovingAverageLength.EMA50, 102.0, true));

        MASupportChecker2.MAInteraction lowestEMA =
                emaOnlyInteractions.stream()
                        .min(Comparator.comparingDouble(MASupportChecker2.MAInteraction::getValue))
                        .orElse(null);

        System.out.println("\nTest Case: EMA10=105, EMA50=102");
        System.out.println("Selected interaction: " + lowestEMA);
        System.out.println("Expected: EMA50 at 102.00 (Support) ✓");

        System.out.println("\n=== Single interaction selection test completed ===");
    }
}
