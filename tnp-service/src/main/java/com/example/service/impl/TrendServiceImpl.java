package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.model.stocks.StockTechnicals;
import com.example.service.CandleStickService;
import com.example.service.TrendService;
import com.example.util.io.model.type.Momentum;
import com.example.util.io.model.type.Trend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TrendServiceImpl implements TrendService {

    @Autowired
    private CandleStickService candleStickService;


    @Override
    public Trend isUpTrend(Stock stock) {

        StockTechnicals stockTechnicals = stock.getTechnicals();

        StockPrice stockPrice = stock.getStockPrice();

        if (stockTechnicals == null || stockPrice == null) {
            return new Trend(Trend.Strength.INVALID, Momentum.SIDEWAYS);
        }
        Momentum momentum = this.detect(stock);

        if(momentum == Momentum.TOP || momentum == Momentum.ADVANCE || momentum == Momentum.RECOVERY){
            if (this.isLongTermUpTrend(stockTechnicals, stockPrice)) {
                return new Trend(Trend.Strength.LONG, momentum);
            } else if (this.isMediumTermUpTrend(stockTechnicals, stockPrice)) {
                return new Trend(Trend.Strength.MEDIUM, momentum);
            } else if (this.isShortTermUpTrend(stockTechnicals, stockPrice)) {
                return new Trend(Trend.Strength.SHORT, momentum);
            }
        }

        return  new Trend(Trend.Strength.INVALID,momentum);
    }

    /**
     * EMA5, EMA10, EMA20
     * @param stockTechnicals
     * @return
     */
    private boolean isShortTermUpTrend(StockTechnicals stockTechnicals, StockPrice stockPrice){

        /*
        return this.isUpTrend(stockTechnicals.getEma5(), stockTechnicals.getEma10(), stockTechnicals.getEma20(), stockTechnicals.getPrevEma5(), stockTechnicals.getPrevEma10(), stockTechnicals.getPrevEma20())
                ||
                this.isUpTrend(stockTechnicals.getPrevEma5(), stockTechnicals.getPrevEma10(), stockTechnicals.getPrevEma20(), stockTechnicals.getPrevPrevEma5(), stockTechnicals.getPrevPrevEma10(), stockTechnicals.getPrevPrevEma20());
        */
        //return this.isUpTrend(stockTechnicals.getEma5(), stockTechnicals.getEma10(), stockTechnicals.getEma20(), stockTechnicals.getPrevEma20(), stockPrice.getClose());
        return stockPrice.getClose() > stockTechnicals.getEma20() && stockTechnicals.getEma20() != 0.0;
    }

    /**
     * EMA20, EMA50, EMA100
     * @param stockTechnicals
     * @return
     */
    private boolean isMediumTermUpTrend(StockTechnicals stockTechnicals, StockPrice stockPrice){

        /*
        return this.isUpTrend(stockTechnicals.getEma10(), stockTechnicals.getEma20(), stockTechnicals.getEma50(), stockTechnicals.getPrevEma10(), stockTechnicals.getPrevEma20(), stockTechnicals.getPrevEma50())
                ||
                this.isUpTrend(stockTechnicals.getPrevEma10(), stockTechnicals.getPrevEma20(), stockTechnicals.getPrevEma50(), stockTechnicals.getPrevPrevEma10(), stockTechnicals.getPrevPrevEma20(), stockTechnicals.getPrevPrevEma50());
                */
       // return this.isUpTrend(stockTechnicals.getEma20(), stockTechnicals.getEma50(), stockTechnicals.getEma100(), stockTechnicals.getPrevEma100(), stockPrice.getClose());
        return stockTechnicals.getEma20() > stockTechnicals.getEma50() && stockTechnicals.getEma50() != 0.0;
    }

    /**
     * EMA50, EMA100, EMA200
     * @param stockTechnicals
     * @return
     */
    private boolean isLongTermUpTrend(StockTechnicals stockTechnicals, StockPrice stockPrice){

        /*
        return this.isUpTrend(stockTechnicals.getEma50(), stockTechnicals.getEma100(), stockTechnicals.getEma200(), stockTechnicals.getPrevEma50(), stockTechnicals.getPrevEma100(), stockTechnicals.getPrevEma200())
                ||
                this.isUpTrend(stockTechnicals.getPrevEma50(), stockTechnicals.getPrevEma100(), stockTechnicals.getPrevEma200(), stockTechnicals.getPrevPrevEma50(), stockTechnicals.getPrevPrevEma100(), stockTechnicals.getPrevPrevEma200())
                ||
                this.isUpTrend(stockTechnicals.getEma20(), stockTechnicals.getEma50(), stockTechnicals.getEma100(), stockTechnicals.getPrevEma20(), stockTechnicals.getPrevEma50(), stockTechnicals.getPrevEma100())
                ||
                this.isUpTrend(stockTechnicals.getPrevEma20(), stockTechnicals.getPrevEma50(), stockTechnicals.getPrevEma100(), stockTechnicals.getPrevPrevEma20(), stockTechnicals.getPrevPrevEma50(), stockTechnicals.getPrevPrevEma100());
                */
        //return this.isUpTrend(stockTechnicals.getEma50(), stockTechnicals.getEma100(), stockTechnicals.getEma200(), stockTechnicals.getPrevEma200(), stockPrice.getClose());
        return stockTechnicals.getEma50() > stockTechnicals.getEma200() && stockTechnicals.getEma200() != 0.0;
    }

    private boolean isUpTrend(double immediateLow, double average, double immediateHigh, double prevImmediateLow, double prevAverage, double prevImmediateHigh){

        int score = 0;

        if (immediateHigh > prevImmediateHigh) {
            score = score + 1;
        }
        if (average > prevAverage) {
            score = score + 1;
        }
        if (immediateLow > prevImmediateLow) {
            score = score + 1;
        }

        return  score >= 2 ? Boolean.TRUE : Boolean.FALSE;
    }

    private boolean isUpTrend(double shortShorterAverage, double shorterAverage, double average, double prevAverage, double close){
        if(shortShorterAverage > shorterAverage && shorterAverage > average){
            if(average > prevAverage && close > average){
                return   Boolean.TRUE;
            }
        }

        return   Boolean.FALSE;
    }

    @Override
    public Trend isDownTrend(Stock stock) {

        StockTechnicals stockTechnicals= stock.getTechnicals();
        StockPrice stockPrice= stock.getStockPrice();

        if(stockTechnicals==null || stockPrice==null){
            return new Trend(Trend.Strength.INVALID, Momentum.SIDEWAYS);
        }
        Momentum momentum = this.detect(stock);
        if(momentum == Momentum.BOTTOM || momentum == Momentum.CORRECTION || momentum == Momentum.PULLBACK) {
            if (this.isLongTermDownTrend(stockTechnicals, stockPrice)) {
                return new Trend(Trend.Strength.LONG, momentum);
            } else if (this.isMediumTermDownTrend(stockTechnicals, stockPrice)) {
                return new Trend(Trend.Strength.MEDIUM, momentum);
            }
            if (this.isShortTermDownTrend(stockTechnicals, stockPrice)) {
                return new Trend(Trend.Strength.SHORT, momentum);
            }
        }

        return  new Trend(Trend.Strength.INVALID, momentum);
    }

    /**
     * EMA5, EMA10, EMA20
     * @param stockTechnicals
     * @return
     */
    private boolean isShortTermDownTrend(StockTechnicals stockTechnicals, StockPrice stockPrice){

        /*
        return this.isDownTrend(stockTechnicals.getEma5(), stockTechnicals.getEma10(), stockTechnicals.getEma20(), stockTechnicals.getPrevEma5(), stockTechnicals.getPrevEma10(), stockTechnicals.getPrevEma20())
                ||
                this.isDownTrend(stockTechnicals.getPrevEma5(), stockTechnicals.getPrevEma10(), stockTechnicals.getPrevEma20(), stockTechnicals.getPrevPrevEma5(), stockTechnicals.getPrevPrevEma10(), stockTechnicals.getPrevPrevEma20());

         */
        //return this.isDownTrend(stockTechnicals.getEma5(), stockTechnicals.getEma10(), stockTechnicals.getEma20(), stockTechnicals.getPrevEma20(), stockPrice.getClose());

        return stockPrice.getClose() < stockTechnicals.getEma20();
    }

    /**
     * EMA20, EMA50, EMA100
     * @param stockTechnicals
     * @return
     */
    private boolean isMediumTermDownTrend(StockTechnicals stockTechnicals,StockPrice stockPrice){

        /*
        return this.isDownTrend(stockTechnicals.getEma10(), stockTechnicals.getEma20(), stockTechnicals.getEma50(), stockTechnicals.getPrevEma10(), stockTechnicals.getPrevEma20(), stockTechnicals.getPrevEma50())
                ||
                this.isDownTrend(stockTechnicals.getPrevEma10(), stockTechnicals.getPrevEma20(), stockTechnicals.getPrevEma50(), stockTechnicals.getPrevPrevEma10(), stockTechnicals.getPrevPrevEma20(), stockTechnicals.getPrevPrevEma50());
        */
        //return this.isDownTrend(stockTechnicals.getEma20(), stockTechnicals.getEma50(), stockTechnicals.getEma100(), stockTechnicals.getPrevEma100(), stockPrice.getClose());
        return stockTechnicals.getEma20() < stockTechnicals.getEma50();
    }

    /**
     *
     * EMA50, EMA100, EMA200
     * @param stockTechnicals
     * @return
     */
    private boolean isLongTermDownTrend(StockTechnicals stockTechnicals, StockPrice stockPrice){

        /*
        return this.isDownTrend(stockTechnicals.getEma50(), stockTechnicals.getEma100(), stockTechnicals.getEma200(), stockTechnicals.getPrevEma50(), stockTechnicals.getPrevEma100(), stockTechnicals.getPrevEma200())
                ||
                this.isDownTrend(stockTechnicals.getPrevEma50(), stockTechnicals.getPrevEma100(), stockTechnicals.getPrevEma200(), stockTechnicals.getPrevPrevEma50(), stockTechnicals.getPrevPrevEma100(), stockTechnicals.getPrevPrevEma200())
                ||
                this.isDownTrend(stockTechnicals.getEma20(), stockTechnicals.getEma50(), stockTechnicals.getEma100(), stockTechnicals.getPrevEma20(), stockTechnicals.getPrevEma50(), stockTechnicals.getPrevEma100())
                ||
                this.isDownTrend(stockTechnicals.getPrevEma20(), stockTechnicals.getPrevEma50(), stockTechnicals.getPrevEma100(), stockTechnicals.getPrevPrevEma20(), stockTechnicals.getPrevPrevEma50(), stockTechnicals.getPrevPrevEma100());
                */
        //return this.isDownTrend(stockTechnicals.getEma50(), stockTechnicals.getEma100(), stockTechnicals.getEma200(), stockTechnicals.getPrevEma200(), stockPrice.getClose());
        return stockTechnicals.getEma50() < stockTechnicals.getEma200();
    }

    private boolean isDownTrend(double shortShorterAverage, double shorterAverage, double average, double prevAverage, double close){
        if(shortShorterAverage < shorterAverage && shorterAverage < average){
            if(average < prevAverage && close < average){
                return   Boolean.TRUE;
            }
        }

        return   Boolean.FALSE;
    }

    private boolean isDownTrend(double immediateLow, double average, double immediateHigh, double prevImmediateLow, double prevAverage, double prevImmediateHigh){
        int score = 0;
        if (immediateHigh < prevImmediateHigh) {
            score = score + 1;
        }
        if (average < prevAverage) {
            score = score + 1;
        }
        if (immediateLow < prevImmediateLow) {
            score = score + 1;
        }
        return  score >= 2 ? Boolean.TRUE : Boolean.FALSE;

    }

    @Override
    public Momentum detect(Stock stock) {

        StockTechnicals stockTechnicals = stock.getTechnicals();

        StockPrice stockPrice = stock.getStockPrice();

        if(stockTechnicals==null || stockPrice==null){
            return Momentum.SIDEWAYS;
        }

        boolean shortTermDown = this.isShortTermDownTrend(stockTechnicals, stockPrice);
        boolean shortTermUp = this.isShortTermUpTrend(stockTechnicals, stockPrice);

        boolean mediumTermDown = this.isMediumTermDownTrend(stockTechnicals, stockPrice);
        boolean mediumTermUp = this.isMediumTermUpTrend(stockTechnicals, stockPrice);

        boolean longTermDown = this.isLongTermDownTrend(stockTechnicals, stockPrice);
        boolean longTermUp = this.isLongTermUpTrend(stockTechnicals, stockPrice);

        // 1️⃣ Pullback: Short-term downtrend, but medium & long-term uptrend
        if (shortTermDown && mediumTermUp && longTermUp) {
            return Momentum.PULLBACK;
        }

        // 2️⃣ Correction: Short-term & medium-term downtrend, but long-term uptrend
        if (shortTermDown && mediumTermDown && longTermUp) {
            return Momentum.CORRECTION;
        }

        // 3️⃣ Bottom: Strong downtrend across all timeframes
        if (shortTermDown && mediumTermDown && longTermDown) {
            return Momentum.BOTTOM;
        }

        // 4️⃣ Early Recovery: 20 EMA is rising, but 50 EMA & 200 EMA are still bearish
        if (shortTermUp && mediumTermDown && longTermDown) {
            return Momentum.RECOVERY;
        }

        // 5️⃣ Reversal: Short-term & medium-term uptrend, but long-term still bearish
        if (shortTermUp && mediumTermUp && longTermDown) {
            return Momentum.ADVANCE;
        }

        // 6️⃣ Top: All trends turning bullish, possible peak formation
        if (shortTermUp && mediumTermUp && longTermUp) {
            return Momentum.TOP;
        }

        // 7️⃣ Sideways: No clear trend (EMAs are flat or mixed)
        return Momentum.SIDEWAYS;
    }

    @Override
    public Momentum scanBullish(Stock stock) {

        StockTechnicals stockTechnicals = stock.getTechnicals();

        StockPrice stockPrice = stock.getStockPrice();

        if(stockTechnicals==null || stockPrice==null){
            return Momentum.SIDEWAYS;
        }

        if(this.isShortTermDownTrend(stockTechnicals, stockPrice) && this.isMediumTermUpTrend(stockTechnicals, stockPrice) && this.isLongTermUpTrend(stockTechnicals, stockPrice)){
            return Momentum.PULLBACK;
        }

        if(this.isShortTermDownTrend(stockTechnicals, stockPrice) && this.isMediumTermDownTrend(stockTechnicals, stockPrice) && this.isLongTermUpTrend(stockTechnicals, stockPrice)){
            return Momentum.CORRECTION;
        }

        if(this.isShortTermDownTrend(stockTechnicals, stockPrice) && this.isMediumTermDownTrend(stockTechnicals, stockPrice) && this.isLongTermDownTrend(stockTechnicals, stockPrice)){
            return Momentum.BOTTOM;
        }

        return Momentum.SIDEWAYS;
    }

    @Override
    public Momentum scanBearish(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();

        StockPrice stockPrice = stock.getStockPrice();

        if(stockTechnicals==null || stockPrice==null){
            return Momentum.SIDEWAYS;
        }

        if(this.isShortTermUpTrend(stockTechnicals, stockPrice) && this.isMediumTermDownTrend(stockTechnicals, stockPrice) && this.isLongTermDownTrend(stockTechnicals, stockPrice)){
            return Momentum.RECOVERY;
        }

        if(this.isShortTermUpTrend(stockTechnicals, stockPrice) && this.isMediumTermUpTrend(stockTechnicals, stockPrice) && this.isLongTermDownTrend(stockTechnicals, stockPrice)){
            return Momentum.ADVANCE;
        }

        if(this.isShortTermUpTrend(stockTechnicals, stockPrice) && this.isMediumTermUpTrend(stockTechnicals, stockPrice) && this.isLongTermUpTrend(stockTechnicals, stockPrice)){
            return Momentum.TOP;
        }

        return Momentum.SIDEWAYS;
    }


    private boolean isVolumeIncreasing(StockTechnicals stockTechnicals){
        return stockTechnicals.getVolumeAvg20() > stockTechnicals.getVolumeAvg20Prev();
    }

    private boolean isVolumeDecreasing(StockTechnicals stockTechnicals){
        return stockTechnicals.getVolumeAvg20() <= stockTechnicals.getVolumeAvg20Prev();
    }
}
