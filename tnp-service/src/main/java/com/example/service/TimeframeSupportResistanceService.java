package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;

public interface TimeframeSupportResistanceService {

    public boolean isBreakout(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    public boolean isNearSupport(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    public boolean isBreakdown(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    public boolean isNearResistance(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
}
