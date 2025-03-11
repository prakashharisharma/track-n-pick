package com.example.service.impl;

import com.example.model.master.Stock;
import com.example.service.*;
import com.example.service.util.StockPriceUtil;
import com.example.util.io.model.type.Momentum;
import com.example.util.io.model.type.Trend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RelevanceServiceImpl implements RelevanceService {

    @Autowired
    private TimeframeSupportResistanceService timeframeSupportResistanceService;
    @Autowired
    private MovingAverageSupportResistanceService movingAverageSupportResistanceService;

    @Autowired
    private RsiIndicatorService rsiIndicatorService;

    @Autowired
    private CandleStickService candleStickService;
    @Autowired
    private VolumeIndicatorService volumeIndicatorService;
    @Autowired
    private YearlySupportResistanceService yearlySupportResistanceService;
    @Autowired
    private QuarterlySupportResistanceService quarterlySupportResistanceService;
    @Autowired
    private MonthlySupportResistanceService monthlySupportResistanceService;
    @Autowired
    private ObvIndicatorService obvIndicatorService;
    @Autowired
    private MultiIndicatorService multiIndicatorService;
    @Autowired
    private AdxIndicatorService adxIndicatorService;
    @Autowired
    private MacdIndicatorService macdIndicatorService;

    @Autowired
    private TrendService trendService;

    @Override
    public boolean isBullishTimeFrame(Stock stock, Trend trend, double minimumScore) {

        double score = 0.0;

        if(volumeIndicatorService.isBullish(stock)) {
            if (timeframeSupportResistanceService.isBreakout(stock, trend) && rsiIndicatorService.isBullish(stock)) {
                log.info("{} timeframe breakout active {} momentum {}}", stock.getNseSymbol(), trend.getStrength(), trend.getMomentum());
                score = score + 1.5;
            }

            if (timeframeSupportResistanceService.isNearSupport(stock, trend) && rsiIndicatorService.isOverSold(stock)) {
                log.info("{} timeframe support active {} momentum {}}", stock.getNseSymbol(), trend.getStrength(), trend.getMomentum());
                score = score + 1.5;
            }
        }

        log.info("{} relevance score: {} trend: {} momentum {}", stock.getNseSymbol(), score, trend.getStrength(), trend.getMomentum());

        return score >= minimumScore;
    }

    @Override
    public boolean isBullishMovingAverage(Stock stock, Trend trend,  double minimumScore) {

        double score = 0.0;

        if(trend.getMomentum() == Momentum.PULLBACK) {
            if (volumeIndicatorService.isBullish(stock)) {

                if (movingAverageSupportResistanceService.isBreakout(stock, trend) && rsiIndicatorService.isBullish(stock)) {
                    log.info("{} moving average breakout active {} momentum {}", stock.getNseSymbol(), trend.getStrength(), trend.getMomentum());
                    score = score + 1.5;
                }

                if (movingAverageSupportResistanceService.isNearSupport(stock, trend) && rsiIndicatorService.isOverSold(stock)) {
                    log.info("{} moving average support active {} momentum {}", stock.getNseSymbol(), trend.getStrength(), trend.getMomentum());
                    score = score + 1.5;
                }

            }
        }

        log.info("{} relevance score: {} trend: {} momentum {}", stock.getNseSymbol(), score, trend.getStrength(), trend.getMomentum());

        return score >= minimumScore;
    }

    @Override
    public boolean isBullishIndicator(Stock stock, Trend trend) {

        if(yearlySupportResistanceService.isBullish(stock) || quarterlySupportResistanceService.isBullish(stock) || monthlySupportResistanceService.isBullish(stock)) {
            if (rsiIndicatorService.isOverSold(stock) && rsiIndicatorService.isIncreasing(stock)) {
                if (obvIndicatorService.isBullish(stock) || volumeIndicatorService.isBullish(stock)) {
                    if (multiIndicatorService.isBullish(stock)) {
                        Stock prevSessionStock = StockPriceUtil.buildStockPricePreviousSession(stock);
                        if(candleStickService.isLowerLow(prevSessionStock) && candleStickService.isLowerHigh(prevSessionStock)) {
                            log.info("{} indicator support / breakout active {} momentum {}", stock.getNseSymbol(), trend.getStrength(), trend.getMomentum());
                            log.info("{} relevance score: {} trend: {} momentum {}", stock.getNseSymbol(), 3.0, trend.getStrength(),trend.getMomentum());
                            return Boolean.TRUE;
                        }
                    }
                }
            }
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isBearishTimeFrame(Stock stock, Trend trend,  double minimumScore) {

        double score = 0.0;


        if(volumeIndicatorService.isBearish(stock)) {
            if (timeframeSupportResistanceService.isBreakdown(stock, trend) && rsiIndicatorService.isBearish(stock)) {
                log.info("{} timeframe breakdown {} momentum {}", stock.getNseSymbol(), trend.getStrength(), trend.getMomentum());
                score = score + 1.5;
            }

            if (timeframeSupportResistanceService.isNearResistance(stock, trend) && rsiIndicatorService.isOverBaught(stock)) {
                log.info("{} timeframe resistance {} momentum {}", stock.getNseSymbol(), trend.getStrength(), trend.getMomentum());
                score = score + 1.5;
            }
        }

        log.info("{} relevance score: {} trend: {} momentum {}", stock.getNseSymbol(), score, trend.getStrength(), trend.getMomentum());
        return score >= minimumScore;
    }

    @Override
    public boolean isBearishMovingAverage(Stock stock, Trend trend,  double minimumScore) {

        double score = 0.0;
        if(trend.getMomentum() == Momentum.RECOVERY) {
            if (volumeIndicatorService.isBearish(stock)) {
                if (movingAverageSupportResistanceService.isBreakdown(stock, trend) && rsiIndicatorService.isBearish(stock)) {
                    log.info("{} moving average breakdown {} momentum {}", stock.getNseSymbol(), trend.getStrength(), trend.getMomentum());
                    score = score + 1.5;
                }

                if (movingAverageSupportResistanceService.isNearResistance(stock, trend) && rsiIndicatorService.isOverBaught(stock)) {
                    log.info("{} moving average resistance {} momentum {}", stock.getNseSymbol(), trend.getStrength(), trend.getMomentum());
                    score = score + 1.5;
                }

            }
        }

        log.info("{} relevance score: {} trend: {} momentum {}", stock.getNseSymbol(), score, trend.getStrength(), trend.getMomentum());
        return score >= minimumScore;
    }

    @Override
    public boolean isBearishIndicator(Stock stock, Trend trend) {

        if(yearlySupportResistanceService.isBearish(stock) || quarterlySupportResistanceService.isBearish(stock) || monthlySupportResistanceService.isBearish(stock)) {
            if(rsiIndicatorService.isOverBaught(stock) && rsiIndicatorService.isDecreasing(stock)) {
                if (obvIndicatorService.isBearish(stock) || volumeIndicatorService.isBearish(stock)) {
                    if (multiIndicatorService.isBearish(stock)) {
                        Stock prevSessionStock = StockPriceUtil.buildStockPricePreviousSession(stock);
                        if(candleStickService.isHigherLow(prevSessionStock) && candleStickService.isHigherHigh(prevSessionStock)) {
                            log.info("{} indicator resistance / breakdown active {} momentum {}", stock.getNseSymbol(), trend.getStrength(), trend.getMomentum());
                            log.info("{} relevance score: {} trend: {} momentum {}", stock.getNseSymbol(), 3.0, trend.getStrength(),trend.getMomentum());
                            return Boolean.TRUE;
                        }
                    }
                }
            }
        }

        return Boolean.FALSE;
    }

    public  double calculateBullishVolumeFactorUsingRsi(double rsi) {
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
        if (rsi > 80 ) return 1.0;

        if (rsi > overBought) {
            return baseValue - (rsi - overBought) * 0.1; // Decrease by 0.50 per 5 units above 70
        } else {
            return baseValue + (overBought - rsi) * 0.2; // Increase by 1.0 per 5 units below 70
        }
    }
}
