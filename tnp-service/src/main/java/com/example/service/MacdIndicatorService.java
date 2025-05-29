package com.example.service;

import com.example.data.transactional.entities.StockTechnicals;

public interface MacdIndicatorService {
    public boolean isMacdCrossedSignal(StockTechnicals stockTechnicals);

    public boolean isSignalCrossedMacd(StockTechnicals stockTechnicals);

    public boolean isSignalNearHistogram(StockTechnicals stockTechnicals);

    public boolean isHistogramGreen(StockTechnicals stockTechnicals);

    public boolean isHistogramIncreased(StockTechnicals stockTechnicals);

    public boolean isHistogramDecreased(StockTechnicals stockTechnicals);

    public boolean isMacdIncreased(StockTechnicals stockTechnicals);

    public boolean isMacdDecreased(StockTechnicals stockTechnicals);

    public boolean isSignalIncreased(StockTechnicals stockTechnicals);

    public boolean isSignalDecreased(StockTechnicals stockTechnicals);
}
