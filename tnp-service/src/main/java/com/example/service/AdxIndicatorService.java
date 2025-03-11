package com.example.service;

import com.example.model.master.Stock;

public interface AdxIndicatorService {

    public static final double ADX_BULLISH_MIN = 20.0;
    public static final double ADX_BULLISH_MAX = 30.0;

    public static final double ADX_BEARISH_MIN = 20.0;
    public static final double ADX_BEARISH_MAX = 40.0;

    public double adx(Stock stock);
    public boolean isAdxIncreasing(Stock stock);
    public boolean isAdxDecreasing(Stock stock);
    public boolean isPlusDiIncreasing(Stock stock);

    public boolean isMinusDiIncreasing(Stock stock);

    public boolean isPlusDiDecreasing(Stock stock);

    public boolean isMinusDiDecreasing(Stock stock);

    public boolean isDmiConvergence(Stock stock);

    public boolean isDmiDivergence(Stock stock);
    public boolean isBullish(Stock stock);
    public boolean isBearish(Stock stock);
}
