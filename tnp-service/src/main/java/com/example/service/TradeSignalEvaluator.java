package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.TradeSetup;

/**
 * TradeSignalEvaluator defines the contract for evaluating trade signals such as entry and exit
 * points based on different trading strategies.
 *
 * <p>Implementations of this interface may use various techniques like price action analysis,
 * technical indicators, volume patterns, or other custom logic to generate trading signals.
 *
 * <p>This interface allows for a pluggable architecture where multiple strategies can be evaluated
 * consistently via a common contract.
 *
 * <p><strong>Example Implementations:</strong>
 *
 * <ul>
 *   <li>{@code PriceActionSignalEvaluator} – based on candlestick and price patterns
 *   <li>{@code IndicatorSignalEvaluator} – based on moving averages, RSI, MACD, etc.
 *   <li>{@code VolumeSignalEvaluator} – based on volume surges or divergences
 * </ul>
 *
 * @author Prakash Hari Sharma
 * @version 1.0
 * @since 1.0
 * @copyright Copyright (c) 2025 equityseer.com. All rights reserved.
 */
public interface TradeSignalEvaluator {

    /**
     * Evaluates whether a trade entry signal is present for the given stock and timeframe.
     *
     * @param timeframe the timeframe to evaluate (e.g., DAILY, WEEKLY)
     * @param stock the stock to evaluate
     * @param stockPrice the current stock price information
     * @param stockTechnicals the derived technical indicators for the stock
     * @return a {@link TradeSetup} containing the result of the entry evaluation, or a disabled
     *     setup if no valid entry condition is detected
     */
    TradeSetup evaluateEntry(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals);

    /**
     * Evaluates whether a trade exit signal is present for the given stock and timeframe.
     *
     * @param timeframe the timeframe to evaluate
     * @param stock the stock to evaluate
     * @param stockPrice the current stock price information
     * @param stockTechnicals the derived technical indicators for the stock
     * @return a {@link TradeSetup} containing the result of the exit evaluation, or a disabled
     *     setup if no valid exit condition is detected
     */
    TradeSetup evaluateExit(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals);
}
