package com.example.service.utils;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.MovingAverageLength;
import com.example.service.MovingAverageResult;
import com.example.service.enhanced.MovingAverageType;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MovingAverageUtil {

    public static double getMovingAverage5(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getEma5() != null ? stockTechnicals.getEma5() : 0.0;
    }

    public static double getPrevMovingAverage5(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getPrevEma5() != null ? stockTechnicals.getPrevEma5() : 0.0;
    }

    public static double getPrev2MovingAverage5(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getPrev2Ema5() != null ? stockTechnicals.getPrev2Ema5() : 0.0;
    }

    public static double getMovingAverage10(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getEma10();
    }

    public static double getPrevMovingAverage10(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getPrevEma10();
    }

    public static double getPrev2MovingAverage10(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getPrev2Ema10();
    }

    public static double getMovingAverage20(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getEma20() != null ? stockTechnicals.getEma20() : 0.0;
    }

    public static double getPrevMovingAverage20(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getPrevEma20() != null ? stockTechnicals.getPrevEma20() : 0.0;
    }

    public static double getPrev2MovingAverage20(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getPrev2Ema20() != null ? stockTechnicals.getPrev2Ema20() : 0.0;
    }

    public static double getMovingAverage50(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getEma50() != null ? stockTechnicals.getEma50() : 0.0;
    }

    public static double getPrevMovingAverage50(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getPrevEma50() != null ? stockTechnicals.getPrevEma50() : 0.0;
    }

    public static double getPrev2MovingAverage50(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getPrev2Ema50() != null ? stockTechnicals.getPrev2Ema50() : 0.0;
    }

    public static double getMovingAverage100(Timeframe timeframe, StockTechnicals stockTechnicals) {
        /*
        return (timeframe == Timeframe.MONTHLY)
                ? stockTechnicals.getSma100()
                : stockTechnicals.getEma100();*/
        return stockTechnicals.getSma100() != null ? stockTechnicals.getSma100() : 0.0;
    }

    public static double getPrevMovingAverage100(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        /*
        return (timeframe == Timeframe.MONTHLY)
                ? stockTechnicals.getPrevSma100()
                : stockTechnicals.getPrevEma100();*/
        return stockTechnicals.getPrevSma100() != null ? stockTechnicals.getPrevSma100() : 0.0;
    }

    public static double getPrev2MovingAverage100(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        /*
        return (timeframe == Timeframe.MONTHLY)
                ? stockTechnicals.getPrevSma100()
                : stockTechnicals.getPrevEma100();*/
        return stockTechnicals.getPrev2Sma100() != null ? stockTechnicals.getPrev2Sma100() : 0.0;
    }

    public static double getMovingAverage200(Timeframe timeframe, StockTechnicals stockTechnicals) {
        /*
        return (timeframe == Timeframe.MONTHLY || timeframe == Timeframe.WEEKLY)
                ? stockTechnicals.getSma200()
                : stockTechnicals.getEma200();
        */
        return stockTechnicals.getSma200() != null ? stockTechnicals.getSma200() : 0.0;
    }

    public static double getPrevMovingAverage200(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        /*
        return (timeframe == Timeframe.MONTHLY || timeframe == Timeframe.WEEKLY)
                ? stockTechnicals.getPrevSma200()
                : stockTechnicals.getPrevEma200();
         */
        return stockTechnicals.getPrevSma200() != null ? stockTechnicals.getPrevSma200() : 0.0;
    }

    public static double getPrev2MovingAverage200(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        /*
        return (timeframe == Timeframe.MONTHLY || timeframe == Timeframe.WEEKLY)
                ? stockTechnicals.getPrevSma200()
                : stockTechnicals.getPrevEma200();
         */
        return stockTechnicals.getPrev2Sma200() != null ? stockTechnicals.getPrev2Sma200() : 0.0;
    }

    public static MovingAverageResult getMovingAverage(
            MovingAverageLength length,
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            boolean sortByValue) {
        List<MAEntry> entries =
                List.of(
                        new MAEntry(
                                5,
                                getMovingAverage5(timeframe, stockTechnicals),
                                getPrevMovingAverage5(timeframe, stockTechnicals)),
                        new MAEntry(
                                20,
                                getMovingAverage20(timeframe, stockTechnicals),
                                getPrevMovingAverage20(timeframe, stockTechnicals)),
                        new MAEntry(
                                50,
                                getMovingAverage50(timeframe, stockTechnicals),
                                getPrevMovingAverage50(timeframe, stockTechnicals)),
                        new MAEntry(
                                100,
                                getMovingAverage100(timeframe, stockTechnicals),
                                getPrevMovingAverage100(timeframe, stockTechnicals)),
                        new MAEntry(
                                200,
                                getMovingAverage200(timeframe, stockTechnicals),
                                getPrevMovingAverage200(timeframe, stockTechnicals)));

        if (sortByValue) {

            // Sort by value descending (highest MA first)

            entries =
                    entries.stream()
                            .sorted(Comparator.comparingDouble((MAEntry ma) -> ma.value).reversed())
                            .toList();
        } else {
            // Sort by fixed period (to ensure consistent mapping)
            entries =
                    entries.stream()
                            .sorted(Comparator.comparingInt(ma -> ma.period))
                            .toList(); // ensures [5, 20, 50, 100, 200] order
        }

        int index =
                switch (length) {
                    case HIGHEST -> 0;
                    case HIGH -> 1;
                    case MEDIUM -> 2;
                    case LOW -> 3;
                    case LOWEST -> 4;
                };

        MAEntry selected = entries.get(index);

        return new MovingAverageResult(selected.value, selected.prevValue);
    }

    private static class MAEntry {
        int period;
        double value;
        double prevValue;

        MAEntry(int period, double value, double prevValue) {
            this.period = period;
            this.value = value;
            this.prevValue = prevValue;
        }
    }

    public static boolean isIncreasing(MovingAverageResult result) {
        if (result == null || result.getValue() == null || result.getPrevValue() == null) {
            return false;
        }
        return result.getValue() > result.getPrevValue();
    }

    public static boolean isDecreasing(MovingAverageResult result) {
        if (result == null || result.getValue() == null || result.getPrevValue() == null) {
            return false;
        }
        return result.getValue() < result.getPrevValue();
    }

    public static double getMovingAverage(
            MovingAverageType type, Timeframe tf, StockTechnicals st) {
        return switch (type) {
            case MA5 -> getMovingAverage5(tf, st);
            case MA20 -> getMovingAverage20(tf, st);
            case MA50 -> getMovingAverage50(tf, st);
            case MA100 -> getMovingAverage100(tf, st);
            case MA200 -> getMovingAverage200(tf, st);
        };
    }

    public static double getPrevMovingAverage(
            MovingAverageType type, Timeframe tf, StockTechnicals st) {
        return switch (type) {
            case MA5 -> getPrevMovingAverage5(tf, st);
            case MA20 -> getPrevMovingAverage20(tf, st);
            case MA50 -> getPrevMovingAverage50(tf, st);
            case MA100 -> getPrevMovingAverage100(tf, st);
            case MA200 -> getPrevMovingAverage200(tf, st);
        };
    }

    public static boolean isAllMaAlignedBullish(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        double ma5 = getMovingAverage5(timeframe, stockTechnicals);
        double ma20 = getMovingAverage20(timeframe, stockTechnicals);
        double ma50 = getMovingAverage50(timeframe, stockTechnicals);
        double ma100 = getMovingAverage100(timeframe, stockTechnicals);
        double ma200 = getMovingAverage200(timeframe, stockTechnicals);

        if (ma200 > 0) {
            return (ma20 > ma50 && ma50 > ma200)
                    || (ma20 > ma50 && ma50 > ma100)
                    || (ma50 > ma100 && ma100 > ma200);
        } else if (ma100 > 0) {
            return (ma20 > ma50 && ma50 > ma100) || (ma20 > ma50);
        } else if (ma50 > 0) {
            return (ma20 > ma50) || (ma5 > ma20);
        } else if (ma20 > 0) {
            return ma5 > ma20;
        }
        return false;
    }

    public static boolean isAllMaAlignedBearish(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        double ma5 = getMovingAverage5(timeframe, stockTechnicals);
        double ma20 = getMovingAverage20(timeframe, stockTechnicals);
        double ma50 = getMovingAverage50(timeframe, stockTechnicals);
        double ma100 = getMovingAverage100(timeframe, stockTechnicals);
        double ma200 = getMovingAverage200(timeframe, stockTechnicals);

        if (ma200 > 0) {
            return (ma20 < ma50 && ma50 < ma200) || (ma20 < ma50 && ma50 < ma100);
        } else if (ma100 > 0) {
            return (ma20 < ma50 && ma50 < ma100) || (ma20 < ma50);
        } else if (ma50 > 0) {
            return (ma20 < ma50) || (ma5 < ma20);
        } else if (ma20 > 0) {
            return ma5 < ma20;
        }
        return false;
    }

    public static boolean isDifferentialMaAlignedBullish(
            Timeframe timeframe, StockTechnicals stockTechnicals) {

        double ma5 = getMovingAverage5(timeframe, stockTechnicals);
        double ma20 = getMovingAverage20(timeframe, stockTechnicals);
        double ma50 = getMovingAverage50(timeframe, stockTechnicals);
        double ma100 = getMovingAverage100(timeframe, stockTechnicals);
        double ma200 = getMovingAverage200(timeframe, stockTechnicals);

        if (ma200 > 0) {
            return ma20 > ma100 && ma50 > ma200;
        }

        if (ma100 > 0) {
            return ma5 > ma50 && ma20 > ma100;
        }

        if (ma50 > 0) {
            return ma5 > ma20 && ma20 > ma50;
        }

        return false;
    }

    public static boolean isLongerMaAlignedBearish(
            MovingAverageLength movingAverageLength,
            Timeframe timeframe,
            StockTechnicals stockTechnicals) {

        // Start with MA50 as base
        int baseMaDays = 50;

        List<MovingAverageLength> longerMAs =
                Arrays.stream(MovingAverageLength.values())
                        .filter(ma -> ma.getMaDays() >= movingAverageLength.getMaDays())
                        .sorted(Comparator.comparingInt(MovingAverageLength::getMaDays))
                        .toList();

        if (longerMAs.size() < 2) return false; // Need at least 2 to compare

        for (int i = 1; i < longerMAs.size(); i++) {
            double prev =
                    MovingAverageUtil.getMovingAverage(
                                    longerMAs.get(i - 1), timeframe, stockTechnicals, false)
                            .getValue();
            double curr =
                    MovingAverageUtil.getMovingAverage(
                                    longerMAs.get(i), timeframe, stockTechnicals, false)
                            .getValue();

            if (prev >= curr) {
                return false; // Not strictly decreasing
            }
        }

        return true;
    }

    public static boolean isAllMAsIncreasing(StockTechnicals stockTechnicals) {
        Timeframe timeframe = stockTechnicals.getTimeframe();
        int count = 0;

        if (getMovingAverage5(timeframe, stockTechnicals)
                >= getPrevMovingAverage5(timeframe, stockTechnicals)) count++;
        if (getMovingAverage20(timeframe, stockTechnicals)
                >= getPrevMovingAverage20(timeframe, stockTechnicals)) count++;
        if (getMovingAverage50(timeframe, stockTechnicals)
                >= getPrevMovingAverage50(timeframe, stockTechnicals)) count++;
        if (getMovingAverage100(timeframe, stockTechnicals)
                >= getPrevMovingAverage100(timeframe, stockTechnicals)) count++;
        if (getMovingAverage200(timeframe, stockTechnicals)
                >= getPrevMovingAverage200(timeframe, stockTechnicals)) count++;

        return count >= 5;
    }

    public static int increasingMaCount(StockTechnicals stockTechnicals) {
        if (stockTechnicals == null) {
            return 0;
        }
        Timeframe timeframe = stockTechnicals.getTimeframe();
        int count = 0;

        if (getMovingAverage5(timeframe, stockTechnicals)
                >= getPrevMovingAverage5(timeframe, stockTechnicals)) count++;
        if (getMovingAverage20(timeframe, stockTechnicals)
                >= getPrevMovingAverage20(timeframe, stockTechnicals)) count++;
        if (getMovingAverage50(timeframe, stockTechnicals)
                >= getPrevMovingAverage50(timeframe, stockTechnicals)) count++;
        if (getMovingAverage100(timeframe, stockTechnicals)
                >= getPrevMovingAverage100(timeframe, stockTechnicals)) count++;
        if (getMovingAverage200(timeframe, stockTechnicals)
                >= getPrevMovingAverage200(timeframe, stockTechnicals)) count++;

        return count;
    }

    public static int decreasingMaCount(StockTechnicals stockTechnicals) {
        Timeframe timeframe = stockTechnicals.getTimeframe();
        int count = 0;

        if (getMovingAverage5(timeframe, stockTechnicals)
                <= getPrevMovingAverage5(timeframe, stockTechnicals)) count++;
        if (getMovingAverage20(timeframe, stockTechnicals)
                <= getPrevMovingAverage20(timeframe, stockTechnicals)) count++;
        if (getMovingAverage50(timeframe, stockTechnicals)
                <= getPrevMovingAverage50(timeframe, stockTechnicals)) count++;
        if (getMovingAverage100(timeframe, stockTechnicals)
                <= getPrevMovingAverage100(timeframe, stockTechnicals)) count++;
        if (getMovingAverage200(timeframe, stockTechnicals)
                <= getPrevMovingAverage200(timeframe, stockTechnicals)) count++;

        return count;
    }

    public static boolean validatedMa200WrtMa100(StockTechnicals stockTechnicals) {
        Timeframe timeframe = stockTechnicals.getTimeframe();
        if (getMovingAverage200(timeframe, stockTechnicals)
                > getPrevMovingAverage200(timeframe, stockTechnicals)) {
            if (getMovingAverage100(timeframe, stockTechnicals)
                    > getPrevMovingAverage100(timeframe, stockTechnicals)) {
                return true;
            }
        }

        if (getMovingAverage100(timeframe, stockTechnicals)
                < getPrevMovingAverage100(timeframe, stockTechnicals)) {
            if (getMovingAverage200(timeframe, stockTechnicals)
                    < getPrevMovingAverage200(timeframe, stockTechnicals)) {
                return true;
            }
        }

        if (getMovingAverage100(timeframe, stockTechnicals)
                > getPrevMovingAverage100(timeframe, stockTechnicals)) {
            if (getMovingAverage200(timeframe, stockTechnicals)
                    < getPrevMovingAverage200(timeframe, stockTechnicals)) {
                return true;
            }
        }
        return false;
    }

    public static boolean validatedMa5MA20AndMa50(StockTechnicals stockTechnicals) {
        Timeframe timeframe = stockTechnicals.getTimeframe();
        if (getMovingAverage5(timeframe, stockTechnicals)
                > getPrevMovingAverage5(timeframe, stockTechnicals)) {
            if (getMovingAverage20(timeframe, stockTechnicals)
                    > getPrevMovingAverage20(timeframe, stockTechnicals)) {
                if (getMovingAverage50(timeframe, stockTechnicals)
                        > getPrevMovingAverage50(timeframe, stockTechnicals)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isAllMAsDecreasing(StockTechnicals stockTechnicals) {
        Timeframe timeframe = stockTechnicals.getTimeframe();
        int count = 0;

        if (getMovingAverage5(timeframe, stockTechnicals)
                < getPrevMovingAverage5(timeframe, stockTechnicals)) count++;
        if (getMovingAverage20(timeframe, stockTechnicals)
                < getPrevMovingAverage20(timeframe, stockTechnicals)) count++;
        if (getMovingAverage50(timeframe, stockTechnicals)
                < getPrevMovingAverage50(timeframe, stockTechnicals)) count++;
        if (getMovingAverage100(timeframe, stockTechnicals)
                < getPrevMovingAverage100(timeframe, stockTechnicals)) count++;
        if (getMovingAverage200(timeframe, stockTechnicals)
                < getPrevMovingAverage200(timeframe, stockTechnicals)) count++;

        return count >= 5;
    }

    public static boolean isLowerMovingAverageIncreasing(
            MovingAverageLength currentLength,
            StockTechnicals stockTechnicals,
            boolean sortByValue) {

        List<MovingAverageLength> lowerLengths =
                Arrays.stream(MovingAverageLength.values())
                        .filter(
                                length -> {
                                    if (sortByValue) {
                                        return length.getWeight() > currentLength.getWeight();
                                    } else {
                                        return length.getMaDays() > currentLength.getMaDays();
                                    }
                                })
                        .toList();

        int increasingCount =
                countIncreasingMovingAverages(lowerLengths, stockTechnicals, sortByValue);
        int requiredCount = (lowerLengths.size() + 1) / 2;

        return increasingCount >= requiredCount;
    }

    public static boolean isHigherMovingAverageDecreasing(
            MovingAverageLength currentLength,
            StockTechnicals stockTechnicals,
            boolean sortByValue) {

        List<MovingAverageLength> higherLengths =
                Arrays.stream(MovingAverageLength.values())
                        .filter(
                                length -> {
                                    if (sortByValue) {
                                        return length.getWeight() < currentLength.getWeight();
                                    } else {
                                        return length.getMaDays() < currentLength.getMaDays();
                                    }
                                })
                        .toList();

        int decreasingCount =
                countDecreasingMovingAverages(higherLengths, stockTechnicals, sortByValue);
        int requiredCount = (higherLengths.size() + 1) / 2;

        return decreasingCount >= requiredCount;
    }

    public static int countIncreasingMovingAverages(
            List<MovingAverageLength> lengths,
            StockTechnicals stockTechnicals,
            boolean sortByValue) {
        int count = 0;

        for (MovingAverageLength length : lengths) {
            MovingAverageResult movingAverageResult =
                    MovingAverageUtil.getMovingAverage(
                            length, stockTechnicals.getTimeframe(), stockTechnicals, sortByValue);

            if (movingAverageResult.getValue() > movingAverageResult.getPrevValue()) {
                count++;
            }
        }

        return count;
    }

    public static int countDecreasingMovingAverages(
            List<MovingAverageLength> lengths,
            StockTechnicals stockTechnicals,
            boolean sortByValue) {
        int count = 0;

        for (MovingAverageLength length : lengths) {
            MovingAverageResult movingAverageResult =
                    MovingAverageUtil.getMovingAverage(
                            length, stockTechnicals.getTimeframe(), stockTechnicals, sortByValue);

            if (movingAverageResult.getValue() > movingAverageResult.getPrevValue()) {
                count++;
            }
        }

        return count;
    }

    public static boolean isAtLeastTwoMovingAverageIncreasing(
            MovingAverageLength currentLength,
            StockTechnicals stockTechnicals,
            boolean sortByValue) {

        // If already at the lowest, no lower MAs to check
        if (currentLength == MovingAverageLength.LOWEST) {

            // Get all lower MA lengths
            List<MovingAverageLength> higherLengths =
                    Arrays.stream(MovingAverageLength.values())
                            .filter(length -> length.getWeight() < currentLength.getWeight())
                            .toList();

            int increasingCount =
                    countIncreasingMovingAverages(higherLengths, stockTechnicals, sortByValue);
            int requiredCount = (higherLengths.size() + 1) / 2;

            return increasingCount >= requiredCount;
        }

        // Get all lower MA lengths
        List<MovingAverageLength> lowerLengths =
                Arrays.stream(MovingAverageLength.values())
                        .filter(length -> length.getWeight() > currentLength.getWeight())
                        .toList();

        int increasingCount =
                countIncreasingMovingAverages(lowerLengths, stockTechnicals, sortByValue);
        int requiredCount = (lowerLengths.size() + 1) / 2;

        return increasingCount >= requiredCount;
    }

    public static boolean isAtLeastTwoMovingAverageDecreasing(
            MovingAverageLength currentLength, StockTechnicals stockTechnicals) {

        // If already at the highest, no higher MAs to check
        if (currentLength == MovingAverageLength.HIGHEST) {

            // Get all lower MA lengths
            List<MovingAverageLength> lowerLengths =
                    Arrays.stream(MovingAverageLength.values())
                            .filter(length -> length.ordinal() > currentLength.ordinal())
                            .toList();

            for (MovingAverageLength lowerLength : lowerLengths) {
                MovingAverageResult movingAverageResult =
                        getMovingAverage(
                                lowerLength, stockTechnicals.getTimeframe(), stockTechnicals, true);
                double currentMA = movingAverageResult.getValue();
                double previousMA = movingAverageResult.getPrevValue();

                if (currentMA < previousMA) {
                    return true;
                }
            }

            return false;
        }

        // Get all higher MA lengths
        List<MovingAverageLength> higherLengths =
                Arrays.stream(MovingAverageLength.values())
                        .filter(length -> length.ordinal() < currentLength.ordinal())
                        .toList();

        for (MovingAverageLength higherLength : higherLengths) {
            MovingAverageResult movingAverageResult =
                    getMovingAverage(
                            higherLength, stockTechnicals.getTimeframe(), stockTechnicals, true);
            double currentMA = movingAverageResult.getValue();
            double previousMA = movingAverageResult.getPrevValue();

            if (currentMA < previousMA) {
                return true;
            }
        }

        return false;
    }
}
