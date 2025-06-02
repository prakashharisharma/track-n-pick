package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TimeframeSupportResistanceServiceImpl implements TimeframeSupportResistanceService {

    private final SupportResistanceUtilService supportResistanceService;
    private final StockPriceService<StockPrice> stockPriceService;

    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    private final RsiIndicatorService rsiIndicatorService;
    private final MacdIndicatorService macdIndicatorService;
    private final BreakoutService breakoutService;

    private final DynamicMovingAverageSupportResolverService
            dynamicMovingAverageSupportResolverService;

    @Override
    public boolean isBreakout(
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            boolean confirmationCheck) {
        Stock stock = stockPrice.getStock();

        // Get HTF stock price and resistance level
        StockPrice htStockPrice = stockPriceService.get(stock, timeframe);
        if (htStockPrice == null) return false;

        double resistance = getReferenceLevel(stockPrice, timeframe, LevelType.RESISTANCE);

        // Get HTF technicals
        StockTechnicals htStockTechnicals = stockTechnicalsService.get(stock, timeframe);
        if (htStockTechnicals == null) return false;

        boolean isNewerSession =
                htStockPrice.getSessionDate().isBefore(stockPrice.getSessionDate());

        // Shift MACD history if older HTF candle is being used
        if (!isNewerSession) {
            shiftMacd(htStockTechnicals);
        }

        // Core breakout check
        boolean breakout = breakoutService.isBreakOut(stockPrice, resistance, resistance);

        boolean isMacdConfirmingBreakout =
                isMacdConfirmingBreakout(stockTechnicals)
                        || isMacdConfirmingBreakout(htStockTechnicals);
        boolean isMacdAndRsiConfirmingBreakout =
                isMacdAndRsiConfirmingBreakout(stockTechnicals)
                        || isMacdAndRsiConfirmingBreakout(htStockTechnicals);
        boolean isMovingAverageConfirmingBreakout =
                isMovingAverageConfirmingBreakout(timeframe.getLower(), stockPrice, stockTechnicals)
                        || isMovingAverageConfirmingBreakout(
                                timeframe, stockPrice, stockTechnicals);

        // If confirmation is required, validate with MACD confirmation
        if (confirmationCheck) {
            return breakout
                    && (isMacdConfirmingBreakout
                            || isMacdAndRsiConfirmingBreakout
                            || isMovingAverageConfirmingBreakout);
        } else {
            return breakout;
        }
    }

    private void shiftMacd(StockTechnicals tech) {
        tech.setMacd(tech.getPrevMacd());
        tech.setPrevMacd(tech.getPrev2Macd());
        tech.setSignal(tech.getPrevSignal());
        tech.setPrevSignal(tech.getPrev2Signal());

        tech.setRsi(tech.getPrevRsi());
        tech.setPrevRsi(tech.getPrev2Rsi());
    }

    private boolean isMacdConfirmingBreakout(StockTechnicals st) {

        if (st == null) {
            return false;
        }

        double macd = st.getMacd();
        double signal = st.getSignal();

        // Case 1: MACD still below signal or in negative zone — check momentum shift
        if (macd < signal && macdIndicatorService.isHistogramBelowZero(st)) {
            return macdIndicatorService.isMacdIncreased(st)
                    && macdIndicatorService.isSignalDecreased(st)
                    && macdIndicatorService.isHistogramIncreased(st);
        }

        // Case 2: MACD crossover happened, even in negative — early breakout signal
        return macdIndicatorService.isMacdCrossedSignal(st);
    }

    private boolean isMacdAndRsiConfirmingBreakout(StockTechnicals st) {

        if (st == null) {
            return false;
        }

        double macd = st.getMacd();
        double signal = st.getSignal();

        // Case 1: MACD still below signal or in negative zone — check momentum shift
        if (macd < signal && macdIndicatorService.isHistogramBelowZero(st)) {
            return rsiIndicatorService.isBullish(st)
                    && macdIndicatorService.isSignalDecreased(st)
                    && macdIndicatorService.isHistogramIncreased(st);
        }

        // Case 2: MACD crossover happened, even in negative — early breakout signal
        return macdIndicatorService.isMacdCrossedSignal(st);
    }

    private boolean isMovingAverageConfirmingBreakout(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {
        Optional<MAEvaluationResult> evaluationResultOptional =
                dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                        timeframe, stockPrice, stockTechnicals);
        if (evaluationResultOptional.isPresent()) {
            MAEvaluationResult evaluationResult = evaluationResultOptional.get();
            return ((timeframe == Timeframe.DAILY
                            && evaluationResult.getLength() != MovingAverageLength.HIGHEST))
                    && evaluationResult.isBreakout();
        }
        return false;
    }

    @Override
    public boolean isBreakdown(Timeframe timeframe, StockPrice stockPrice, StockTechnicals unused) {
        double support = getReferenceLevel(stockPrice, timeframe, LevelType.SUPPORT);
        return breakoutService.isBreakDown(stockPrice, support, support);
    }

    @Override
    public boolean isNearSupport(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals unused) {
        double support = getReferenceLevel(stockPrice, timeframe, LevelType.SUPPORT);
        return supportResistanceService.isNearSupport(stockPrice, support);
    }

    @Override
    public boolean isNearResistance(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals unused) {
        double resistance = getReferenceLevel(stockPrice, timeframe, LevelType.RESISTANCE);
        return supportResistanceService.isNearResistance(stockPrice, resistance);
    }

    private enum LevelType {
        SUPPORT,
        RESISTANCE
    }

    /**
     * Calculates the reference support or resistance level based on a higher timeframe (HTF)
     * candle. Includes SR flip logic and handles cross scenarios based on open and close.
     *
     * @param stockPrice The current timeframe stock price (e.g., daily)
     * @param timeframe The higher timeframe for reference (e.g., weekly/monthly)
     * @param type The level type to calculate (SUPPORT or RESISTANCE)
     * @return The calculated reference level
     */
    private double getReferenceLevel(StockPrice stockPrice, Timeframe timeframe, LevelType type) {
        Stock stock = stockPrice.getStock();
        StockPrice htStockPrice = stockPriceService.get(stock, timeframe);

        if (htStockPrice == null) {
            log.warn(
                    "HTF stock price not found for stock: {} and timeframe: {}",
                    stock.getNseSymbol(),
                    timeframe);
            return Double.NaN;
        }

        boolean isNewerSession =
                htStockPrice.getSessionDate().isBefore(stockPrice.getSessionDate());
        boolean isBullish = htStockPrice.getClose() > htStockPrice.getOpen();
        boolean isBearish = htStockPrice.getClose() < htStockPrice.getOpen();

        log.debug(
                "HTF Candle - Symbol: {}, Date: {}, Bullish: {}, Bearish: {}",
                stock.getNseSymbol(),
                htStockPrice.getSessionDate(),
                isBullish,
                isBearish);

        double rawLevel;
        switch (type) {
            case SUPPORT:
                rawLevel =
                        isNewerSession
                                ? (isBullish ? htStockPrice.getHigh() : htStockPrice.getLow())
                                : (isBullish
                                        ? htStockPrice.getPrevHigh()
                                        : htStockPrice.getPrevLow());
                break;
            case RESISTANCE:
                rawLevel =
                        isNewerSession
                                ? (isBearish ? htStockPrice.getLow() : htStockPrice.getHigh())
                                : (isBearish
                                        ? htStockPrice.getPrevLow()
                                        : htStockPrice.getPrevHigh());
                break;
            default:
                throw new IllegalArgumentException("Unsupported level type: " + type);
        }

        double open = stockPrice.getOpen();
        double close = stockPrice.getClose();
        double min = Math.min(open, close);
        double max = Math.max(open, close);

        // Cross detection: if candle body crosses the level
        boolean bodyCrossesLevel =
                (open < rawLevel && close > rawLevel) || (open > rawLevel && close < rawLevel);
        log.debug(
                "Current Candle: Open={}, Close={}, Type={}, Level={}",
                open,
                close,
                type,
                rawLevel);

        if (bodyCrossesLevel) {
            log.info("Candle body crosses the {} level at {}", type, rawLevel);
            return rawLevel;
        }

        // SR Flip logic
        if (type == LevelType.SUPPORT && max < rawLevel) {
            log.info("Close below SUPPORT: level {} is acting as RESISTANCE", rawLevel);
            return rawLevel;
        }

        if (type == LevelType.RESISTANCE && min > rawLevel) {
            log.info("Close above RESISTANCE: level {} is acting as SUPPORT", rawLevel);
            return rawLevel;
        }

        log.info("{} level holds at {}", type, rawLevel);
        return rawLevel;
    }
}
