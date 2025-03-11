package com.example.service;

import com.example.model.master.Stock;
import com.example.util.io.model.type.Trend;

public interface LongTermMovingAverageSupportResistanceService {

    /**
     *
     * 1. Breakout 50, 100 or 200 EMA
     * 2. Near Support 20, 50, 100, 200
     * Should be executed in a downtrend
     * @param stock
     * @return
     */
    public boolean isBullish(Stock stock);

    public boolean isBreakout(Stock stock);

    public boolean isNearSupport(Stock stock);

    /**
     * 1. Breakdown 50, 100, 200
     * 2. Near Resistance 20, 50, 100, 200
     * Should be executed in a uptrend
     * @param stock
     * @return
     */
    public boolean isBearish(Stock stock);

    public boolean isBreakdown(Stock stock);

    public boolean isNearResistance(Stock stock);
}
