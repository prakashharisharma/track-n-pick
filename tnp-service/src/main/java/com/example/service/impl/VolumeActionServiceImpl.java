package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.model.stocks.StockTechnicals;
import com.example.service.VolumeActionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VolumeActionServiceImpl implements VolumeActionService {

    private static final int WEEKLY_FACTOR = 5;
    private static final int MONTHLY_FACTOR = 3;

    @Override
    public boolean isVolumeAboveWeeklyAverage(Stock stock) {

        StockTechnicals stockTechnicals  = stock.getTechnicals();

        if(stockTechnicals.getVolume() >= (stockTechnicals.getWeeklyVolume() * WEEKLY_FACTOR)){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isVolumeAbovePreviousDay(Stock stock) {

        StockTechnicals stockTechnicals  = stock.getTechnicals();

        if(stockTechnicals.getVolume() >= stockTechnicals.getPrevVolume()){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isVolumeAboveMonthlyAverage(Stock stock) {

        StockTechnicals stockTechnicals  = stock.getTechnicals();

        if(stockTechnicals.getVolume() >= (stockTechnicals.getMonthlyVolume() * MONTHLY_FACTOR)){
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }
}
