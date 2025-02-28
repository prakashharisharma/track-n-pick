package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.service.AdxIndicatorService;
import com.example.service.BreakoutConfirmationService;
import com.example.service.CandleStickService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BreakoutConfirmationServiceImpl implements BreakoutConfirmationService {

    @Autowired
    private CandleStickService candleStickService;
    @Autowired
    private AdxIndicatorService adxIndicatorService;

    @Override
    public boolean isBullishFollowup(Stock stock, double average) {
        if(stock.getStockPrice().getOpen() > average) {
            if (candleStickService.isGreen(stock)) {
                if (candleStickService.isHigherHigh(stock)) {
                    StockPrice stockPrice = stock.getStockPrice();
                    if(stockPrice.getClose() > stockPrice.getPrevClose()){
                        return Boolean.TRUE;
                    }else if(candleStickService.lowerWickSize(stock) > candleStickService.upperWickSize(stock) * 2){
                        return Boolean.TRUE;
                    }
                }
            }
        }else if(stock.getStockPrice().getClose() > average){
            if(adxIndicatorService.isDmiDivergence(stock)){
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isBullishConfirmation(Stock stock, double average) {
        if(candleStickService.isGapUp(stock)){
            StockPrice stockPrice = stock.getStockPrice();
            if(stockPrice.getOpen() > stockPrice.getPrevHigh() ){
                return Boolean.TRUE;
            }else if(stockPrice.getPrevHigh() < average){
                return Boolean.TRUE;
            }
        }else if(adxIndicatorService.isDmiDivergence(stock)){
            return Boolean.TRUE;
        }else if(this.isBodyMidHigherThanAverage(stock, average)){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    private boolean isBodyMidHigherThanAverage(Stock stock, double average){

        StockPrice stockPrice = stock.getStockPrice();
        double mid = (stockPrice.getOpen() + stockPrice.getClose())/2;
        if(mid > average){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }


    @Override
    public boolean isBearishFollowup(Stock stock, double average) {
        if(stock.getStockPrice().getOpen() < average) {
            if (candleStickService.isRed(stock)) {
                if (candleStickService.isLowerLow(stock)) {
                    StockPrice stockPrice = stock.getStockPrice();
                    if(stockPrice.getClose() < stockPrice.getPrevClose()){
                        return Boolean.TRUE;
                    }else if(candleStickService.upperWickSize(stock) * 2 > candleStickService.lowerWickSize(stock)){
                        return Boolean.TRUE;
                    }
                }
            }
        }else if(stock.getStockPrice().getClose() < average){
            if(adxIndicatorService.isDmiConvergence(stock)){
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isBearishConfirmation(Stock stock, double average) {
        if(candleStickService.isGapDown(stock)){
            StockPrice stockPrice = stock.getStockPrice();
            if(stockPrice.getOpen() < stockPrice.getPrevLow()){
                return Boolean.TRUE;
            }else if(stockPrice.getPrevLow() > average){
                return Boolean.TRUE;
            }
        }else if(adxIndicatorService.isDmiConvergence(stock)){
            return Boolean.TRUE;
        }else if(this.isBodyMidLowerThanAverage(stock, average)){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    private boolean isBodyMidLowerThanAverage(Stock stock, double average){

        StockPrice stockPrice = stock.getStockPrice();
        double mid = (stockPrice.getOpen() + stockPrice.getClose())/2;
        if(mid < average){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }
}
