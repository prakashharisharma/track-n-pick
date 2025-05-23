package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;

/**
 * Service interface for analyzing volume-based indicators to determine bullish or bearish trends.
 * This service helps identify significant volume changes that could indicate trend reversals,
 * breakouts, or confirmations of existing trends.
 *
 * @author phs
 */
public interface VolumeIndicatorService {

    public boolean isBullish(
            StockPrice stockPrice, StockTechnicals stockTechnicals, Timeframe timeframe);

    public boolean isBullish(
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            Timeframe timeframe,
            double multiplier);

    public boolean isBearish(
            StockPrice stockPrice, StockTechnicals stockTechnicals, Timeframe timeframe);

    public boolean isBearish(
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            Timeframe timeframe,
            double multiplier);

    public boolean isBullish(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals, int days);

    public boolean isBearish(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals, int days);

    public boolean isTradingValueSufficient(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
}
