package com.example.service;

import com.example.model.master.Stock;

public interface VolumeIndicatorService {

    public boolean isHigh(Stock stock);

    public boolean isBullish(Stock stock);

    public boolean isBullish(Stock stock, double multiplier);
    public boolean isBearish(Stock stock);

    public boolean isBearish(Stock stock, double multiplier);
}
