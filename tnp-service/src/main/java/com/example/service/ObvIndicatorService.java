package com.example.service;

import com.example.model.master.Stock;

public interface ObvIndicatorService {

    public boolean isBullish(Stock stock);

    public boolean isBearish(Stock stock);

    public boolean isObvIncreasing(Stock stock);
    public boolean isObvDecreasing(Stock stock);

    public boolean isObvAvgIncreasing(Stock stock);
    public boolean isObvAvgDecreasing(Stock stock);
}
