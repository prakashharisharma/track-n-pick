package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.EvaluationLog;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.EvaluationLogService;
import com.example.service.VolumeIndicatorService;
import com.example.util.StringUtils;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class VolumeIndicatorServiceImpl implements VolumeIndicatorService {

    private static double BULLISH_MULTIPLIER_DAILY = 2.0;
    private static double BEARISH_MULTIPLIER_DAILY = 1.5;
    private static double THRESHOLD_DAILY = 0.5;

    private static double BULLISH_MULTIPLIER_WEEKLY = 2.0;
    private static double BEARISH_MULTIPLIER_WEEKLY = 1.5;
    private static double THRESHOLD_WEEKLY = 0.5;

    private static double BULLISH_MULTIPLIER_MONTHLY = 2.0;
    private static double BEARISH_MULTIPLIER_MONTHLY = 1.5;
    private static double THRESHOLD_MONTHLY = 0.5;

    private static double MIN_TRADING_VALUE = 7_00_00_000.0; // 5CR Avg per day

    private static long MIN_TRADING_VOLUME = 3_50_000; // 5 L avg Volume

    private final EvaluationLogService evaluationLogService;

    private static Map<Timeframe, Double> volumeMultipleFactor = new HashMap<>();

    static {
        volumeMultipleFactor.put(Timeframe.DAILY, 1.5);
        volumeMultipleFactor.put(Timeframe.WEEKLY, 1.25);
        volumeMultipleFactor.put(Timeframe.MONTHLY, 1.0);
    }

    @Override
    public boolean isBullish(
            StockPrice stockPrice, StockTechnicals stockTechnicals, Timeframe timeframe) {

        return this.isCurrentSessionHigh(
                        stockPrice,
                        stockTechnicals,
                        timeframe,
                        this.getBullishMultiplier(timeframe))
                || this.isPreviousSessionHigh(
                        stockPrice,
                        stockTechnicals,
                        timeframe,
                        this.getBullishMultiplier(timeframe));
    }

    @Override
    public boolean isBullish(
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            Timeframe timeframe,
            double multiplier) {

        return this.isCurrentSessionHigh(stockPrice, stockTechnicals, timeframe, multiplier)
                || this.isPreviousSessionHigh(stockPrice, stockTechnicals, timeframe, multiplier);
    }

    @Override
    public boolean isBearish(
            StockPrice stockPrice, StockTechnicals stockTechnicals, Timeframe timeframe) {

        return this.isCurrentSessionHigh(
                        stockPrice,
                        stockTechnicals,
                        timeframe,
                        this.getBearishMultiplier(timeframe))
                || this.isPreviousSessionHigh(
                        stockPrice,
                        stockTechnicals,
                        timeframe,
                        this.getBearishMultiplier(timeframe));
    }

    @Override
    public boolean isBearish(
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            Timeframe timeframe,
            double multiplier) {

        return this.isCurrentSessionHigh(stockPrice, stockTechnicals, timeframe, multiplier)
                || this.isPreviousSessionHigh(stockPrice, stockTechnicals, timeframe, multiplier);
    }

    @Override
    public boolean isBullish(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals, int days) {

        long volume = stockTechnicals.getVolume();
        long preVolume = stockTechnicals.getPrevVolume();
        long prev2Volume = stockTechnicals.getPrev2Volume();

        long volumeMA, prevVolumeMA, prev2VolumeMA;

        // Select the correct moving average based on the timeframe
        switch (timeframe) {
            case WEEKLY:
                volumeMA = stockTechnicals.getVolumeAvg10();
                prevVolumeMA = stockTechnicals.getPrevVolumeAvg10();
                prev2VolumeMA = stockTechnicals.getPrev2VolumeAvg10();
                break;
            case MONTHLY:
                volumeMA = stockTechnicals.getVolumeAvg5();
                prevVolumeMA = stockTechnicals.getPrevVolumeAvg5();
                prev2VolumeMA = stockTechnicals.getPrev2VolumeAvg5();
                break;
            default: // DAILY
                volumeMA = stockTechnicals.getVolumeAvg20();
                prevVolumeMA = stockTechnicals.getPrevVolumeAvg20();
                prev2VolumeMA = stockTechnicals.getPrev2VolumeAvg20();
        }

        if (days == 3) {
            return this.checkThreeSession(
                    stockPrice.getClose(),
                    volume,
                    volumeMA,
                    preVolume,
                    prevVolumeMA,
                    prev2Volume,
                    prev2VolumeMA,
                    this.getBullishMultiplier(timeframe));
        }
        if (days == 2) {
            return this.checkTwoSession(
                    stockPrice.getClose(),
                    volume,
                    volumeMA,
                    preVolume,
                    prevVolumeMA,
                    this.getBullishMultiplier(timeframe));
        }

        return this.checkSingleSession(
                stockPrice.getClose(),
                volume,
                volumeMA,
                preVolume,
                prevVolumeMA,
                this.getBullishMultiplier(timeframe));
    }

    @Override
    public boolean isBearish(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals, int days) {

        long volume = stockTechnicals.getVolume();
        long preVolume = stockTechnicals.getPrevVolume();
        long prev2Volume = stockTechnicals.getPrev2Volume();

        long volumeMA, prevVolumeMA, prev2VolumeMA;

        // Select the correct moving average based on the timeframe
        switch (timeframe) {
            case WEEKLY:
                volumeMA = stockTechnicals.getVolumeAvg10();
                prevVolumeMA = stockTechnicals.getPrevVolumeAvg10();
                prev2VolumeMA = stockTechnicals.getPrev2VolumeAvg10();
                break;
            case MONTHLY:
                volumeMA = stockTechnicals.getVolumeAvg5();
                prevVolumeMA = stockTechnicals.getPrevVolumeAvg5();
                prev2VolumeMA = stockTechnicals.getPrev2VolumeAvg5();
                break;
            default: // DAILY
                volumeMA = stockTechnicals.getVolumeAvg20();
                prevVolumeMA = stockTechnicals.getPrevVolumeAvg20();
                prev2VolumeMA = stockTechnicals.getPrev2VolumeAvg20();
        }

        if (days == 3) {
            return this.checkThreeSession(
                    stockPrice.getClose(),
                    volume,
                    volumeMA,
                    preVolume,
                    prevVolumeMA,
                    prev2Volume,
                    prev2VolumeMA,
                    this.getBearishMultiplier(timeframe));
        }
        if (days == 2) {
            return this.checkTwoSession(
                    stockPrice.getClose(),
                    volume,
                    volumeMA,
                    preVolume,
                    prevVolumeMA,
                    this.getBearishMultiplier(timeframe));
        }

        return this.checkSingleSession(
                stockPrice.getClose(),
                volume,
                volumeMA,
                preVolume,
                prevVolumeMA,
                this.getBearishMultiplier(timeframe));
    }

    private boolean isCurrentSessionHigh(
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            Timeframe timeframe,
            double multiplier) {

        long avg = stockTechnicals.getVolumeAvg20();
        long prevAvg = stockTechnicals.getPrevVolumeAvg20();

        // Adjust volume averages based on the timeframe
        if (timeframe == Timeframe.WEEKLY) {
            avg = stockTechnicals.getVolumeAvg10();
            prevAvg = stockTechnicals.getPrevVolumeAvg10();
        } else if (timeframe == Timeframe.MONTHLY) {
            avg = stockTechnicals.getVolumeAvg5();
            prevAvg = stockTechnicals.getPrevVolumeAvg5();
        }

        // Check if current volume is significantly higher than the selected average
        if (this.isHighVolume(stockTechnicals.getVolume(), avg, multiplier)) {
            if (this.isIncreasing(stockTechnicals.getVolume(), stockTechnicals.getPrevVolume())) {
                if (this.isIncreasing(avg, prevAvg)) {
                    return Boolean.TRUE;
                }
            }
        }
        // Alternative condition: Volume is higher than both avg (1.0x) and prev volume (multiplier)
        else if (this.isHighVolume(
                        stockTechnicals.getVolume(), avg, multiplier - this.getThreshold(timeframe))
                && this.isHighVolume(
                        stockTechnicals.getVolume(), stockTechnicals.getPrevVolume(), multiplier)) {
            if (this.isIncreasing(stockTechnicals.getVolume(), stockTechnicals.getPrevVolume())) {
                if (this.isIncreasing(avg, prevAvg)) {
                    return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE;
    }

    private boolean isPreviousSessionHigh(
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            Timeframe timeframe,
            double multiplier) {

        long avg = stockTechnicals.getPrevVolumeAvg20();
        long prevAvg = stockTechnicals.getPrev2VolumeAvg20();

        // Adjust volume averages based on the timeframe
        if (timeframe == Timeframe.WEEKLY) {
            avg = stockTechnicals.getPrevVolumeAvg10();
            prevAvg = stockTechnicals.getPrev2VolumeAvg10();
        } else if (timeframe == Timeframe.MONTHLY) {
            avg = stockTechnicals.getPrevVolumeAvg5();
            prevAvg = stockTechnicals.getPrev2VolumeAvg5();
        }

        // Check if current volume is significantly higher than the selected average
        if (this.isHighVolume(stockTechnicals.getPrevVolume(), avg, multiplier)) {
            if (this.isIncreasing(
                    stockTechnicals.getPrevVolume(), stockTechnicals.getPrev2Volume())) {
                if (this.isIncreasing(avg, prevAvg)) {
                    return Boolean.TRUE;
                }
            }
        }
        // Alternative condition: Volume is higher than both avg (1.0x) and prev volume (multiplier)
        else if (this.isHighVolume(
                        stockTechnicals.getPrevVolume(),
                        avg,
                        multiplier - this.getThreshold(timeframe))
                && this.isHighVolume(
                        stockTechnicals.getPrevVolume(),
                        stockTechnicals.getPrev2Volume(),
                        multiplier)) {
            if (this.isIncreasing(
                    stockTechnicals.getPrevVolume(), stockTechnicals.getPrev2Volume())) {
                if (this.isIncreasing(avg, prevAvg)) {
                    return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE;
    }

    public boolean checkSingleSession(
            double close,
            long volume,
            long volumeMA,
            long prevVolume,
            long prevVolumeMA,
            double threshold) {

        return this.isHighVolume(volume, volumeMA, threshold)
                || this.isHighVolume(prevVolume, prevVolumeMA, threshold)
                || this.isHighRVOL(volume, volumeMA, threshold)
                || this.isHighRVOL(prevVolume, prevVolumeMA, threshold);
    }

    public boolean checkTwoSession(
            double close,
            long volume,
            long volumeMA,
            long prevVolume,
            long prevVolumeMA,
            double threshold) {

        double avgVolume = (volume + prevVolume) / 2.0;
        double avgVolumeMA = (volumeMA + prevVolumeMA) / 2.0;

        // Condition 1: Average volume is greater than threshold × average volume MA
        boolean condition1 = avgVolume > threshold * avgVolumeMA;

        // Condition 2: Increasing volume pattern (prev volume above prev MA, current volume above
        // current MA, and increasing)
        boolean condition2 = volume > prevVolume && volume > volumeMA;

        // Condition 3: Combined RVOL check (Relative volume ratio > threshold)
        boolean condition3 = avgVolumeMA > 0 && (avgVolume / avgVolumeMA) > threshold;

        return condition1 || condition2 || condition3;
    }

    public boolean checkThreeSession(
            double close,
            long volume,
            long volumeMA,
            long prevVolume,
            long prevVolumeMA,
            long prev2Volume,
            long prev2VolumeMA,
            double threshold) {

        double avgVolume = (volume + prevVolume + prev2Volume) / 3.0;
        double avgVolumeMA = (volumeMA + prevVolumeMA + prev2VolumeMA) / 3.0;

        // Condition 1: Average volume over 3 sessions is greater than threshold × average volume MA
        boolean condition1 = avgVolume > threshold * avgVolumeMA;

        // Condition 2: Increasing volume pattern (each volume is above its MA and increasing)
        boolean condition2 = prevVolume > prev2Volume && prevVolume > volume && volume > volumeMA;

        // Condition 3: Combined RVOL check (Relative volume ratio > threshold)
        boolean condition3 = avgVolumeMA > 0 && (avgVolume / avgVolumeMA) > threshold;

        return condition1 || condition2 || condition3;
    }

    private boolean isHighVolume(long volume, long average, double multiplier) {
        return ((average > 0) && volume >= (average * multiplier)) ? Boolean.TRUE : Boolean.FALSE;
    }

    public boolean isHighRVOL(long volume, long volumeMA, double threshold) {
        if (volumeMA == 0) {
            return false; // or handle as appropriate (e.g., return true, throw exception, etc.)
        }
        return ((double) volume / volumeMA) > threshold;
    }

    private boolean isIncreasing(long volume, long preVolume) {
        return (volume > preVolume) ? Boolean.TRUE : Boolean.FALSE;
    }

    private double getBullishMultiplier(Timeframe timeframe) {

        if (timeframe == Timeframe.MONTHLY) {
            return BULLISH_MULTIPLIER_MONTHLY;
        } else if (timeframe == Timeframe.WEEKLY) {
            return BULLISH_MULTIPLIER_WEEKLY;
        }

        return BULLISH_MULTIPLIER_DAILY;
    }

    private double getBearishMultiplier(Timeframe timeframe) {

        if (timeframe == Timeframe.MONTHLY) {
            return BEARISH_MULTIPLIER_MONTHLY;
        } else if (timeframe == Timeframe.WEEKLY) {
            return BEARISH_MULTIPLIER_WEEKLY;
        }
        return BEARISH_MULTIPLIER_DAILY;
    }

    private double getThreshold(Timeframe timeframe) {

        if (timeframe == Timeframe.MONTHLY) {
            return THRESHOLD_MONTHLY;
        } else if (timeframe == Timeframe.WEEKLY) {
            return THRESHOLD_WEEKLY;
        }
        return THRESHOLD_DAILY;
    }

    @Override
    public boolean isTradingValueSufficient(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        long minimumTradingVolume = MIN_TRADING_VOLUME;
        double minimumTradingValue = MIN_TRADING_VALUE;

        if (timeframe == Timeframe.WEEKLY) {
            minimumTradingVolume = 2 * 5 * minimumTradingVolume;
            minimumTradingValue = 2 * 5 * minimumTradingValue;
        }

        if (timeframe == Timeframe.MONTHLY) {
            minimumTradingVolume = 2 * 20 * minimumTradingVolume;
            minimumTradingValue = 2 * 20 * minimumTradingValue;
        }

        double avgClose;
        long avgVolume;

        avgClose = stockTechnicals.getSma20();
        avgVolume = stockTechnicals.getVolumeAvg20();

        double avgTradingValue = avgClose * avgVolume;

        log.info(
                "{} average trading value {}",
                stockPrice.getStock().getNseSymbol(),
                avgTradingValue);

        return avgTradingValue >= minimumTradingValue || avgVolume >= minimumTradingVolume;
    }

    @Override
    public boolean isVolumeSurge(StockTechnicals stockTechnicals) {

        long currentVolume = stockTechnicals.getVolume();
        long prevVolume = stockTechnicals.getPrevVolume();
        long prevPrevVolume = stockTechnicals.getPrev2Volume();
        long avgVolume = stockTechnicals.getVolumeAvg20();
        long prevAvgVolume = stockTechnicals.getPrevVolumeAvg20();
        long prevPrevAvgVolume = stockTechnicals.getPrev2VolumeAvg20();

        if (stockTechnicals.getTimeframe() == Timeframe.WEEKLY) {
            avgVolume = stockTechnicals.getVolumeAvg10();
            prevAvgVolume = stockTechnicals.getPrevVolumeAvg10();
            prevPrevAvgVolume = stockTechnicals.getPrev2VolumeAvg10();
        }

        if (stockTechnicals.getTimeframe() == Timeframe.MONTHLY) {
            avgVolume = stockTechnicals.getVolumeAvg5();
            prevAvgVolume = stockTechnicals.getPrevVolumeAvg5();
            prevPrevAvgVolume = stockTechnicals.getPrev2VolumeAvg5();
        }

        Timeframe timeframe = stockTechnicals.getTimeframe();
        double multiplier =
                volumeMultipleFactor.getOrDefault(timeframe, 1.5); // fallback multiplier

        boolean isAvgIncreasing = avgVolume > prevAvgVolume;
        boolean isVolumeIncreasing = currentVolume > prevVolume;
        boolean isVolumeAboveAverage = currentVolume > avgVolume;

        if (currentVolume < avgVolume) {
            return false;
        }
        if (isAvgIncreasing && currentVolume > 1.5 * avgVolume) {
            evaluationLogService.add(
                    stockTechnicals,
                    EvaluationLog.Type.POSITIVE,
                    StringUtils.format(
                            "Volume Surge: AvgIncreasing && currentVolume > 1.5 * avgVolume"));
            return true;
        } else if (isAvgIncreasing && currentVolume > 2 * prevVolume) {
            evaluationLogService.add(
                    stockTechnicals,
                    EvaluationLog.Type.POSITIVE,
                    StringUtils.format(
                            "Volume Surge: AvgIncreasing && currentVolume > 2 * prevVolume"));
            return true;
        } else if (isAvgIncreasing
                && (currentVolume > 1.25 * avgVolume)
                && (prevVolume > 1.25 * prevAvgVolume)) {
            evaluationLogService.add(
                    stockTechnicals,
                    EvaluationLog.Type.POSITIVE,
                    StringUtils.format(
                            "Volume Surge: AvgIncreasing && currentVolume > 1.25 * avgVolume &&"
                                    + " prevVolume > 1.25 * prevAvgVolume"));
            return true;
        } else if (isAvgIncreasing && isVolumeIncreasing && isVolumeAboveAverage) {
            evaluationLogService.add(
                    stockTechnicals,
                    EvaluationLog.Type.POSITIVE,
                    StringUtils.format(
                            "Volume Surge: AvgIncreasing && VolumeIncreasing &&"
                                    + " VolumeAboveAverage"));
            return true;
        } else if (isAvgIncreasing
                && (prevVolume > 1.25 * prevAvgVolume)
                && (prevPrevVolume > 1.25 * prevPrevAvgVolume)) {
            evaluationLogService.add(
                    stockTechnicals,
                    EvaluationLog.Type.POSITIVE,
                    StringUtils.format(
                            "Volume Surge: AvgIncreasing && prevPrevVolume > 1.25 *"
                                    + " prevPrevAvgVolume && prevVolume > 1.25 * prevAvgVolume"));
            return true;
        }

        evaluationLogService.add(
                stockTechnicals,
                EvaluationLog.Type.NEUTRAL,
                StringUtils.format(
                        "No volume surge: volume:{} prevVolume:{} avg20:{} prevAvg20:{}"
                                + " multiplier:{}",
                        currentVolume,
                        prevVolume,
                        avgVolume,
                        prevAvgVolume,
                        multiplier));

        return false;
    }

    @Override
    public boolean isMinVolumeAvg(StockTechnicals stockTechnicals, double multiplier) {
        long minVolumeAvg = MIN_VOLUME_AVG;

        if (stockTechnicals.getTimeframe() == Timeframe.WEEKLY) {
            minVolumeAvg = 2 * 5 * minVolumeAvg;
        }
        if (stockTechnicals.getTimeframe() == Timeframe.MONTHLY) {
            minVolumeAvg = 2 * 20 * minVolumeAvg;
        }
        return stockTechnicals.getVolumeAvg20() > minVolumeAvg * multiplier;
    }

    @Override
    public boolean isMinVolume(StockTechnicals stockTechnicals, double multiplier) {
        long minVolume = MIN_VOLUME;

        if (stockTechnicals.getTimeframe() == Timeframe.WEEKLY) {
            minVolume = 2 * 5 * minVolume;
        }
        if (stockTechnicals.getTimeframe() == Timeframe.MONTHLY) {
            minVolume = 2 * 20 * minVolume;
        }
        return stockTechnicals.getVolume() > minVolume * multiplier;
    }
}
