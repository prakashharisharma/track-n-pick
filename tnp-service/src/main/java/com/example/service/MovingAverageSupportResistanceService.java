package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;

public interface MovingAverageSupportResistanceService {

    public boolean isBreakout(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean confirmationCheck);

    public boolean isNearSupport(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean confirmationCheck);

    public boolean isBreakdown(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean confirmationCheck);

    public boolean isNearResistance(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean confirmationCheck);

    public double getValue(Timeframe timeframe, StockTechnicals stockTechnicals);
    public double getPrevValue(Timeframe timeframe, StockTechnicals stockTechnicals);
}
