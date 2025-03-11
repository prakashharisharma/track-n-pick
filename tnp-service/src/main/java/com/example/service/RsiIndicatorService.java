package com.example.service;

import com.example.model.master.Stock;

public interface RsiIndicatorService {

    public boolean isBullish(Stock stock);
    public boolean isOverSold(Stock stock);
    public boolean isBearish(Stock stock);

    public boolean isOverBaught(Stock stock);



    public double rsi(Stock stock);

    public double rsiPreviousSession(Stock stock);

    public boolean isIncreasing(Stock stock);
    public boolean isDecreasing(Stock stock);
}
