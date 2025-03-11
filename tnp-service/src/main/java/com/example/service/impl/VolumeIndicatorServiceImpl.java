package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.model.stocks.StockTechnicals;
import com.example.service.ObvIndicatorService;
import com.example.service.VolumeIndicatorService;
import com.example.service.VolumeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VolumeIndicatorServiceImpl implements VolumeIndicatorService {

    private static double BULLISH_MULTIPLIER = 2.0;
    private static double BEARISH_MULTIPLIER = 1.5;

    @Autowired
    private ObvIndicatorService obvIndicatorService;

    @Autowired
    private VolumeService volumeService;

    @Override
    public boolean isHigh(Stock stock) {


        if(volumeService.isVolumeAboveMonthlyAverage(stock, 2.0) || volumeService.isPreviousSessionVolumeAboveMonthlyAverage(stock, 2.0)){
            log.info("{} volume above monthly average", stock.getNseSymbol());
            return Boolean.TRUE;
        }else if(volumeService.isVolumeAboveWeeklyAverage(stock, 3.0) || volumeService.isPreviousSessionVolumeAboveWeeklyAverage(stock, 3.0)){
            log.info("{} volume above weekly average", stock.getNseSymbol());
            return Boolean.TRUE;
        }else if(volumeService.isVolumeWeeklyAboveMonthlyAverage(stock, 1.5)){
            log.info("{} weekly volume above monthly average", stock.getNseSymbol());
            return Boolean.TRUE;
        }


        /*
        if(volumeService.isVolumeIncreasedDailyMonthly(stock, 2.0, 2.0)){
            //if(volumeService.isVolumeIncreasedDailyMonthly(stock, this.calculateBullishVolumeFactorUsingRsi(rsiIndicatorService.rsi(stock)), this.calculateBullishVolumeFactorUsingRsi(rsiIndicatorService.rsiPreviousSession(stock)))){
            log.info("High volume {}", stock.getNseSymbol());
            return Boolean.TRUE;
        }else if(volumeService.isVolumeIncreasedDailyWeekly(stock, 3.0, 3.0)){
            //if(volumeService.isVolumeIncreasedDailyMonthly(stock, this.calculateBullishVolumeFactorUsingRsi(rsiIndicatorService.rsi(stock)), this.calculateBullishVolumeFactorUsingRsi(rsiIndicatorService.rsiPreviousSession(stock)))){
            log.info("High volume {}", stock.getNseSymbol());
            return Boolean.TRUE;
        }
        else if(volumeService.isVolumeIncreasedWeeklyMonthly(stock, 2.0, 2.0)){
            //else if(volumeService.isVolumeIncreasedWeeklyMonthly(stock, this.calculateBearishVolumeFactorUsingRsi(rsiIndicatorService.rsi(stock)), this.calculateBearishVolumeFactorUsingRsi(rsiIndicatorService.rsiPreviousSession(stock)))){
            log.info("High volume {}", stock.getNseSymbol());
            return Boolean.TRUE;
        }*/

        return Boolean.FALSE;
    }

    @Override
    public boolean isBullish(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();
        return this.isCurrentSessionHigh(stockTechnicals, BULLISH_MULTIPLIER) || this.isPreviousSessionHigh(stockTechnicals, BULLISH_MULTIPLIER);
    }

    @Override
    public boolean isBullish(Stock stock, double multiplier) {
        StockTechnicals stockTechnicals = stock.getTechnicals();
        return this.isCurrentSessionHigh(stockTechnicals, multiplier) || this.isPreviousSessionHigh(stockTechnicals, multiplier);
    }

    @Override
    public boolean isBearish(Stock stock) {
        StockTechnicals stockTechnicals = stock.getTechnicals();
        return this.isCurrentSessionHigh(stockTechnicals, BEARISH_MULTIPLIER) || this.isPreviousSessionHigh(stockTechnicals, BEARISH_MULTIPLIER);
    }

    @Override
    public boolean isBearish(Stock stock, double multiplier) {
        StockTechnicals stockTechnicals = stock.getTechnicals();
        return this.isCurrentSessionHigh(stockTechnicals, multiplier) || this.isPreviousSessionHigh(stockTechnicals, multiplier);
    }

    private boolean isCurrentSessionHigh(StockTechnicals stockTechnicals, double multiplier){

        if(this.isHigher(stockTechnicals.getVolume(), stockTechnicals.getVolumeAvg20(), multiplier)){
            if(this.isIncreasing(stockTechnicals.getVolume(), stockTechnicals.getPrevVolume())){
                if(this.isIncreasing(stockTechnicals.getVolumeAvg20(), stockTechnicals.getPrevVolumeAvg20())){
                    return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE;
    }

    private boolean isPreviousSessionHigh(StockTechnicals stockTechnicals, double multiplier){

        if(this.isHigher(stockTechnicals.getPrevVolume(), stockTechnicals.getPrevVolumeAvg20(), multiplier)){
            if(this.isIncreasing(stockTechnicals.getPrevVolume(), stockTechnicals.getPrevPrevVolume())){
                if(this.isIncreasing(stockTechnicals.getPrevVolumeAvg20(), stockTechnicals.getPrevPrevVolumeAvg20())){
                    return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE;
    }


    private boolean isHigher(long volume, long average, double multiplier){
        return ((average > 0) && volume >= (average * multiplier)) ? Boolean.TRUE : Boolean.FALSE;
    }

    private boolean isIncreasing(long volume, long preVolume){
        return (volume > preVolume) ? Boolean.TRUE : Boolean.FALSE;
    }
}
