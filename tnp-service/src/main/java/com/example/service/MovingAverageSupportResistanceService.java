package com.example.service;

import com.example.enhanced.model.stocks.StockPrice;
import com.example.enhanced.model.stocks.StockTechnicals;
import com.example.model.master.Stock;
import com.example.util.io.model.type.Timeframe;
import com.example.util.io.model.type.Trend;

public interface MovingAverageSupportResistanceService {

    public boolean isBreakout(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    public boolean isNearSupport(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    public boolean isBreakdown(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    public boolean isNearResistance(Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
}
