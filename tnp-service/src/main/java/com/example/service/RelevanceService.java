package com.example.service;

import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.model.stocks.StockTechnicals;
import com.example.util.io.model.type.Timeframe;
import com.example.util.io.model.type.Trend;

public interface RelevanceService {
    public boolean isBullishTimeFrame(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals, double minimumScore);
    public boolean isBullishMovingAverage(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals, double minimumScore);
    public boolean isBullishIndicator(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
    public boolean isBearishTimeFrame(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals, double minimumScore);
    public boolean isBearishMovingAverage(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals, double minimumScore);
    public boolean isBearishIndicator(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
}
