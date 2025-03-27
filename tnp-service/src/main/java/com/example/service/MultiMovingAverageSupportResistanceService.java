package com.example.service;

import com.example.enhanced.model.stocks.StockPrice;
import com.example.enhanced.model.stocks.StockTechnicals;
import com.example.model.master.Stock;
import com.example.util.io.model.type.Timeframe;
import com.example.util.io.model.type.Trend;

public interface MultiMovingAverageSupportResistanceService {


    public boolean isBreakout(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    public boolean isNearSupport(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);


    public boolean isBreakdown(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    public boolean isNearResistance(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
}
