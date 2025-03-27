package com.example.service;

import com.example.enhanced.model.stocks.StockTechnicals;
import com.example.model.master.Stock;
import com.example.util.io.model.type.Timeframe;

public interface ObvIndicatorService {

    public boolean isBullish(StockTechnicals stockTechnicals);

    public boolean isBearish(StockTechnicals stockTechnicals);

    public boolean isObvIncreasing(StockTechnicals stockTechnicals);
    public boolean isObvDecreasing(StockTechnicals stockTechnicals);

    public boolean isObvAvgIncreasing(StockTechnicals stockTechnicals);
    public boolean isObvAvgDecreasing(StockTechnicals stockTechnicals);
}
