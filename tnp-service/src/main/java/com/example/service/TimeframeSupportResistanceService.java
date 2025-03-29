package com.example.service;

import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.model.stocks.StockTechnicals;
import com.example.util.io.model.type.Timeframe;
import com.example.util.io.model.type.Trend;


/**
 * Service interface for determining support and resistance levels
 * across multiple timeframes and assessing bullish or bearish conditions.
 * @author phs
 */
public interface TimeframeSupportResistanceService {



    /**
     * Checks if a breakout has occurred at the given timeframe.
     * A breakout occurs when the stock price moves above a resistance level.
     *
     * @param stock     The stock being analyzed.
     * @param timeframe The timeframe for analysis.
     * @param trend     The overall trend of the stock.
     * @return true if a breakout is detected, false otherwise.
     */
    public boolean isBreakout(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    /**
     * Determines if the stock is near a key support level.
     * A stock is considered near support if it is close to a historically significant price level.
     *
     * @param stock     The stock being analyzed.
     * @param timeframe The timeframe for analysis.
     * @param trend     The overall trend of the stock.
     * @return true if the stock is near a support level, false otherwise.
     */
    public boolean isNearSupport(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);


    /**
     * Checks if a breakdown has occurred at the given timeframe.
     * A breakdown occurs when the stock price falls below a support level.
     *
     * @param stock     The stock being analyzed.
     * @param timeframe The timeframe for analysis.
     * @param trend     The overall trend of the stock.
     * @return true if a breakdown is detected, false otherwise.
     */
    public boolean isBreakdown(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);

    /**
     * Determines if the stock is near a key resistance level.
     * A stock is considered near resistance if it is close to a historically significant price level.
     *
     * @param stock     The stock being analyzed.
     * @param timeframe The timeframe for analysis.
     * @param trend     The overall trend of the stock.
     * @return true if the stock is near a resistance level, false otherwise.
     */
    public boolean isNearResistance(Trend trend, Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals);
}
