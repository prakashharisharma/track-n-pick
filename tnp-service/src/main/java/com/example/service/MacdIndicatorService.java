package com.example.service;

import com.example.transactional.model.stocks.StockTechnicals;

public interface MacdIndicatorService {
    public boolean isMacdCrossedSignal(StockTechnicals stockTechnicals);

    public boolean isSignalNearHistogram(StockTechnicals stockTechnicals);

    public boolean isHistogramGreen(StockTechnicals stockTechnicals);

    public boolean isHistogramIncreased(StockTechnicals stockTechnicals);

    public boolean isHistogramDecreased(StockTechnicals stockTechnicals);
}
