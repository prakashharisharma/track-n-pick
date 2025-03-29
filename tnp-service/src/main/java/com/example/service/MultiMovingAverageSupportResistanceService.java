package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.model.stocks.StockTechnicals;


public interface MultiMovingAverageSupportResistanceService {


    public boolean isBreakout(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    public boolean isNearSupport(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);


    public boolean isBreakdown(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    public boolean isNearResistance(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
}
