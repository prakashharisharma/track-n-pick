package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.model.stocks.StockTechnicals;
import com.example.service.CandleStickService;
import com.example.service.SupportResistanceUtilService;
import com.example.util.FibonacciRatio;
import com.example.util.FormulaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SupportResistanceUtilServiceImpl implements SupportResistanceUtilService {

    @Autowired
    private CandleStickService candleStickService;

    @Autowired
    private FormulaService formulaService;


    @Override
    public boolean isNearSupport(double open,double high, double low, double close, double average) {
        if (close > average) {
            if (formulaService.inRange(low, high, average) || formulaService.isEpsilonEqual(low, average, FibonacciRatio.RATIO_161_8)) {
                log.info("Support low: {} close {} above avg: {}",low,  close, average);
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }



    private boolean isLowNearAverage(Stock stock, double open, double high, double low, double close, double average, double immediateLowAverage, boolean isLongEma ){

        if(formulaService.applyPercentChange(immediateLowAverage, FibonacciRatio.RATIO_38_2 * -100) >= average) {
            if (close >= average) {
                if (formulaService.inRange(low, high, average) || formulaService.isEpsilonEqual(low, average, FibonacciRatio.RATIO_161_8)) {
                    return Boolean.TRUE;
                } else if (isLongEma && formulaService.calculateChangePercentage(average, low) <= -1 * FibonacciRatio.RATIO_161_8 * 100) {
                    return Boolean.TRUE;
                }else if (candleStickService.isHigherHigh(stock) && candleStickService.isHigherLow(stock)) {
                    return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE;
    }

    private boolean isLowNearAverage(double currClose, double open,double high, double low, double close, double average,double immediateLowAverage, boolean isLongEma ){

        if(formulaService.applyPercentChange(immediateLowAverage, FibonacciRatio.RATIO_38_2 * -100) >= average) {
            if (currClose >= average) {
                if (formulaService.inRange(low, high, average) || formulaService.isEpsilonEqual(low, average, FibonacciRatio.RATIO_161_8)) {
                    return Boolean.TRUE;
                } else if (isLongEma && formulaService.calculateChangePercentage(average, low) <= -1 * FibonacciRatio.RATIO_161_8 * 100) {
                    return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isNearResistance(Stock stock) {
        StockPrice stockPrice = stock.getStockPrice();
        StockTechnicals stockTechnicals = stock.getTechnicals();

        double open = stockPrice.getOpen();
        double low = stockPrice.getLow();
        double high = stockPrice.getHigh();
        double close = stockPrice.getClose();
        double ema10 = stockTechnicals.getEma10();
        double ema20 = stockTechnicals.getEma20();
        double ema50 = stockTechnicals.getEma50();
        double ema100 = stockTechnicals.getEma100();
        double ema200 = stockTechnicals.getEma200();

        if(this.isHighNearAverage(stock, open, high, low, close, ema200,ema100, Boolean.TRUE) || this.isHighNearAverage(stock, open, high, low, close, ema100,ema50, Boolean.TRUE) || this.isHighNearAverage(stock, open, high, low, close, ema50,ema20, Boolean.FALSE) || this.isHighNearAverage(stock, open, high, low, close, ema20,ema10, Boolean.FALSE)){
            return Boolean.TRUE;
        }

        double currClose = stockPrice.getClose();
        open = stockPrice.getPrevOpen();
        low = stockPrice.getPrevLow();
        high = stockPrice.getPrevHigh();
        close = stockPrice.getPrevClose();
        ema10 = stockTechnicals.getPrevEma10();
        ema20 = stockTechnicals.getPrevEma20();
        ema50 = stockTechnicals.getPrevEma50();
        ema100 = stockTechnicals.getPrevEma100();
        ema200 = stockTechnicals.getPrevEma200();

        return this.isHighNearAverage(currClose, open, high, low, close, ema200,ema100, Boolean.TRUE) ||  this.isHighNearAverage(currClose, open, high, low, close, ema100,ema50, Boolean.TRUE) ||  this.isHighNearAverage(currClose, open, high, low, close, ema50,ema20, Boolean.FALSE) || this.isHighNearAverage(currClose, open, high, low, close, ema20,ema10, Boolean.FALSE);

    }

    @Override
    public boolean isNearResistance(double open, double high, double low, double close, double average) {
        if (close < average) {
            if (formulaService.inRange(low, high, average) || formulaService.isEpsilonEqual(high, average, FibonacciRatio.RATIO_161_8)) {
                log.info("Resistance high: {} close: {} below avg: {}", high, close, average);
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    private boolean isHighNearAverage(Stock stock, double open,double high, double low, double close, double average,double immediateLowAverage, boolean isLongEma ){
        if(formulaService.applyPercentChange(immediateLowAverage, FibonacciRatio.RATIO_38_2 * 100) <= average) {
            if (close <= average) {
                if (formulaService.inRange(low, high, average) || formulaService.isEpsilonEqual(high, average, FibonacciRatio.RATIO_161_8)) {
                    return Boolean.TRUE;
                }else if (candleStickService.isLowerHigh(stock) && candleStickService.isLowerLow(stock)) {
                    return Boolean.TRUE;
                }
            } else if (!isLongEma && formulaService.calculateChangePercentage(average, high) >= FibonacciRatio.RATIO_161_8 * 100) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    private boolean isHighNearAverage(double currClose, double open,double high, double low, double close, double average,double immediateLowAverage, boolean isLongEma ){
        if(formulaService.applyPercentChange(immediateLowAverage, FibonacciRatio.RATIO_38_2 * 100) <= average) {
            if (currClose <= average) {
                if (formulaService.inRange(low, high, average) || formulaService.isEpsilonEqual(high, average, FibonacciRatio.RATIO_161_8)) {
                    return Boolean.TRUE;
                }
            } else if (!isLongEma && formulaService.calculateChangePercentage(average, high) >= FibonacciRatio.RATIO_161_8 * 100) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }
}
