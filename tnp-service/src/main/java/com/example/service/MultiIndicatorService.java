package com.example.service;

import com.example.enhanced.model.stocks.StockTechnicals;
import com.example.model.master.Stock;
import com.example.util.io.model.type.Timeframe;

/**
 *
 * Service interface for multi-indicator analysis.
 * Provides methods to determine whether a stock is in a bullish or bearish trend
 * based on various technical indicators.
 *
 * @author phs
 */
public interface MultiIndicatorService {

    /**
     * Determines if the given stock exhibits a bullish trend based on multiple indicators.
     * Typically considers factors like:
     * - RSI increasing (indicating growing strength)
     * - MACD histogram increasing (showing bullish momentum)
     * - ADX confirming a strong trend
     *
     * @param stockTechnicals The technical data of the stock.
     * @return {@code true} if the stock is in a bullish trend, otherwise {@code false}.
     */
    public boolean isBullish(StockTechnicals stockTechnicals);

    /**
     * Determines if the given stock exhibits a bearish trend based on multiple indicators.
     * Typically considers factors like:
     * - RSI decreasing (indicating weakening strength)
     * - MACD histogram decreasing (showing bearish momentum)
     * - ADX confirming a strong downward trend
     *
     * @param stockTechnicals The technical data of the stock.
     * @return {@code true} if the stock is in a bearish trend, otherwise {@code false}.
     */
    public boolean isBearish(StockTechnicals stockTechnicals);
}
