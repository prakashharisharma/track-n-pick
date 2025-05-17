package com.example.service.utils;

import static com.example.service.MovingAverageLength.*;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.MovingAverageLength;
import com.example.service.MovingAverageResult;
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

    public static double getMovingAverage10(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getEma10();
    }

    public static double getPrevMovingAverage10(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getPrevEma10();
    }

    public static double getMovingAverage20(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getEma20();
    }

    public static double getPrevMovingAverage20(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getPrevEma20();
    }

    public static double getMovingAverage50(Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getEma50();
    }

    public static double getPrevMovingAverage50(
            Timeframe timeframe, StockTechnicals stockTechnicals) {
        return stockTechnicals.getPrevEma50();
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

            // Sort by current value
            /*
            entries = entries.stream()
                    .sorted(Comparator.comparingDouble(ma -> ma.value))
                    .toList();*/
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
                    case SHORTEST -> 0;
                    case SHORT -> 1;
                    case MEDIUM -> 2;
                    case LONG -> 3;
                    case LONGEST -> 4;
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
}
