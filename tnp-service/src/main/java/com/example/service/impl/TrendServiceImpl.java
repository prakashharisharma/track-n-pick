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
    public Trend analyze(Stock stock) {

        if(candleStickService.isHigherHigh(stock) && candleStickService.isHigherLow(stock)){
            return Trend.UP;
        }else if(candleStickService.isLowerHigh(stock) && candleStickService.isLowerLow(stock)){
            return Trend.DOWN;
        }

        return stock.getTechnicals().getTrend();
    }

    @Override
    public Trend isUpTrend(Stock stock) {

        StockTechnicals stockTechnicals= stock.getTechnicals();

        StockPrice stockPrice= stock.getStockPrice();

        if(stockTechnicals==null || stockPrice==null){
            return Trend.INVALID;
        }

        if(this.isLongTermUpTrend(stockTechnicals, stockPrice)){
            return Trend.LONG;
        }else if(this.isMediumTermUpTrend(stockTechnicals, stockPrice)){
            return Trend.MEDIUM;
        }else if(this.isShortTermUpTrend(stockTechnicals, stockPrice)){
            return Trend.SHORT;
        }

        return  Trend.INVALID;
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
        return this.isUpTrend(stockTechnicals.getEma5(), stockTechnicals.getEma10(), stockTechnicals.getEma20(), stockTechnicals.getPrevPrevEma20(), stockPrice.getClose());

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
        return this.isUpTrend(stockTechnicals.getEma10(), stockTechnicals.getEma50(), stockTechnicals.getEma100(), stockTechnicals.getPrevPrevEma100(), stockPrice.getClose());

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
        return this.isUpTrend(stockTechnicals.getEma50(), stockTechnicals.getEma100(), stockTechnicals.getEma200(), stockTechnicals.getPrevPrevEma200(), stockPrice.getClose());

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

    private boolean isUpTrend(double shortShorterAverage, double shorterAverage, double average, double prevPrevAverage, double close){
        if(shortShorterAverage > shorterAverage && shorterAverage > average){
            if(average > prevPrevAverage && close > average){
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
            return Trend.INVALID;
        }

        if(this.isShortTermDownTrend(stockTechnicals, stockPrice)){
            return Trend.SHORT;
        }else if(this.isLongTermDownTrend(stockTechnicals, stockPrice)){
            return Trend.LONG;
        }else if(this.isMediumTermDownTrend(stockTechnicals, stockPrice)){
            return Trend.MEDIUM;
        }

        return  Trend.INVALID;
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
        return this.isDownTrend(stockTechnicals.getEma5(), stockTechnicals.getEma10(), stockTechnicals.getEma20(), stockTechnicals.getPrevPrevEma20(), stockPrice.getClose());

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
        return this.isDownTrend(stockTechnicals.getEma20(), stockTechnicals.getEma50(), stockTechnicals.getEma100(), stockTechnicals.getPrevPrevEma100(), stockPrice.getClose());
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
        return this.isDownTrend(stockTechnicals.getEma50(), stockTechnicals.getEma100(), stockTechnicals.getEma200(), stockTechnicals.getPrevPrevEma200(), stockPrice.getClose());

    }

    private boolean isDownTrend(double shortShorterAverage, double shorterAverage, double average, double prevPrevAverage, double close){
        if(shortShorterAverage < shorterAverage && shorterAverage < average){
            if(average < prevPrevAverage && close < average){
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
            return Momentum.PUSHBACK;
        }

        if(this.isShortTermUpTrend(stockTechnicals, stockPrice) && this.isMediumTermUpTrend(stockTechnicals, stockPrice) && this.isLongTermDownTrend(stockTechnicals, stockPrice)){
            return Momentum.ADVANCE;
        }

        if(this.isShortTermUpTrend(stockTechnicals, stockPrice) && this.isMediumTermUpTrend(stockTechnicals, stockPrice) && this.isLongTermUpTrend(stockTechnicals, stockPrice)){
            return Momentum.TOP;
        }

        return Momentum.SIDEWAYS;
    }

    @Override
    public int strength(Stock stock) {

        int strength = 0;
        StockTechnicals stockTechnicals = stock.getTechnicals();

        if(this.isUpTrend(stock) !=Trend.INVALID && this.isVolumeIncreasing(stockTechnicals)){
            strength = 1;
        }else if(this.isDownTrend(stock) !=Trend.INVALID && this.isVolumeIncreasing(stockTechnicals)){
            strength = 1;
        }else if(this.isUpTrend(stock) !=Trend.INVALID && this.isVolumeDecreasing(stockTechnicals)){
            strength = 0;
        }else if(this.isDownTrend(stock ) !=Trend.INVALID && this.isVolumeDecreasing(stockTechnicals)){
            strength = 0;
        }

        return strength;
    }

    private boolean isVolumeIncreasing(StockTechnicals stockTechnicals){
        return stockTechnicals.getVolumeAvg20() > stockTechnicals.getVolumeAvg20Prev();
    }

    private boolean isVolumeDecreasing(StockTechnicals stockTechnicals){
        return stockTechnicals.getVolumeAvg20() <= stockTechnicals.getVolumeAvg20Prev();
    }
}
