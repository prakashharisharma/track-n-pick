package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.StockTechnicals;

/**
 * Service interface for analyzing volume-based indicators to determine bullish or bearish trends.
 * This service helps identify significant volume changes that could indicate trend reversals,
 * breakouts, or confirmations of existing trends.
 *
 * @author phs
 */
public interface VolumeIndicatorService {

    /**
     * Determines if the stock is showing bullish volume characteristics based on historical trends.
     * This method evaluates whether the volume is increasing in a way that supports a bullish
     * trend.
     *
     * @param stockTechnicals The stock's technical data, including volume and moving averages.
     * @param timeframe The timeframe (Daily, Weekly, Monthly) to analyze the volume pattern.
     * @return {@code true} if the volume pattern supports a bullish trend, otherwise {@code false}.
     */
    public boolean isBullish(StockTechnicals stockTechnicals, Timeframe timeframe);

    public boolean isBullish(
            StockTechnicals stockTechnicals, Timeframe timeframe, double multiplier);

    /**
     * Determines if the stock is showing bearish volume characteristics based on historical trends.
     * This method evaluates whether the volume is decreasing or behaving in a way that supports a
     * bearish trend.
     *
     * @param stockTechnicals The stock's technical data, including volume and moving averages.
     * @param timeframe The timeframe (Daily, Weekly, Monthly) to analyze the volume pattern.
     * @return {@code true} if the volume pattern supports a bearish trend, otherwise {@code false}.
     */
    public boolean isBearish(StockTechnicals stockTechnicals, Timeframe timeframe);

    public boolean isBearish(
            StockTechnicals stockTechnicals, Timeframe timeframe, double multiplier);

    public boolean isBullish(Timeframe timeframe, StockTechnicals stockTechnicals, int days);

    public boolean isBearish(Timeframe timeframe, StockTechnicals stockTechnicals, int days);
}
