package com.example.service.utils;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.MovingAverageLength;
import com.example.service.MovingAverageResult;
import com.example.service.enhanced.MovingAverageType;
import java.util.Comparator;
import java.util.List;

public class MovingAverageUtil {

    public static double getMovingAverage5(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getEma5();
    }

    public static double getPrevMovingAverage5(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getPrevEma5();
    }

    public static double getPrev2MovingAverage5(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getPrev2Ema5();
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
        return stockTechnicals.getEma20();
    }

    public static double getPrevMovingAverage20(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getPrevEma20();
    }

    public static double getPrev2MovingAverage20(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getPrev2Ema20();
    }

    public static double getMovingAverage50(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getEma50();
    }

    public static double getPrevMovingAverage50(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getPrevEma50();
    }

    public static double getPrev2MovingAverage50(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getPrev2Ema50();
    }

    public static double getMovingAverage100(Timeframe timeframe, StockTechnicals stockTechnicals) {
        /*
        return (timeframe == Timeframe.MONTHLY)
                ? stockTechnicals.getSma100()
                : stockTechnicals.getEma100();*/
        return stockTechnicals.getSma100();
    }

    public static double getPrevMovingAverage100(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        /*
        return (timeframe == Timeframe.MONTHLY)
                ? stockTechnicals.getPrevSma100()
                : stockTechnicals.getPrevEma100();*/
        return stockTechnicals.getPrevSma100();
    }

    public static double getPrev2MovingAverage100(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        /*
        return (timeframe == Timeframe.MONTHLY)
                ? stockTechnicals.getPrevSma100()
                : stockTechnicals.getPrevEma100();*/
        return stockTechnicals.getPrev2Sma100();
    }

    public static double getMovingAverage200(Timeframe timeframe, StockTechnicals stockTechnicals) {
        /*
        return (timeframe == Timeframe.MONTHLY || timeframe == Timeframe.WEEKLY)
                ? stockTechnicals.getSma200()
                : stockTechnicals.getEma200();
        */
        return stockTechnicals.getSma200();
    }

    public static double getPrevMovingAverage200(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        /*
        return (timeframe == Timeframe.MONTHLY || timeframe == Timeframe.WEEKLY)
                ? stockTechnicals.getPrevSma200()
                : stockTechnicals.getPrevEma200();
         */
        return stockTechnicals.getPrevSma200();
    }

    public static double getPrev2MovingAverage200(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        /*
        return (timeframe == Timeframe.MONTHLY || timeframe == Timeframe.WEEKLY)
                ? stockTechnicals.getPrevSma200()
                : stockTechnicals.getPrevEma200();
         */
        return stockTechnicals.getPrev2Sma200();
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

        /*
        System.out.printf("Selected MA-%d with value=%.2f, prev=%.2f based on %s sorting%n",
                selected.period, selected.value, selected.prevValue, sortByValue ? "value" : "period");
        */

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
}
