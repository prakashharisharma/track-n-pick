package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import com.example.service.utils.MovingAverageUtil;
import com.example.service.utils.TrendDirectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DynamicTrendServiceImpl implements DynamicTrendService {

    private final StockPriceService<StockPrice> stockPriceService;

    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    private boolean isShortestTermUpTrend(
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            StockPrice stockPrice,
            MovingAverageResult movingAverageResult) {

        // MovingAverageResult  movingAverageResult =
        // MovingAverageUtil.getMovingAverage(MovingAverageLength.SHORTEST, timeframe,
        // stockTechnicals, true);

        double avg = movingAverageResult.getValue();
        double prevAvg = movingAverageResult.getPrevValue();

        return this.isUpTrend(avg, prevAvg, stockPrice.getClose(), stockPrice.getPrevClose());
    }
    /**
     * EMA5, EMA10, EMA20
     *
     * @param stockTechnicals
     * @return
     */
    private boolean isShortTermUpTrend(
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            StockPrice stockPrice,
            MovingAverageResult movingAverageResult) {

        // MovingAverageResult  movingAverageResult =
        // MovingAverageUtil.getMovingAverage(MovingAverageLength.SHORT, timeframe, stockTechnicals,
        // true);

        double avg = movingAverageResult.getValue();
        double prevAvg = movingAverageResult.getPrevValue();

        return this.isUpTrend(avg, prevAvg, stockPrice.getClose(), stockPrice.getPrevClose());
    }

    /**
     * EMA20, EMA50, EMA100
     *
     * @param stockTechnicals
     * @return
     */
    private boolean isMediumTermUpTrend(
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            StockPrice stockPrice,
            MovingAverageResult movingAverageResult) {
        // MovingAverageResult  movingAverageResult =
        // MovingAverageUtil.getMovingAverage(MovingAverageLength.MEDIUM, timeframe,
        // stockTechnicals, true);

        double avg = movingAverageResult.getValue();
        double prevAvg = movingAverageResult.getPrevValue();

        return this.isUpTrend(avg, prevAvg, stockPrice.getClose(), stockPrice.getPrevClose());
    }

    /**
     * EMA50, EMA100, EMA200
     *
     * @param stockTechnicals
     * @return
     */
    private boolean isLongTermUpTrend(
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            StockPrice stockPrice,
            MovingAverageResult movingAverageResult) {

        // MovingAverageResult  movingAverageResult =
        // MovingAverageUtil.getMovingAverage(MovingAverageLength.LONG, timeframe, stockTechnicals,
        // true);

        double avg = movingAverageResult.getValue();
        double prevAvg = movingAverageResult.getPrevValue();

        return this.isUpTrend(avg, prevAvg, stockPrice.getClose(), stockPrice.getPrevClose());
    }

    private boolean isLongestTermUpTrend(
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            StockPrice stockPrice,
            MovingAverageResult movingAverageResult) {

        double avg = movingAverageResult.getValue();
        double prevAvg = movingAverageResult.getPrevValue();

        return this.isUpTrend(avg, prevAvg, stockPrice.getClose(), stockPrice.getPrevClose());
    }

    private boolean isUpTrend(double average, double prevAverage, double close, double prevClose) {

        // Ensure previous EMA is valid
        if (prevAverage == 0.0) {
            return true; // Not enough data to determine trend, will return true by default
        }

        // Condition 1: Price Above EMA (Price trading above moving average)
        boolean priceAboveEMA = close > average;

        // Condition 2: Slope of EMA (EMA is trending upward)
        // boolean emaSlopeUp = average > prevAverage;

        // Condition 3: EMA Support Bounce (Price was below EMA before, now above)
        boolean emaSupportBounce = (prevClose < prevAverage) && (close > prevAverage);

        // Confirm Uptrend: Either price is above rising EMA or EMA bounce is confirmed
        return (priceAboveEMA) || (emaSupportBounce);
    }

    private boolean isShortestTermDownTrend(
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            StockPrice stockPrice,
            MovingAverageResult movingAverageResult) {

        double avg = movingAverageResult.getValue();
        double prevAvg = movingAverageResult.getPrevValue();

        return this.isDownTrend(avg, prevAvg, stockPrice.getClose(), stockPrice.getPrevClose());
    }

    /**
     * EMA5, EMA10, EMA20
     *
     * @param stockTechnicals
     * @return
     */
    private boolean isShortTermDownTrend(
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            StockPrice stockPrice,
            MovingAverageResult movingAverageResult) {

        // MovingAverageResult  movingAverageResult =
        // MovingAverageUtil.getMovingAverage(MovingAverageLength.SHORT, timeframe, stockTechnicals,
        // true);

        double avg = movingAverageResult.getValue();
        double prevAvg = movingAverageResult.getPrevValue();

        return this.isDownTrend(avg, prevAvg, stockPrice.getClose(), stockPrice.getPrevClose());
    }

    /**
     * EMA20, EMA50, EMA100
     *
     * @param stockTechnicals
     * @return
     */
    private boolean isMediumTermDownTrend(
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            StockPrice stockPrice,
            MovingAverageResult movingAverageResult) {

        // MovingAverageResult  movingAverageResult =
        // MovingAverageUtil.getMovingAverage(MovingAverageLength.MEDIUM, timeframe,
        // stockTechnicals, true);

        double avg = movingAverageResult.getValue();
        double prevAvg = movingAverageResult.getPrevValue();

        return this.isDownTrend(avg, prevAvg, stockPrice.getClose(), stockPrice.getPrevClose());
    }

    /**
     * EMA50, EMA100, EMA200
     *
     * @param stockTechnicals
     * @return
     */
    private boolean isLongTermDownTrend(
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            StockPrice stockPrice,
            MovingAverageResult movingAverageResult) {

        // MovingAverageResult  movingAverageResult =
        // MovingAverageUtil.getMovingAverage(MovingAverageLength.LONG, timeframe, stockTechnicals,
        // true);

        double avg = movingAverageResult.getValue();
        double prevAvg = movingAverageResult.getPrevValue();

        return this.isDownTrend(avg, prevAvg, stockPrice.getClose(), stockPrice.getPrevClose());
    }

    private boolean isLongestTermDownTrend(
            Timeframe timeframe,
            StockTechnicals stockTechnicals,
            StockPrice stockPrice,
            MovingAverageResult movingAverageResult) {

        double avg = movingAverageResult.getValue();
        double prevAvg = movingAverageResult.getPrevValue();

        return this.isDownTrend(avg, prevAvg, stockPrice.getClose(), stockPrice.getPrevClose());
    }

    private boolean isDownTrend(
            double average, double prevAverage, double close, double prevClose) {

        // Ensure previous EMA is valid
        if (prevAverage == 0.0) {
            return true; // Not enough data to determine trend, will return true by default
        }

        // Condition 1: Price Below EMA
        boolean priceBelowEMA = close < average;

        // Condition 2: Slope of EMA (EMA is falling)
        // boolean emaSlopeDown = average < prevAverage;

        // Condition 3: EMA Resistance Bounce (Price was above EMA before, now below)
        boolean emaResistanceBounce = (prevClose > prevAverage) && (close < average);

        // Confirm Downtrend: Either price is below a falling EMA or price rejected from EMA.
        return (priceBelowEMA) || (emaResistanceBounce);
    }

    @Override
    public Trend detect(Stock stock, Timeframe timeframe) {

        Trend.Phase phase = Trend.Phase.SIDEWAYS;

        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);

        StockPrice stockPrice = stockPriceService.get(stock, timeframe);

        if (stockTechnicals == null || stockPrice == null) {
            log.info("stockTechnicals or stockPrice not found");
            return new Trend(Trend.Direction.INVALID, phase);
        }

        MovingAverageResult shortestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, true);

        boolean shortestTermDown =
                this.isShortestTermDownTrend(
                        timeframe, stockTechnicals, stockPrice, shortestMovingAverageResult);
        boolean shortestTermUp =
                this.isShortestTermUpTrend(
                        timeframe, stockTechnicals, stockPrice, shortestMovingAverageResult);
        MovingAverageResult shortMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGH, timeframe, stockTechnicals, true);

        boolean shortTermDown =
                this.isShortTermDownTrend(
                        timeframe, stockTechnicals, stockPrice, shortMovingAverageResult);
        boolean shortTermUp =
                this.isShortTermUpTrend(
                        timeframe, stockTechnicals, stockPrice, shortMovingAverageResult);

        MovingAverageResult mediumMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.MEDIUM, timeframe, stockTechnicals, true);

        boolean mediumTermDown =
                this.isMediumTermDownTrend(
                        timeframe, stockTechnicals, stockPrice, mediumMovingAverageResult);
        boolean mediumTermUp =
                this.isMediumTermUpTrend(
                        timeframe, stockTechnicals, stockPrice, mediumMovingAverageResult);
        MovingAverageResult longMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.LOW, timeframe, stockTechnicals, true);

        boolean longTermDown =
                this.isLongTermDownTrend(
                        timeframe, stockTechnicals, stockPrice, longMovingAverageResult);
        boolean longTermUp =
                this.isLongTermUpTrend(
                        timeframe, stockTechnicals, stockPrice, longMovingAverageResult);
        MovingAverageResult longestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.LOWEST, timeframe, stockTechnicals, true);

        boolean longestTermDown =
                this.isLongestTermDownTrend(
                        timeframe, stockTechnicals, stockPrice, longestMovingAverageResult);
        boolean longestTermUp =
                this.isLongestTermUpTrend(
                        timeframe, stockTechnicals, stockPrice, longestMovingAverageResult);

        // 1️⃣ DIP: Shortest-term down, but short-term (20 EMA) is flat or uncertain, while medium &
        // long-term up
        if (shortestTermDown && shortTermUp && mediumTermUp && longTermUp && longestTermUp) {
            if (MovingAverageUtil.isIncreasing(shortMovingAverageResult)) {
                phase = Trend.Phase.DIP;
            }
        }

        // 2️⃣ PULLBACK: Short-term downtrend (5 EMA & 20 EMA falling), but medium & long-term
        // uptrend
        if (shortestTermDown && shortTermDown && mediumTermUp && longTermUp && longestTermUp) {
            if (MovingAverageUtil.isIncreasing(mediumMovingAverageResult)) {
                phase = Trend.Phase.PULLBACK;
            }
        }

        // 3️⃣ CORRECTION: Short-term & medium-term downtrend, but long-term uptrend
        if (shortTermDown && shortTermDown && mediumTermDown && longTermUp && longestTermUp) {
            if (MovingAverageUtil.isIncreasing(longMovingAverageResult)) {
                phase = Trend.Phase.CORRECTION;
            }
        }

        // 4️⃣ MILD_BOTTOM: Strong downtrend across all timeframes (5, 20, 50, 200 EMA all bearish)
        if (shortestTermDown && shortTermDown && mediumTermDown && longTermDown && longestTermUp) {
            if (MovingAverageUtil.isIncreasing(longestMovingAverageResult)) {
                phase = Trend.Phase.DEEP_CORRECTION;
            }
        }

        // 4️⃣ BOTTOM: Strong downtrend across all timeframes (5, 20, 50, 200 EMA all bearish)
        if (shortestTermDown
                && shortTermDown
                && mediumTermDown
                && longTermDown
                && longestTermDown) {
            phase = Trend.Phase.BOTTOM;
        }

        // 8️⃣ TOP: Short-term weakening while medium & long-term are still bullish
        if (shortestTermUp && shortTermUp && mediumTermUp && longTermUp && longestTermUp) {
            phase = Trend.Phase.TOP;
        }

        return new Trend(TrendDirectionUtil.findDirection(stockPrice), phase);
    }
}
