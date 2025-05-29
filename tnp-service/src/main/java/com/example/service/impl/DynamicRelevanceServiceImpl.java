package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import com.example.service.utils.CandleStickUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class DynamicRelevanceServiceImpl implements DynamicRelevanceService {

    private final DynamicMovingAverageSupportResolverService
            dynamicMovingAverageSupportResolverService;

    private final TimeframeSupportResistanceService timeframeSupportResistanceService;
    private final RsiIndicatorService rsiIndicatorService;

    private final MultiIndicatorService multiIndicatorService;
    private final ObvIndicatorService obvIndicatorService;

    @Override
    public boolean isNearSupport(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        if (trend.getDirection() == Trend.Direction.DOWN) {
            if (dynamicMovingAverageSupportResolverService.isNearSupport(
                            trend, timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isOverSold(stockTechnicals)) {
                log.info(
                        "{} dynamic moving average support active {} momentum {}",
                        stockPrice.getStock().getNseSymbol(),
                        trend.getDirection(),
                        trend.getMomentum());
                return true;
            } else if (trend.getMomentum() == Trend.Phase.BOTTOM
                    && timeframeSupportResistanceService.isNearSupport(
                            trend, timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isOverSold(stockTechnicals)) {
                log.info(
                        "{} multi time frame support active {} momentum {}",
                        stockPrice.getStock().getNseSymbol(),
                        trend.getDirection(),
                        trend.getMomentum());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isNearResistance(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        if (trend.getDirection() == Trend.Direction.UP) {
            if (trend.getMomentum() == Trend.Phase.TOP
                    && timeframeSupportResistanceService.isNearResistance(
                            trend, timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isOverBought(stockTechnicals)) {
                log.info(
                        "{} dynamic timeframe resistance {} momentum {}",
                        stockPrice.getStock().getNseSymbol(),
                        trend.getDirection(),
                        trend.getMomentum());
                return true;
            } else if (dynamicMovingAverageSupportResolverService.isNearResistance(
                            trend, timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isOverBought(stockTechnicals)) {
                log.info(
                        "{} dynamic moving average resistance {} momentum {}",
                        stockPrice.getStock().getNseSymbol(),
                        trend.getDirection(),
                        trend.getMomentum());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBreakout(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        if (trend.getDirection() == Trend.Direction.UP
                && CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals)) {
            if (trend.getMomentum() == Trend.Phase.TOP
                    && timeframeSupportResistanceService.isBreakout(
                            trend, timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isBullish(stockTechnicals)) {
                log.info(
                        "{} timeframe breakout active {} momentum {}}",
                        stockPrice.getStock().getNseSymbol(),
                        trend.getDirection(),
                        trend.getMomentum());
                return true;
            } else if (dynamicMovingAverageSupportResolverService.isBreakout(
                            trend, timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isBullish(stockTechnicals)) {
                log.info(
                        "{} moving average breakout active {} momentum {}",
                        stockPrice.getStock().getNseSymbol(),
                        trend.getDirection(),
                        trend.getMomentum());
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isBreakdown(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        if (trend.getDirection() == Trend.Direction.DOWN
                && CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals)) {

            if (dynamicMovingAverageSupportResolverService.isBreakdown(
                            trend, timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isBearish(stockTechnicals)) {
                log.info(
                        "{} moving average breakdown {} momentum {}",
                        stockPrice.getStock().getNseSymbol(),
                        trend.getDirection(),
                        trend.getMomentum());
                return true;
            } else if (trend.getMomentum() == Trend.Phase.BOTTOM
                    && timeframeSupportResistanceService.isBreakdown(
                            trend, timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isBearish(stockTechnicals)) {
                log.info(
                        "{} timeframe breakdown {} momentum {}",
                        stockPrice.getStock().getNseSymbol(),
                        trend.getDirection(),
                        trend.getMomentum());
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isBullishIndicator(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        // Check if OBV or Volume indicators are bullish
        boolean isVolumeBullish = obvIndicatorService.isBullish(stockTechnicals);

        if (!isVolumeBullish) {
            return false;
        }

        // Check multi-indicator bullish conditions
        if (!multiIndicatorService.isBullish(stockTechnicals)) {
            return false;
        }

        log.info(
                "{} indicator support / breakout rejected as price is away from EMA20. Using"
                        + " Strategy: {}, SubStrategy: {}",
                stockPrice.getStock().getNseSymbol(),
                ResearchTechnical.Strategy.PRICE,
                ResearchTechnical.SubStrategy.RMAO);

        return true;
    }

    @Override
    public boolean isBearishIndicator(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        // Check if OBV or Volume indicators are bearish
        boolean isVolumeBearish = obvIndicatorService.isBearish(stockTechnicals);

        if (!isVolumeBearish) {
            return false;
        }

        // Check multi-indicator bearish conditions
        if (!multiIndicatorService.isBearish(stockTechnicals)) {
            return false;
        }

        log.info(
                "{} indicator resistance / breakdown rejected as price is away from EMA20. Using"
                        + " Strategy: {}, SubStrategy: {}",
                stockPrice.getStock().getNseSymbol(),
                ResearchTechnical.Strategy.PRICE,
                ResearchTechnical.SubStrategy.RMAO);

        return true;
    }

    @Override
    public boolean isBottomBreakout(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        return false;
    }

    @Override
    public boolean isTopBreakdown(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        return false;
    }
}
