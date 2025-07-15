package com.example.service.impl;

import com.example.data.transactional.entities.StockPrice;
import com.example.service.CandleStickService;
import com.example.service.SupportResistanceUtilService;
import com.example.util.FormulaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SupportResistanceUtilServiceImpl implements SupportResistanceUtilService {

    @Autowired private CandleStickService candleStickService;

    @Autowired private FormulaService formulaService;

    /**
     * Determines if the stock is currently near a support level based on price behavior over the
     * last three trading sessions and the provided moving averages.
     *
     * <p>This method is typically called after a candlestick pattern is detected, to evaluate
     * whether the stock price action confirms proximity to a technical support level.
     *
     * <p>It evaluates three conditions:
     *
     * <ul>
     *   <li><b>Condition 1:</b> Any of the last three sessions' key price points (prev2Close,
     *       prev2Low, prevClose, prevLow, open, low) are within 0.5% of their respective moving
     *       averages.
     *   <li><b>Condition 2:</b> Current session shows a rejection of support — open and close are
     *       above the average, but low pierced or touched the support level.
     *   <li><b>Condition 3:</b> Reclaim of support — current session closed above the average after
     *       opening below, and both of the prior sessions closed above their averages.
     * </ul>
     *
     * @param stockPrice the current day's stock price
     * @param average the moving average for the current session
     * @param prevAverage the moving average for the previous session
     * @param prev2Average the moving average for two sessions ago
     * @return true if the stock appears to be near support; false otherwise
     */
    @Override
    public boolean isNearSupport(
            StockPrice stockPrice, double average, double prevAverage, double prev2Average) {

        if (stockPrice == null) return false;

        // Extract price points from stock price history
        double prev2Close = stockPrice.getPrev2Close();
        double prev2Low = stockPrice.getPrev2Low();
        double prevClose = stockPrice.getPrevClose();
        double prevLow = stockPrice.getPrevLow();
        double open = stockPrice.getOpen();
        double close = stockPrice.getClose();
        double low = stockPrice.getLow();

        // Only check for low slightly above average (edge case), since < average is in condition2
        boolean condition1 = (low > average) && ((low - average) / average <= 0.005);

        // Condition 2: Current session open/close above average, but low touched or pierced support
        boolean condition2 =
                open > average
                        && close > average
                        && (low < average || (low - average) / average <= 0.005);

        // Condition 3: Reclaim — open below average, close above, with strong prior sessions
        boolean condition3 = open < average && close > average && prevClose > prevAverage;

        return condition3 || condition2 || condition1;
    }

    @Override
    public boolean isNearSupport(StockPrice stockPrice, double average) {

        return this.isNearSupport(stockPrice, average, average, average);
    }

    @Override
    /**
     * Determines if the stock is currently near a resistance level based on price behavior over the
     * last three trading sessions and the provided moving averages.
     *
     * <p>This method can be used after detecting a bearish candlestick pattern to validate whether
     * the price is reacting to a known resistance area.
     *
     * <p>It evaluates three conditions:
     *
     * <ul>
     *   <li><b>Condition 1:</b> Any of the last three sessions' key price points (prev2Close,
     *       prev2High, prevClose, prevHigh, open, high) are within 0.5% of their respective moving
     *       averages.
     *   <li><b>Condition 2:</b> Current session open and close are below resistance average, but
     *       high touched or pierced the level (rejection of resistance).
     *   <li><b>Condition 3:</b> Failed breakout — open above average but close below, and both
     *       previous sessions closed below their averages.
     * </ul>
     *
     * @param stockPrice the current day's stock price
     * @param average the moving average for the current session
     * @param prevAverage the moving average for the previous session
     * @param prev2Average the moving average for two sessions ago
     * @return true if the stock appears to be near resistance; false otherwise
     */
    public boolean isNearResistance(
            StockPrice stockPrice, double average, double prevAverage, double prev2Average) {

        if (stockPrice == null) return false;

        double prev2Close = stockPrice.getPrev2Close();
        double prev2High = stockPrice.getPrev2High();
        double prevClose = stockPrice.getPrevClose();
        double prevHigh = stockPrice.getPrevHigh();
        double open = stockPrice.getOpen();
        double close = stockPrice.getClose();
        double high = stockPrice.getHigh();

        // Condition 1: Current high is within +0.5% of the average, but still below it.
        // This indicates price is approaching resistance without breaking through.
        boolean condition1 = high < average && ((average - high) / average <= 0.005);

        // Condition 2: Open and close are both above average,
        // but high touched or pierced resistance (above or very close to average).
        // This suggests a potential rejection or failed breakout.
        boolean condition2 =
                open < average
                        && close < average
                        && (high > average || ((average - high) / average <= 0.005));

        // Condition 3: Failed breakout — open above average, close below, prior sessions below
        // resistance
        boolean condition3 = open > average && close < average && prevClose < prevAverage;

        return condition3 || condition2 || condition1;
    }

    @Override
    public boolean isNearResistance(StockPrice stockPrice, double average) {

        return this.isNearResistance(stockPrice, average, average, average);
    }
}
