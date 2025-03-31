package com.example.service;


import com.example.data.transactional.entities.StockTechnicals;

/**
 * Service interface for RSI (Relative Strength Index) analysis.
 * Provides methods to determine bullish/bearish conditions, overbought/oversold zones,
 * and RSI trend movements.
 */
public interface RsiIndicatorService {

    /**
     * Determines if the stock is in a bullish RSI condition.
     * A stock is considered bullish if the RSI is increasing
     * and crosses above the oversold threshold.
     *
     * @param stockTechnicals The stock's technical data.
     * @return {@code true} if RSI indicates bullishness, otherwise {@code false}.
     */
    public boolean isBullish(StockTechnicals stockTechnicals);

    /**
     * Checks if the stock is in an oversold condition.
     * An oversold condition occurs when RSI is below or equal to the oversold threshold.
     *
     * @param stockTechnicals The stock's technical data.
     * @return {@code true} if RSI is in the oversold zone, otherwise {@code false}.
     */
    public boolean isOverSold(StockTechnicals stockTechnicals);

    /**
     * Determines if the stock is in a bearish RSI condition.
     * A stock is considered bearish if the RSI is decreasing
     * and crosses below the overbought threshold.
     *
     * @param stockTechnicals The stock's technical data.
     * @return {@code true} if RSI indicates bearishness, otherwise {@code false}.
     */
    public boolean isBearish(StockTechnicals stockTechnicals);

    /**
     * Checks if the stock is in an overbought condition.
     * An overbought condition occurs when RSI is above or equal to the overbought threshold.
     *
     * @param stockTechnicals The stock's technical data.
     * @return {@code true} if RSI is in the overbought zone, otherwise {@code false}.
     */
    public boolean isOverBought(StockTechnicals stockTechnicals);

    /**
     * Calculates the RSI (Relative Strength Index) for the given stock.
     *
     * @param stockTechnicals The stock's technical data.
     * @return The calculated RSI value.
     */
    public double rsi(StockTechnicals stockTechnicals);

    /**
     * Determines if the RSI is increasing compared to the previous value.
     *
     * @param stockTechnicals The stock's technical data.
     * @return {@code true} if RSI is increasing, otherwise {@code false}.
     */
    public boolean isIncreasing(StockTechnicals stockTechnicals);

    /**
     * Determines if the RSI is decreasing compared to the previous value.
     *
     * @param stockTechnicals The stock's technical data.
     * @return {@code true} if RSI is decreasing, otherwise {@code false}.
     */
    public boolean isDecreasing(StockTechnicals stockTechnicals);
}
