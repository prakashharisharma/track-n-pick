package com.example.service;

import com.example.model.master.Stock;
import com.example.util.io.model.type.Trend;

public interface MovingAverageSupportResistanceService {

    /**
     *
     * 1. Breakout 50, 100 or 200 EMA
     * 2. Near Support 20, 50, 100, 200
     * Should be executed in a downtrend
     * @param stock
     * @return
     */
    public boolean isBullish(Stock stock, Trend trend);

    /**
     * 1. Breakdown 50, 100, 200
     * 2. Near Resistance 20, 50, 100, 200
     * Should be executed in a uptrend
     * @param stock
     * @return
     */
    public boolean isBearish(Stock stock, Trend trend);
}
