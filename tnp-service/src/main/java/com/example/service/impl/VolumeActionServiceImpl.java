package com.example.service.impl;

import com.example.model.ledger.BreakoutLedger;
import com.example.model.master.Stock;
import com.example.model.stocks.StockTechnicals;
import com.example.service.BreakoutLedgerService;
import com.example.service.VolumeActionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VolumeActionServiceImpl implements VolumeActionService {

    private static final int WEEKLY_FACTOR = 5;
    private static final int MONTHLY_FACTOR = 3;

    private static long MIN_VOLUME = 500000;

    private static double MIN_VALUE = 1000.0;
    @Autowired
    private BreakoutLedgerService breakoutLedgerService;

    @Override
    public boolean isHighVolumeAboveWeeklyAverage(Stock stock) {

        boolean isAboveAverage = Boolean.FALSE;

        StockTechnicals stockTechnicals  = stock.getTechnicals();

        /*
        if(stockTechnicals.getWeeklyVolume() >= 4000000 &&  (stockTechnicals.getVolume() > (stockTechnicals.getWeeklyVolume() * 2))
        ){
            isAboveAverage =  Boolean.TRUE;
        }else if(stockTechnicals.getWeeklyVolume() >= 2000000 &&  (stockTechnicals.getVolume() > (stockTechnicals.getWeeklyVolume() * 3))
        ){
            isAboveAverage =  Boolean.TRUE;
        }else if(stockTechnicals.getWeeklyVolume() >= 1000000 &&  (stockTechnicals.getVolume() > (stockTechnicals.getWeeklyVolume() * 4))
        ){
            isAboveAverage =  Boolean.TRUE;
        }else if(stockTechnicals.getWeeklyVolume() >= 500000 &&(stockTechnicals.getVolume() > (stockTechnicals.getWeeklyVolume() * 5))
        ){
                isAboveAverage = Boolean.TRUE;
        }else
         */
        if((stockTechnicals.getVolume() > (stockTechnicals.getWeeklyVolume() * 10))){
            isAboveAverage = Boolean.TRUE;
        }else if((stockTechnicals.getVolume() > (stockTechnicals.getWeeklyVolume() * 5))){
                if(this.isLiquidityInVolume(stock)) {
                    isAboveAverage = Boolean.TRUE;
            }
        }

        if(isAboveAverage){
            this.createLedgerEntry(stock);
        }

        return isAboveAverage;
    }

    @Override
    public boolean isVolumeAboveWeeklyAverage(Stock stock) {
        StockTechnicals stockTechnicals  = stock.getTechnicals();
        if((stockTechnicals.getVolume() > stockTechnicals.getWeeklyVolume() * 2 )){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public boolean isVolumeAbovePreviousDay(Stock stock) {

        boolean isAboveAverage = Boolean.FALSE;
        StockTechnicals stockTechnicals  = stock.getTechnicals();

        if(stockTechnicals.getVolume() > stockTechnicals.getPrevVolume() * 1.5){
            //if(this.isLiquidityInVolume(stock)) {
                return Boolean.TRUE;
            //}
        }

        return Boolean.FALSE;
    }

    private void createLedgerEntry(Stock stock){
        breakoutLedgerService.addPositive(stock, BreakoutLedger.BreakoutCategory.VOLUME_HIGH);
    }

    @Override
    public boolean isVolumeAboveMonthlyAverage(Stock stock) {

        StockTechnicals stockTechnicals  = stock.getTechnicals();

        if(stockTechnicals.getVolume() >= (stockTechnicals.getMonthlyVolume() * MONTHLY_FACTOR)){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isLiquidityInVolume(Stock stock) {

        StockTechnicals stockTechnicals  = stock.getTechnicals();

        if((stockTechnicals.getVolume()) >= MIN_VOLUME){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    private double getPriceFactor(Stock stock){

        double factor = MIN_VALUE / stock.getStockPrice().getClose();

        if(factor <= 1.0){
            return 1.0;
        }

        return factor;
    }
}
