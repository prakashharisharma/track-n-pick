package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.TradeSetup;
import com.example.service.*;
import com.example.service.StockPriceService;
import com.example.service.StockTechnicalsService;
import com.example.util.FormulaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SwingActionServiceImpl implements SwingActionService {

    @Autowired private SupportResistanceUtilService supportResistanceService;
    @Autowired private CandleStickConfirmationService candleStickHelperService;
    @Autowired private MacdIndicatorService macdIndicatorService;
    @Autowired private CandleStickService candleStickService;
    @Autowired private RsiIndicatorService rsiTrendService;
    @Autowired private StockPriceServiceOld stockPriceServiceOld;

    @Autowired private BreakoutService breakoutService;

    @Autowired private FormulaService formulaService;

    @Autowired private VolumeIndicatorService volumeIndicatorService;
    @Autowired private TrendService trendService;

    @Autowired private RsiIndicatorService rsiIndicatorService;

    @Autowired private ObvIndicatorService obvIndicatorService;

    @Autowired private AdxIndicatorService adxIndicatorService;

    @Autowired private StockPriceService<StockPrice> stockPriceService;

    @Autowired private StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    /**
     * Volume > Weekly && Volume > Monthly if RSISwing Or AdxSwing
     *
     * @param stock
     * @return
     */
    @Override
    public TradeSetup breakOut(Stock stock, Timeframe timeframe) {
        StockPrice stockPrice = stockPriceService.get(stock, timeframe);
        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);
        Trend trend = trendService.detect(stock, timeframe);
        log.info(
                "{} : Scanning swing action breakout Direction: {}, Momentum:{}",
                stock.getNseSymbol(),
                trend.getDirection(),
                trend.getMomentum());
        if (trend.getMomentum() == Trend.Phase.DIP
                || trend.getMomentum() == Trend.Phase.PULLBACK
                || trend.getMomentum() == Trend.Phase.CORRECTION) {
            if (candleStickHelperService.isBullishConfirmed(
                    timeframe, stockPrice, stockTechnicals)) {
                ResearchTechnical.SubStrategy subStrategy = ResearchTechnical.SubStrategy.RM;

                // boolean isTmaConvergenceAndDivergence = this.isTmaDivergence(stock, timeframe,
                // trend);
                boolean isRsiMacdBreakout = this.isRsiMacdBreakout(stock, timeframe, trend);

                boolean isSwingAction = Boolean.FALSE;

                /*if (isTmaConvergenceAndDivergence) {
                    subStrategy = ResearchTechnical.SubStrategy.TEMA;

                    isSwingAction = Boolean.TRUE;
                } else */
                if (isRsiMacdBreakout) {
                    subStrategy = ResearchTechnical.SubStrategy.RM;
                    isSwingAction = Boolean.TRUE;
                }

                if (isSwingAction) {
                    // StockTechnicals stockTechnicals = stockTechnicalsService.get(stock,
                    // timeframe);

                    if (Math.abs(
                                    formulaService.calculateChangePercentage(
                                            stockTechnicals.getEma20(), stockPrice.getClose()))
                            < 10.0) {

                        log.info(
                                "{} bullish swing action confirmed using {}:{}",
                                stock.getNseSymbol(),
                                ResearchTechnical.Strategy.PRICE,
                                subStrategy);

                        return TradeSetup.builder()
                                .active(Boolean.TRUE)
                                .strategy(ResearchTechnical.Strategy.SWING)
                                .subStrategy(subStrategy)
                                .build();
                    }
                    log.info(
                            "{} bullish swing action rejected as price is away from ema 20 using"
                                    + " {}:{}",
                            stock.getNseSymbol(),
                            ResearchTechnical.Strategy.PRICE,
                            subStrategy);
                }
            }
        }
        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    /**
     * Volume > Weekly && Volume > Monthly if RSISwing Or AdxSwing
     *
     * @param stock
     * @return
     */
    @Override
    public TradeSetup breakDown(Stock stock, Timeframe timeframe) {
        StockPrice stockPrice = stockPriceService.get(stock, timeframe);
        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);
        Trend trend = trendService.detect(stock, timeframe);
        log.info(
                "{} : Scanning swing action breakdown Direction: {}, Momentum:{}",
                stock.getNseSymbol(),
                trend.getDirection(),
                trend.getMomentum());
        if (trend.getMomentum() == Trend.Phase.TOP
                || trend.getMomentum() == Trend.Phase.ADVANCE
                || trend.getMomentum() == Trend.Phase.RECOVERY) {
            // if (trend.getMomentum() == Trend.Momentum.RECOVERY || trend.getMomentum() ==
            // Trend.Momentum.ADVANCE) {
            if (candleStickHelperService.isBearishConfirmed(
                    timeframe, stockPrice, stockTechnicals)) {
                ResearchTechnical.SubStrategy subStrategy = ResearchTechnical.SubStrategy.RM;

                boolean isTmaConvergence = this.isTmaConvergence(stock, timeframe, trend);
                boolean isRsiMacdBreakdown = this.isRsiMacdBreakdown(stock, timeframe, trend);

                double entryPrice = stockPrice.getLow();
                double stopLossPrice = stockPrice.getHigh();

                double targetPrice = 0.0;
                double risk =
                        Math.abs(
                                formulaService.calculateChangePercentage(
                                        entryPrice, stopLossPrice));

                boolean isSwingAction = Boolean.FALSE;

                if (isTmaConvergence) {
                    subStrategy = ResearchTechnical.SubStrategy.TEMA;
                    targetPrice =
                            formulaService.calculateTarget(
                                    entryPrice, stopLossPrice, RiskReward.SWING_TEMA);
                    isSwingAction = Boolean.TRUE;
                } else if (isRsiMacdBreakdown) {
                    subStrategy = ResearchTechnical.SubStrategy.RM;
                    targetPrice =
                            formulaService.calculateTarget(
                                    entryPrice, stopLossPrice, RiskReward.SWING_RM);
                    isSwingAction = Boolean.TRUE;
                }

                if (isSwingAction) {
                    // StockTechnicals stockTechnicals = stockTechnicalsService.get(stock,
                    // timeframe);

                    if (Math.abs(
                                    formulaService.calculateChangePercentage(
                                            stockTechnicals.getEma20(), stockPrice.getClose()))
                            > 10.0) {

                        log.info(
                                "{} bearish swing action confirmed using {}:{}",
                                stock.getNseSymbol(),
                                ResearchTechnical.Strategy.PRICE,
                                subStrategy);
                        log.info(
                                "{} bearish swing action active for trend:{}, momentum:{},"
                                    + " entryPrice:{}, targetPrice:{}, stopLossPrice:{}, risk {}",
                                stock.getNseSymbol(),
                                trend.getStrength(),
                                trend.getMomentum(),
                                entryPrice,
                                targetPrice,
                                stopLossPrice,
                                risk);

                        return TradeSetup.builder()
                                .active(Boolean.TRUE)
                                .strategy(ResearchTechnical.Strategy.SWING)
                                .subStrategy(subStrategy)
                                .build();
                    }
                    log.info(
                            "{} bearish swing action rejected as price is away from ema 20 using"
                                    + " {}:{}",
                            stock.getNseSymbol(),
                            ResearchTechnical.Strategy.PRICE,
                            subStrategy);
                }
            }
            // }
            // }
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    /**
     * RSI >= 60 MACD histogram is green Close Above EMA20
     *
     * @param stock
     * @return
     */
    private boolean isRsiMacdBreakout(Stock stock, Timeframe timeframe, Trend trend) {

        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);
        if (obvIndicatorService.isBullish(stockTechnicals)) {
            if (rsiTrendService.isBullish(stockTechnicals)) {
                if (macdIndicatorService.isHistogramGreen(stockTechnicals)
                        && macdIndicatorService.isHistogramIncreased(stockTechnicals)) {
                    if (macdIndicatorService.isSignalNearHistogram(stockTechnicals)) {
                        if (stockPriceServiceOld.isCloseAboveEma(stock, timeframe)) {
                            return Boolean.TRUE;
                        }
                    }
                }
            }
        }
        return Boolean.FALSE;
    }

    /**
     * uptrend TMA (EMA10, EMA20, EMA50) Convergence & Divergence ADX Increasing
     *
     * @param stock
     * @return
     */
    private boolean isTmaDivergence(Stock stock, Timeframe timeframe, Trend trend) {
        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);
        if (obvIndicatorService.isBullish(stockTechnicals)) {
            if (adxIndicatorService.isBullish(stockTechnicals)) {
                if (stockPriceServiceOld.isTmaDivergence(stock, timeframe, trend)) {
                    return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE;
    }

    private boolean isRsiMacdBreakdown(Stock stock, Timeframe timeframe, Trend trend) {

        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);
        if (obvIndicatorService.isBearish(stockTechnicals)) {
            if (rsiTrendService.isBearish(stockTechnicals)) {
                if (macdIndicatorService.isHistogramDecreased(stockTechnicals)) {
                    // if (macdIndicatorService.isSignalNearHistogram(stockTechnicals)) {
                    if (stockPriceServiceOld.isCloseBelowEma(stock, timeframe)) {
                        return Boolean.TRUE;
                    }
                    // }
                }
            }
        }
        return Boolean.FALSE;
    }

    private boolean isTmaConvergence(Stock stock, Timeframe timeframe, Trend trend) {
        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);
        if (obvIndicatorService.isBearish(stockTechnicals)) {
            if (adxIndicatorService.isBearish(stockTechnicals)) {
                if (stockPriceServiceOld.isTmaConvergence(stock, timeframe, trend)) {
                    return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE;
    }
}
