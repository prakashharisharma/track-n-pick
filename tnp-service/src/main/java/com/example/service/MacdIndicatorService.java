package com.example.service;

import com.example.enhanced.model.stocks.StockTechnicals;
import com.example.model.master.Stock;
import com.example.util.io.model.type.Timeframe;

public interface MacdIndicatorService {
    public boolean isMacdCrossedSignal(StockTechnicals stockTechnicals);

    public boolean isSignalNearHistogram(StockTechnicals stockTechnicals);

    public boolean isHistogramGreen(StockTechnicals stockTechnicals);

    public boolean isHistogramIncreased(StockTechnicals stockTechnicals);

    public boolean isHistogramDecreased(StockTechnicals stockTechnicals);
}
