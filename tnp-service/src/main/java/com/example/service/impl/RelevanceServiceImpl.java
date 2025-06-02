package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.*;
import com.example.service.utils.CandleStickUtils;
import com.example.util.FormulaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RelevanceServiceImpl implements RelevanceService {

    @Autowired
    private MultiTimeframeSupportResistanceService multiTimeframeSupportResistanceService;

    @Autowired
    private MultiMovingAverageSupportResistanceService multiMovingAverageSupportResistanceService;

    @Autowired private FormulaService formulaService;

    @Autowired private RsiIndicatorService rsiIndicatorService;

    @Autowired private CandleStickService candleStickService;
    @Autowired private VolumeIndicatorService volumeIndicatorService;

    @Autowired private ObvIndicatorService obvIndicatorService;
    @Autowired private MultiIndicatorService multiIndicatorService;
    @Autowired private AdxIndicatorService adxIndicatorService;
    @Autowired private MacdIndicatorService macdIndicatorService;

    @Override
    public boolean isNearSupport(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        if (trend.getDirection() == Trend.Direction.DOWN) {

            if (multiMovingAverageSupportResistanceService.isNearSupport(
                            trend, timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isOverSold(stockTechnicals)) {
                log.info(
                        "{} moving average support active {} momentum {}",
                        stockPrice.getStock().getNseSymbol(),
                        trend.getDirection(),
                        trend.getMomentum());
                return true;
            } else if (trend.getMomentum() == Trend.Phase.BOTTOM
                    && multiTimeframeSupportResistanceService.isNearSupport(
                            timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isOverSold(stockTechnicals)) {
                log.info(
                        "{} timeframe support active {} momentum {}",
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
                    && multiTimeframeSupportResistanceService.isNearResistance(
                            timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isOverBought(stockTechnicals)) {
                log.info(
                        "{} timeframe resistance {} momentum {}",
                        stockPrice.getStock().getNseSymbol(),
                        trend.getDirection(),
                        trend.getMomentum());
                return true;
            } else if (multiMovingAverageSupportResistanceService.isNearResistance(
                            trend, timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isOverBought(stockTechnicals)) {
                log.info(
                        "{} moving average resistance {} momentum {}",
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
                    && multiTimeframeSupportResistanceService.isBreakout(
                            timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isBullish(stockTechnicals)) {
                log.info(
                        "{} timeframe breakout active {} momentum {}}",
                        stockPrice.getStock().getNseSymbol(),
                        trend.getDirection(),
                        trend.getMomentum());
                return true;
            } else if (multiMovingAverageSupportResistanceService.isBreakout(
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

            if (multiMovingAverageSupportResistanceService.isBreakdown(
                            trend, timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isBearish(stockTechnicals)) {
                log.info(
                        "{} moving average breakdown {} momentum {}",
                        stockPrice.getStock().getNseSymbol(),
                        trend.getDirection(),
                        trend.getMomentum());
                return true;
            } else if (trend.getMomentum() == Trend.Phase.BOTTOM
                    && multiTimeframeSupportResistanceService.isBreakdown(
                            timeframe, stockPrice, stockTechnicals)
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
    public boolean isBullishTimeFrame(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            double minimumScore) {

        double score = 0.0;

        // if(volumeIndicatorService.isBullish(stockTechnicals, timeframe)) {

        if (trend.getDirection() == Trend.Direction.DOWN) {
            if (multiTimeframeSupportResistanceService.isNearSupport(
                            timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isOverSold(stockTechnicals)) {
                log.info(
                        "{} timeframe support active {} momentum {}",
                        stockPrice.getStock().getNseSymbol(),
                        trend.getDirection(),
                        trend.getMomentum());
                score = score + 1.5;
            }
        }

        if (trend.getDirection() == Trend.Direction.UP) {
            if (multiTimeframeSupportResistanceService.isBreakout(
                            timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isBullish(stockTechnicals)) {
                log.info(
                        "{} timeframe breakout active {} momentum {}}",
                        stockPrice.getStock().getNseSymbol(),
                        trend.getDirection(),
                        trend.getMomentum());
                score = score + 1.5;
            }
        }

        // }

        log.info(
                "{} relevance score: {} trend: {} momentum {}",
                stockPrice.getStock().getNseSymbol(),
                score,
                trend.getDirection(),
                trend.getMomentum());

        return score >= minimumScore;
    }

    @Override
    public boolean isBullishMovingAverage(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            double minimumScore) {

        double score = 0.0;

        // if(trend.getMomentum() == Trend.Momentum.PULLBACK || trend.getMomentum() ==
        // Trend.Momentum.CORRECTION) {
        // if (volumeIndicatorService.isBullish(stockTechnicals, timeframe)) {
        if (trend.getDirection() == Trend.Direction.DOWN) {
            if (multiMovingAverageSupportResistanceService.isNearSupport(
                            trend, timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isOverSold(stockTechnicals)) {
                log.info(
                        "{} moving average support active {} momentum {}",
                        stockPrice.getStock().getNseSymbol(),
                        trend.getDirection(),
                        trend.getMomentum());
                score = score + 1.5;
            }
        }
        if (trend.getDirection() == Trend.Direction.UP) {
            if (multiMovingAverageSupportResistanceService.isBreakout(
                            trend, timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isBullish(stockTechnicals)) {
                log.info(
                        "{} moving average breakout active {} momentum {}",
                        stockPrice.getStock().getNseSymbol(),
                        trend.getDirection(),
                        trend.getMomentum());
                score = score + 1.5;
            }
        }

        // }
        // }

        log.info(
                "{} relevance score: {} trend: {} momentum {}",
                stockPrice.getStock().getNseSymbol(),
                score,
                trend.getDirection(),
                trend.getMomentum());

        return score >= minimumScore;
    }

    @Override
    public boolean isBullishIndicator(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        // Check if the trend is in a dip, pullback or correction phase
        if (trend.getMomentum() != Trend.Phase.DIP
                && trend.getMomentum() != Trend.Phase.PULLBACK
                && trend.getMomentum() != Trend.Phase.CORRECTION
                && trend.getMomentum() != Trend.Phase.DEEP_CORRECTION) {
            return false;
        }

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
    public boolean isBearishTimeFrame(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            double minimumScore) {

        double score = 0.0;

        // if(volumeIndicatorService.isBearish(stockTechnicals, timeframe)) {
        if (trend.getDirection() == Trend.Direction.DOWN) {
            if (multiTimeframeSupportResistanceService.isBreakdown(
                            timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isBearish(stockTechnicals)) {
                log.info(
                        "{} timeframe breakdown {} momentum {}",
                        stockPrice.getStock().getNseSymbol(),
                        trend.getDirection(),
                        trend.getMomentum());
                score = score + 1.5;
            }
        }
        if (trend.getDirection() == Trend.Direction.UP) {
            if (multiTimeframeSupportResistanceService.isNearResistance(
                            timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isOverBought(stockTechnicals)) {
                log.info(
                        "{} timeframe resistance {} momentum {}",
                        stockPrice.getStock().getNseSymbol(),
                        trend.getDirection(),
                        trend.getMomentum());
                score = score + 1.5;
            }
        }
        // }

        log.info(
                "{} relevance score: {} trend: {} momentum {}",
                stockPrice.getStock().getNseSymbol(),
                score,
                trend.getDirection(),
                trend.getMomentum());
        return score >= minimumScore;
    }

    @Override
    public boolean isBearishMovingAverage(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            double minimumScore) {

        double score = 0.0;
        if (trend.getDirection() == Trend.Direction.DOWN) {
            // if(trend.getMomentum() == Trend.Momentum.RECOVERY || trend.getMomentum() ==
            // Trend.Momentum.ADVANCE) {
            // if (volumeIndicatorService.isBearish(stockTechnicals, timeframe)) {
            if (multiMovingAverageSupportResistanceService.isBreakdown(
                            trend, timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isBearish(stockTechnicals)) {
                log.info(
                        "{} moving average breakdown {} momentum {}",
                        stockPrice.getStock().getNseSymbol(),
                        trend.getDirection(),
                        trend.getMomentum());
                score = score + 1.5;
            }
        }
        if (trend.getDirection() == Trend.Direction.UP) {
            if (multiMovingAverageSupportResistanceService.isNearResistance(
                            trend, timeframe, stockPrice, stockTechnicals)
                    && rsiIndicatorService.isOverBought(stockTechnicals)) {
                log.info(
                        "{} moving average resistance {} momentum {}",
                        stockPrice.getStock().getNseSymbol(),
                        trend.getDirection(),
                        trend.getMomentum());
                score = score + 1.5;
            }
        }
        // }

        log.info(
                "{} relevance score: {} trend: {} momentum {}",
                stockPrice.getStock().getNseSymbol(),
                score,
                trend.getDirection(),
                trend.getMomentum());
        return score >= minimumScore;
    }

    @Override
    public boolean isBearishIndicator(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        // Check if the trend is in a top or advance phase
        if (trend.getMomentum() != Trend.Phase.TOP
                && trend.getMomentum() != Trend.Phase.ADVANCE
                && trend.getMomentum() != Trend.Phase.RECOVERY
                && trend.getMomentum() != Trend.Phase.EARLY_RECOVERY) {
            return false;
        }

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

    public double calculateBullishVolumeFactorUsingRsi(double rsi) {
        double baseValue = 2.5;
        double overSold = 30.0;
        if (rsi == overSold) return baseValue;
        if (rsi < 20.0) return 1.0;

        if (rsi > overSold) {
            return baseValue + (rsi - overSold) * 0.2;
        } else {
            return baseValue - (overSold - rsi) * 0.1;
        }
    }

    public double calculateBearishVolumeFactorUsingRsi(double rsi) {
        double baseValue = 2.0;
        double overBought = 70.0;
        if (rsi == overBought) return baseValue; // Base value at 70
        if (rsi > 80) return 1.0;

        if (rsi > overBought) {
            return baseValue - (rsi - overBought) * 0.1; // Decrease by 0.50 per 5 units above 70
        } else {
            return baseValue + (overBought - rsi) * 0.2; // Increase by 1.0 per 5 units below 70
        }
    }
}
