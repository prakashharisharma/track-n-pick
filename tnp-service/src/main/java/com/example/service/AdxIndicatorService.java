package com.example.service;

import com.example.data.transactional.entities.StockTechnicals;

public interface AdxIndicatorService {

    public static final double ADX_BULLISH_MIN = 20.0;
    public static final double ADX_BULLISH_MAX = 30.0;

    public static final double ADX_BEARISH_MIN = 20.0;
    public static final double ADX_BEARISH_MAX = 40.0;

    public double adx(StockTechnicals stockTechnicals);

    public boolean isAdxIncreasing(StockTechnicals stockTechnicals);

    public boolean isAdxDecreasing(StockTechnicals stockTechnicals);

    public boolean isPlusDiIncreasing(StockTechnicals stockTechnicals);

    public boolean isMinusDiIncreasing(StockTechnicals stockTechnicals);

    public boolean isPlusDiDecreasing(StockTechnicals stockTechnicals);

    public boolean isMinusDiDecreasing(StockTechnicals stockTechnicals);

    public boolean isDmiConvergence(StockTechnicals stockTechnicals);

    public boolean isDmiDivergence(StockTechnicals stockTechnicals);

    public boolean isBullish(StockTechnicals stockTechnicals);

    public boolean isBearish(StockTechnicals stockTechnicals);
}
