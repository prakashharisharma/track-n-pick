package com.example.service;

import com.example.dto.OHLCV;
import com.example.model.master.Stock;

public interface DailySupportResistanceService {

    /**
     *
     * 1. Breakout monthly resistance
     * 2. Near support
     * Should be executed in a downtrend
     * @param stock
     * @return
     */
    public boolean isBullish(Stock stock);

    public boolean isBreakout(Stock stock);

    public boolean isNearSupport(Stock stock);

    /**
     * 1. Breakdown monthly support
     * 2. Near resistance
     * Should be executed in a uptrend
     * @param stock
     * @return
     */
    public boolean isBearish(Stock stock);

    public boolean isBreakdown(Stock stock);

    public boolean isNearResistance(Stock stock);


    public OHLCV supportAndResistance(Stock stock);
}
