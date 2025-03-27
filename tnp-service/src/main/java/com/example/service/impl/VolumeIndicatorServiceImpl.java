package com.example.service.impl;

import com.example.enhanced.model.stocks.StockTechnicals;
import com.example.service.VolumeIndicatorService;
import com.example.util.io.model.type.Timeframe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VolumeIndicatorServiceImpl implements VolumeIndicatorService {

    private static double BULLISH_MULTIPLIER = 2.0;
    private static double BEARISH_MULTIPLIER = 1.5;

    private static double THRESHOLD = 0.5;


    @Override
    public boolean isBullish(StockTechnicals stockTechnicals, Timeframe timeframe) {
        return this.isCurrentSessionHigh(stockTechnicals, timeframe, BULLISH_MULTIPLIER) || this.isPreviousSessionHigh(stockTechnicals, timeframe, BULLISH_MULTIPLIER);
    }

    @Override
    public boolean isBullish(StockTechnicals stockTechnicals, Timeframe timeframe, double multiplier) {
        return this.isCurrentSessionHigh(stockTechnicals, timeframe, multiplier) || this.isPreviousSessionHigh(stockTechnicals, timeframe, multiplier);
    }

    @Override
    public boolean isBearish(StockTechnicals stockTechnicals, Timeframe timeframe) {
        return this.isCurrentSessionHigh(stockTechnicals, timeframe, BEARISH_MULTIPLIER) || this.isPreviousSessionHigh(stockTechnicals, timeframe, BEARISH_MULTIPLIER);
    }

    @Override
    public boolean isBearish(StockTechnicals stockTechnicals, Timeframe timeframe, double multiplier) {
        return this.isCurrentSessionHigh(stockTechnicals, timeframe, multiplier) || this.isPreviousSessionHigh(stockTechnicals, timeframe, multiplier);
    }

    @Override
    public boolean isBullish(Timeframe timeframe, StockTechnicals stockTechnicals, int days) {

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

        if(days == 3){
            return this.checkThreeSession(volume, volumeMA, preVolume, prevVolumeMA, prev2Volume, prev2VolumeMA, BULLISH_MULTIPLIER);
        }
        if(days == 2){
            return this.checkTwoSession(volume, volumeMA, preVolume, prevVolumeMA, BULLISH_MULTIPLIER);
        }

        return this.checkSingleSession(volume, volumeMA, preVolume, prevVolumeMA, BULLISH_MULTIPLIER);
    }

    @Override
    public boolean isBearish(Timeframe timeframe, StockTechnicals stockTechnicals, int days) {

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

        if(days == 3){
            return this.checkThreeSession(volume, volumeMA, preVolume, prevVolumeMA, prev2Volume, prev2VolumeMA, BEARISH_MULTIPLIER);
        }
        if(days == 2){
            return this.checkTwoSession(volume, volumeMA, preVolume, prevVolumeMA, BEARISH_MULTIPLIER);
        }

        return this.checkSingleSession(volume, volumeMA, preVolume, prevVolumeMA, BEARISH_MULTIPLIER);
    }

    /**
     * Determines if the current session has a significantly high volume compared to its historical averages.
     * The function selects the appropriate volume average based on the timeframe and applies a multiplier
     * to assess if the current volume is unusually high.
     *
     * @param stockTechnicals The stock's technical data, including volume and moving averages.
     * @param timeframe       The timeframe (Daily, Weekly, Monthly) used to determine volume trends.
     * @param multiplier      A factor to compare current volume against historical averages.
     * @return {@code true} if the current session's volume is high and increasing, otherwise {@code false}.
     */
    private boolean isCurrentSessionHigh(StockTechnicals stockTechnicals, Timeframe timeframe, double multiplier) {

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
        else if (this.isHighVolume(stockTechnicals.getVolume(), avg, multiplier - THRESHOLD) &&
                this.isHighVolume(stockTechnicals.getVolume(), stockTechnicals.getPrevVolume(), multiplier)) {
            if (this.isIncreasing(stockTechnicals.getVolume(), stockTechnicals.getPrevVolume())) {
                if (this.isIncreasing(avg, prevAvg)) {
                    return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE;
    }

    /**
     * Determines if the previous session had significantly high volume compared to historical averages.
     * The method checks if the previous session's volume was higher than a selected moving average,
     * and whether it was increasing over multiple periods.
     *
     * @param stockTechnicals The stock's technical indicators, including volume and moving averages.
     * @param timeframe       The timeframe (Daily, Weekly, Monthly) to assess volume trends.
     * @param multiplier      A factor to compare previous session volume against historical averages.
     * @return {@code true} if the previous session had a high and increasing volume trend, otherwise {@code false}.
     */
    private boolean isPreviousSessionHigh(StockTechnicals stockTechnicals, Timeframe timeframe, double multiplier) {

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
            if (this.isIncreasing(stockTechnicals.getPrevVolume(), stockTechnicals.getPrev2Volume())) {
                if (this.isIncreasing(avg, prevAvg)) {
                    return Boolean.TRUE;
                }
            }
        }
        // Alternative condition: Volume is higher than both avg (1.0x) and prev volume (multiplier)
        else if (this.isHighVolume(stockTechnicals.getPrevVolume(), avg, multiplier - THRESHOLD) &&
                this.isHighVolume(stockTechnicals.getPrevVolume(), stockTechnicals.getPrev2Volume(), multiplier)) {
            if (this.isIncreasing(stockTechnicals.getPrevVolume(), stockTechnicals.getPrev2Volume())) {
                if (this.isIncreasing(avg, prevAvg)) {
                    return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE;
    }

    /**
     * // Check 1-session candlestick high volume
     *
     * @param volume
     * @param volumeMA
     * @param prevVolume
     * @param prevVolumeMA
     * @param threshold
     * @return
     */
    public boolean checkSingleSession(long volume, long volumeMA, long prevVolume, long prevVolumeMA, double threshold) {
        return this.isHighVolume(volume, volumeMA, threshold) || this.isHighVolume(prevVolume, prevVolumeMA,threshold) || this.isHighRVOL(volume, volumeMA, threshold) || this.isHighRVOL(prevVolume, prevVolumeMA, threshold);
    }

    public boolean checkTwoSession(long volume, long volumeMA, long prevVolume, long prevVolumeMA, double threshold) {
        double avgVolume = (volume + prevVolume) / 2.0;
        double avgVolumeMA = (volumeMA + prevVolumeMA) / 2.0;

        // Condition 1: Average volume is greater than threshold × average volume MA
        boolean condition1 = avgVolume > threshold * avgVolumeMA;

        // Condition 2: Increasing volume pattern (prev volume above prev MA, current volume above current MA, and increasing)
        boolean condition2 = prevVolume > prevVolumeMA && volume > volumeMA && volume > prevVolume;

        // Condition 3: Combined RVOL check (Relative volume ratio > threshold)
        boolean condition3 = avgVolumeMA > 0 && (avgVolume / avgVolumeMA) > threshold;

        return condition1 || condition2 || condition3;
    }

    public boolean checkThreeSession(long volume, long volumeMA, long prevVolume, long prevVolumeMA,
                                     long prev2Volume, long prev2VolumeMA, double threshold) {
        double avgVolume = (volume + prevVolume + prev2Volume) / 3.0;
        double avgVolumeMA = (volumeMA + prevVolumeMA + prev2VolumeMA) / 3.0;

        // Condition 1: Average volume over 3 sessions is greater than threshold × average volume MA
        boolean condition1 = avgVolume > threshold * avgVolumeMA;

        // Condition 2: Increasing volume pattern (each volume is above its MA and increasing)
        boolean condition2 = prev2Volume > prev2VolumeMA && prevVolume > prevVolumeMA && volume > volumeMA
                && prevVolume > prev2Volume && volume > prevVolume;

        // Condition 3: Combined RVOL check (Relative volume ratio > threshold)
        boolean condition3 = avgVolumeMA > 0 && (avgVolume / avgVolumeMA) > threshold;

        return condition1 || condition2 || condition3;
    }


    private boolean isHighVolume(long volume, long average, double multiplier){
        return ((average > 0) && volume >= (average * multiplier)) ? Boolean.TRUE : Boolean.FALSE;
    }

    public boolean isHighRVOL(long volume, long volumeMA, double threshold) {
        return (volume / volumeMA) > threshold;
    }

    private boolean isIncreasing(long volume, long preVolume){
        return (volume > preVolume) ? Boolean.TRUE : Boolean.FALSE;
    }
}
