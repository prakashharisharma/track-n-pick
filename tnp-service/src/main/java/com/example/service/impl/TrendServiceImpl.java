package com.example.service.impl;

import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.model.stocks.StockTechnicals;
import com.example.transactional.service.StockPriceService;
import com.example.transactional.service.StockTechnicalsService;
import com.example.transactional.utils.MovingAverageUtil;
import com.example.transactional.model.master.Stock;
import com.example.service.TrendService;
import com.example.util.io.model.type.Timeframe;
import com.example.util.io.model.type.Trend;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TrendServiceImpl implements TrendService {

    private final StockPriceService<StockPrice> stockPriceService;

    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    /**
     * EMA5, EMA10, EMA20
     * @param stockTechnicals
     * @return
     */
    private boolean isShortTermUpTrend(Timeframe timeframe,StockTechnicals stockTechnicals, StockPrice stockPrice){

        if(timeframe == Timeframe.DAILY) {
            return this.isUpTrend(MovingAverageUtil.getMovingAverage20(timeframe, stockTechnicals), MovingAverageUtil.getPrevMovingAverage20(timeframe, stockTechnicals), stockPrice.getClose(), stockPrice.getPrevClose());
        }else if(timeframe == Timeframe.WEEKLY) {
            return this.isUpTrend(MovingAverageUtil.getMovingAverage10(timeframe, stockTechnicals), MovingAverageUtil.getPrevMovingAverage10(timeframe, stockTechnicals), stockPrice.getClose(), stockPrice.getPrevClose());
        }
        else if(timeframe == Timeframe.MONTHLY) {
            return this.isUpTrend(MovingAverageUtil.getMovingAverage5(timeframe, stockTechnicals), MovingAverageUtil.getPrevMovingAverage5(timeframe, stockTechnicals), stockPrice.getClose(), stockPrice.getPrevClose());
        }
        return Boolean.FALSE;
    }

    /**
     * EMA20, EMA50, EMA100
     * @param stockTechnicals
     * @return
     */
    private boolean isMediumTermUpTrend(Timeframe timeframe,StockTechnicals stockTechnicals, StockPrice stockPrice){
        if(timeframe == Timeframe.DAILY) {
            return this.isUpTrend(MovingAverageUtil.getMovingAverage50(timeframe, stockTechnicals), MovingAverageUtil.getPrevMovingAverage50(timeframe, stockTechnicals), stockPrice.getClose(), stockPrice.getPrevClose());
        }else if(timeframe == Timeframe.WEEKLY) {
            return this.isUpTrend(MovingAverageUtil.getMovingAverage20(timeframe, stockTechnicals), MovingAverageUtil.getPrevMovingAverage20(timeframe, stockTechnicals), stockPrice.getClose(), stockPrice.getPrevClose());
        }else if(timeframe == Timeframe.MONTHLY) {
            return this.isUpTrend(MovingAverageUtil.getMovingAverage10(timeframe, stockTechnicals), MovingAverageUtil.getPrevMovingAverage10(timeframe, stockTechnicals), stockPrice.getClose(), stockPrice.getPrevClose());
        }

        return Boolean.FALSE;
    }

    /**
     * EMA50, EMA100, EMA200
     * @param stockTechnicals
     * @return
     */
    private boolean isLongTermUpTrend(Timeframe timeframe,StockTechnicals stockTechnicals, StockPrice stockPrice){

        if(timeframe == Timeframe.DAILY) {
            return this.isUpTrend(MovingAverageUtil.getMovingAverage200(timeframe, stockTechnicals), MovingAverageUtil.getPrevMovingAverage200(timeframe, stockTechnicals), stockPrice.getClose(), stockPrice.getPrevClose());
        }else if(timeframe == Timeframe.WEEKLY) {
            return this.isUpTrend(MovingAverageUtil.getMovingAverage50(timeframe, stockTechnicals), MovingAverageUtil.getPrevMovingAverage50(timeframe, stockTechnicals), stockPrice.getClose(), stockPrice.getPrevClose());
        }else if(timeframe == Timeframe.MONTHLY) {
            return this.isUpTrend(MovingAverageUtil.getMovingAverage20(timeframe, stockTechnicals), MovingAverageUtil.getPrevMovingAverage20(timeframe, stockTechnicals), stockPrice.getClose(), stockPrice.getPrevClose());
        }

        return Boolean.FALSE;
    }


    private boolean isUpTrend(double average, double prevAverage, double close, double prevClose) {

        // Ensure previous EMA is valid
        if (prevAverage == 0.0) {
            return false; // Not enough data to determine trend
        }

        // Condition 1: Price Above EMA (Price trading above moving average)
        boolean priceAboveEMA = close > average;

        // Condition 2: Slope of EMA (EMA is trending upward)
        boolean emaSlopeUp = average > prevAverage;

        // Condition 3: EMA Support Bounce (Price was below EMA before, now above)
        boolean emaSupportBounce = (prevClose < prevAverage) && (close > prevAverage);

        // Confirm Uptrend: Either price is above rising EMA or EMA bounce is confirmed
        return (priceAboveEMA && emaSlopeUp) || (emaSlopeUp && emaSupportBounce);
    }


    /**
     * EMA5, EMA10, EMA20
     * @param stockTechnicals
     * @return
     */
    private boolean isShortTermDownTrend(Timeframe timeframe,StockTechnicals stockTechnicals, StockPrice stockPrice){

        if(timeframe == Timeframe.DAILY) {
            return this.isDownTrend(MovingAverageUtil.getMovingAverage20(timeframe, stockTechnicals), MovingAverageUtil.getPrevMovingAverage20(timeframe, stockTechnicals), stockPrice.getClose(), stockPrice.getPrevClose());
        }else if(timeframe == Timeframe.WEEKLY) {
            return this.isDownTrend(MovingAverageUtil.getMovingAverage10(timeframe, stockTechnicals), MovingAverageUtil.getPrevMovingAverage10(timeframe, stockTechnicals), stockPrice.getClose(), stockPrice.getPrevClose());
        }
        else if(timeframe == Timeframe.MONTHLY) {
            return this.isDownTrend(MovingAverageUtil.getMovingAverage5(timeframe, stockTechnicals), MovingAverageUtil.getPrevMovingAverage5(timeframe, stockTechnicals), stockPrice.getClose(), stockPrice.getPrevClose());
        }

        return Boolean.FALSE;
    }

    /**
     * EMA20, EMA50, EMA100
     * @param stockTechnicals
     * @return
     */
    private boolean isMediumTermDownTrend(Timeframe timeframe,StockTechnicals stockTechnicals, StockPrice stockPrice){

        if(timeframe == Timeframe.DAILY) {
            return this.isDownTrend(MovingAverageUtil.getMovingAverage50(timeframe, stockTechnicals), MovingAverageUtil.getPrevMovingAverage50(timeframe, stockTechnicals), stockPrice.getClose(), stockPrice.getPrevClose());
        }else if(timeframe == Timeframe.WEEKLY) {
            return this.isDownTrend(MovingAverageUtil.getMovingAverage20(timeframe, stockTechnicals), MovingAverageUtil.getPrevMovingAverage20(timeframe, stockTechnicals), stockPrice.getClose(), stockPrice.getPrevClose());
        }else if(timeframe == Timeframe.MONTHLY) {
            return this.isDownTrend(MovingAverageUtil.getMovingAverage10(timeframe, stockTechnicals), MovingAverageUtil.getPrevMovingAverage10(timeframe, stockTechnicals), stockPrice.getClose(), stockPrice.getPrevClose());
        }

        return Boolean.FALSE;
    }

    /**
     *
     * EMA50, EMA100, EMA200
     * @param stockTechnicals
     * @return
     */
    private boolean isLongTermDownTrend(Timeframe timeframe,StockTechnicals stockTechnicals, StockPrice stockPrice){


        if(timeframe == Timeframe.DAILY) {
            return this.isDownTrend(MovingAverageUtil.getMovingAverage200(timeframe, stockTechnicals), MovingAverageUtil.getPrevMovingAverage200(timeframe, stockTechnicals), stockPrice.getClose(), stockPrice.getPrevClose());
        }else if(timeframe == Timeframe.WEEKLY) {
            return this.isDownTrend(MovingAverageUtil.getMovingAverage50(timeframe, stockTechnicals), MovingAverageUtil.getPrevMovingAverage50(timeframe, stockTechnicals), stockPrice.getClose(), stockPrice.getPrevClose());
        }else if(timeframe == Timeframe.MONTHLY) {
            return this.isDownTrend(MovingAverageUtil.getMovingAverage20(timeframe, stockTechnicals), MovingAverageUtil.getPrevMovingAverage20(timeframe, stockTechnicals), stockPrice.getClose(), stockPrice.getPrevClose());
        }

        return Boolean.FALSE;
    }

    private boolean isDownTrend(double average, double prevAverage, double close, double prevClose) {

        // Ensure previous EMA is valid
        if (prevAverage == 0.0) {
            return false; // Not enough data to determine trend
        }

        // Condition 1: Price Below EMA
        boolean priceBelowEMA = close < average;

        // Condition 2: Slope of EMA (EMA is falling)
        boolean emaSlopeDown = average < prevAverage;

        // Condition 3: EMA Resistance Bounce (Price was above EMA before, now below)
        boolean emaResistanceBounce = (prevClose > prevAverage) && (close < average);

        // Confirm Downtrend: Either price is below a falling EMA or price rejected from EMA.
        return (priceBelowEMA && emaSlopeDown) || (emaSlopeDown && emaResistanceBounce);
    }


    @Override
    public Trend detect(Stock stock, Timeframe timeframe) {

        Trend.Momentum momentum = Trend.Momentum.SIDEWAYS;

        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);

        StockPrice stockPrice = stockPriceService.get(stock, timeframe);

        if(stockTechnicals == null || stockPrice == null){
            return  new Trend(Trend.Direction.INVALID,Trend.Strength.INVALID, momentum);
        }

        boolean shortTermDown = this.isShortTermDownTrend(timeframe, stockTechnicals, stockPrice);
        boolean shortTermUp = this.isShortTermUpTrend(timeframe, stockTechnicals, stockPrice);

        boolean mediumTermDown = this.isMediumTermDownTrend(timeframe, stockTechnicals, stockPrice);
        boolean mediumTermUp = this.isMediumTermUpTrend(timeframe, stockTechnicals, stockPrice);

        boolean longTermDown = this.isLongTermDownTrend(timeframe, stockTechnicals, stockPrice);
        boolean longTermUp = this.isLongTermUpTrend(timeframe, stockTechnicals, stockPrice);

        // 1️⃣ Pullback: Short-term downtrend, but medium & long-term uptrend
        if (shortTermDown && mediumTermUp && longTermUp) {
            momentum =  Trend.Momentum.PULLBACK;
        }

        // 2️⃣ Correction: Short-term & medium-term downtrend, but long-term uptrend
        if (shortTermDown && mediumTermDown && longTermUp) {
            momentum =  Trend.Momentum.CORRECTION;
        }

        // 3️⃣ Bottom: Strong downtrend across all timeframes
        if (shortTermDown && mediumTermDown && longTermDown) {
            momentum =  Trend.Momentum.BOTTOM;
        }

        // 4️⃣ Early Recovery: 20 EMA is rising, but 50 EMA & 200 EMA are still bearish
        if (shortTermUp && mediumTermDown && longTermDown) {
            momentum =  Trend.Momentum.RECOVERY;
        }

        // 5️⃣ Reversal: Short-term & medium-term uptrend, but long-term still bearish
        if (shortTermUp && mediumTermUp && longTermDown) {
            momentum =  Trend.Momentum.ADVANCE;
        }

        // 6️⃣ Top: All trends turning bullish, possible peak formation
        if (shortTermUp && mediumTermUp && longTermUp) {
            momentum =  Trend.Momentum.TOP;
        }

        if(momentum == Trend.Momentum.BOTTOM || momentum == Trend.Momentum.CORRECTION || momentum == Trend.Momentum.PULLBACK) {
            if(longTermDown){
                return new Trend(Trend.Direction.DOWN,Trend.Strength.LONG, momentum);
            }else if(mediumTermDown){
                return new Trend(Trend.Direction.DOWN,Trend.Strength.MEDIUM, momentum);
            }else if(shortTermDown){
                return new Trend(Trend.Direction.DOWN,Trend.Strength.SHORT, momentum);
            }
            return new Trend(Trend.Direction.DOWN, Trend.Strength.WEAK, momentum  );
        }else if(momentum == Trend.Momentum.TOP || momentum == Trend.Momentum.ADVANCE || momentum == Trend.Momentum.RECOVERY) {
            if(longTermUp){
                return new Trend(Trend.Direction.UP,Trend.Strength.LONG, momentum);
            }else if(mediumTermUp){
                return new Trend(Trend.Direction.UP,Trend.Strength.MEDIUM, momentum);
            }else if(shortTermUp){
                return new Trend(Trend.Direction.UP,Trend.Strength.SHORT, momentum);
            }
            return new Trend(Trend.Direction.UP, Trend.Strength.STRONG, momentum  );
        }

        // 7️⃣ Sideways: No clear trend (EMAs are flat or mixed)
        return  new Trend(Trend.Direction.INVALID,Trend.Strength.INVALID, momentum);
    }


}
