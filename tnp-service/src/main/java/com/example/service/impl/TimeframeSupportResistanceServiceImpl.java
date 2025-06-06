package com.example.service.impl;

import com.example.data.common.type.SupportResistanceStyle;
import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class TimeframeSupportResistanceServiceImpl implements TimeframeSupportResistanceService {

    private final SupportResistanceUtilService supportResistanceService;
    private final StockPriceService<StockPrice> stockPriceService;
    private final BreakoutService breakoutService;

    @Override
    public boolean isBreakout(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        double resistance = getReferenceLevel(stockPrice, timeframe, LevelType.RESISTANCE, SupportResistanceStyle.BODY_BASED);

        return breakoutService.isBreakOut(stockPrice, resistance, resistance);
    }

    @Override
    public boolean isBreakdown(Timeframe timeframe, StockPrice stockPrice, StockTechnicals unused) {
        double support = getReferenceLevel(stockPrice, timeframe, LevelType.SUPPORT,SupportResistanceStyle.BODY_BASED);
        return breakoutService.isBreakDown(stockPrice, support, support);
    }

    @Override
    public boolean isNearSupport(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals unused) {
        double support = getReferenceLevel(stockPrice, timeframe, LevelType.SUPPORT,SupportResistanceStyle.WICK_BASED);
        return supportResistanceService.isNearSupport(stockPrice, support);
    }

    @Override
    public boolean isNearResistance(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals unused) {
        double resistance = getReferenceLevel(stockPrice, timeframe, LevelType.RESISTANCE,SupportResistanceStyle.WICK_BASED);
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
    private double getReferenceLevel(StockPrice stockPrice, Timeframe timeframe, LevelType type, SupportResistanceStyle style) {
        Stock stock = stockPrice.getStock();
        StockPrice htStockPrice = stockPriceService.get(stock, timeframe);

        if (htStockPrice == null) {
            log.warn("HTF stock price not found for stock: {} and timeframe: {}", stock.getNseSymbol(), timeframe);
            return Double.NaN;
        }

        boolean isNewerSession = htStockPrice.getSessionDate().isBefore(stockPrice.getSessionDate());

        double rawLevel;

        if (style == SupportResistanceStyle.BODY_BASED) {
            double htOpen = isNewerSession ? htStockPrice.getOpen() : htStockPrice.getPrevOpen();
            double htClose = isNewerSession ? htStockPrice.getClose() : htStockPrice.getPrevClose();
            double level = type == LevelType.SUPPORT ? Math.min(htOpen, htClose) : Math.max(htOpen, htClose);

            log.debug("Using BODY-BASED {} level: {}", type, level);
            rawLevel = level;

        } else {
            boolean isBullish = htStockPrice.getClose() > htStockPrice.getOpen();
            boolean isBearish = htStockPrice.getClose() < htStockPrice.getOpen();

            rawLevel = switch (type) {
                case SUPPORT -> isNewerSession
                        ? (isBullish ? htStockPrice.getHigh() : htStockPrice.getLow())
                        : (isBullish ? htStockPrice.getPrevHigh() : htStockPrice.getPrevLow());
                case RESISTANCE -> isNewerSession
                        ? (isBearish ? htStockPrice.getLow() : htStockPrice.getHigh())
                        : (isBearish ? htStockPrice.getPrevLow() : htStockPrice.getPrevHigh());
            };

            log.debug("Using WICK-BASED {} level: {}", type, rawLevel);
        }

        // SR Flip & body-cross logic
        double open = stockPrice.getOpen();
        double close = stockPrice.getClose();
        double min = Math.min(open, close);
        double max = Math.max(open, close);

        boolean bodyCrossesLevel = (open < rawLevel && close > rawLevel) || (open > rawLevel && close < rawLevel);
        if (bodyCrossesLevel) {
            log.info("Candle body crosses the {} level at {}", type, rawLevel);
            return rawLevel;
        }

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
