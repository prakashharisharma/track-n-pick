package com.example.service;

import com.example.model.master.Stock;
import com.example.util.io.model.type.Trend;

/**
 * Works in respect of Monthly, Weekly and Daily
 *
 * Primary Trend - Monthly
 * Secondary Trend - Weekly
 * Minor Trend - Daily
 *
 */
public interface TimeframeSupportResistanceService {
    /**
     *
     * 1. Breakout monthly and weekly resistance
     * 1.1. Breakout can be confirmed using PlusDi increasing, MinusDi decreasing and ADX increasing
     * 2. Near support monthly and weekly
     * 2.1 Support can be confirmed using RSI
     * a. Check for prior 2 weeks candle and see if bullish
     * b. Check for daily candle and see if bullish
     * Should be executed in a downtrend
     * @param stock
     * @return
     */
    public boolean isBullish(Stock stock, Trend trend);

    /**
     * 1. Breakdown monthly support and weekly
     * 1.1. Breakdown can be confirmed using MinusDi increasing, PlusDi decreasing and ADX increasing
     * 2. Near resistance monthly and weekly
     * 2.1 Resistance can be confirmed using RSI
     * a. Check for prior 2 Weeks candle and see if bearish
     * b. Check for daily candle and see if bearish
     * Should be executed in a uptrend
     * @param stock
     * @return
     */
    public boolean isBearish(Stock stock, Trend trend);
}
