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
import com.example.service.utils.CandleStickUtils;
import com.example.util.FormulaService;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicReference;
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
    @Autowired private StockPriceHelperService stockPriceHelperService;

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

        StockPrice stockPriceHigherTF = stockPriceService.get(stock, timeframe.getHigher());
        StockTechnicals stockTechnicalsHigherTF =
                stockTechnicalsService.get(stock, timeframe.getHigher());

        Trend trend = trendService.detect(stock, timeframe);

        log.info(
                "{} : Scanning swing action breakout Direction: {}, Momentum:{}",
                stock.getNseSymbol(),
                trend.getDirection(),
                trend.getMomentum());

        AtomicReference<ResearchTechnical.SubStrategy> subStrategyRef = new AtomicReference<>();

        if (isHigherTimeframeBullish(
                timeframe.getHigher(), stockPriceHigherTF, stockTechnicalsHigherTF)) {
            if (isCurrentTimeframeBullish(
                    trend, timeframe, stockPrice, stockTechnicals, subStrategyRef)) {
                return TradeSetup.builder()
                        .active(true)
                        .strategy(ResearchTechnical.Strategy.SWING)
                        .subStrategy(subStrategyRef.get())
                        .build();
            }
        }

        return TradeSetup.builder().active(false).build();
    }

    private boolean isHigherTimeframeBullish(
            Timeframe timeframe, StockPrice price, StockTechnicals techs) {
        boolean aboveEma =
                stockPriceHelperService.isAboveEma20(price, techs)
                        && stockPriceHelperService.isAboveEma50(price, techs);

        boolean trendStructure = CandleStickUtils.isHigherHighAndHigherLow(price);
        boolean adxBullish = adxIndicatorService.isBullish(techs);
        boolean rsiBullish = rsiTrendService.isBullish(techs);
        boolean isBearishCandleStick =
                candleStickHelperService.isBearishConfirmed(timeframe, price, techs);

        if (!aboveEma) log.debug("Failed EMA alignment");
        if (!trendStructure) log.debug("Failed HH-HL structure");
        if (!adxBullish) log.debug("Failed ADX bullish condition");
        if (!rsiBullish) log.debug("Failed RSI bullish condition");

        return aboveEma && trendStructure && adxBullish && rsiBullish && !isBearishCandleStick;
    }

    private boolean isCurrentTimeframeBullish(
            Trend trend,
            Timeframe tf,
            StockPrice price,
            StockTechnicals techs,
            AtomicReference<ResearchTechnical.SubStrategy> subRef) {
        boolean action = isBullishAction(trend, tf, price, techs, subRef);

        if (!action) log.debug("No bullish action detected");

        return action;
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

        StockPrice stockPriceHigherTF = stockPriceService.get(stock, timeframe.getHigher());
        StockTechnicals stockTechnicalsHigherTF =
                stockTechnicalsService.get(stock, timeframe.getHigher());

        Trend trend = trendService.detect(stock, timeframe);

        log.info(
                "{} : Scanning swing action breakdown Direction: {}, Momentum:{}",
                stock.getNseSymbol(),
                trend.getDirection(),
                trend.getMomentum());

        AtomicReference<ResearchTechnical.SubStrategy> subStrategyRef = new AtomicReference<>();

        if (isHigherTimeframeBearish(
                timeframe.getHigher(), stockPriceHigherTF, stockTechnicalsHigherTF)) {
            if (isCurrentTimeframeBearish(
                    trend, timeframe, stockPrice, stockTechnicals, subStrategyRef)) {
                return TradeSetup.builder()
                        .active(true)
                        .strategy(ResearchTechnical.Strategy.SWING)
                        .subStrategy(subStrategyRef.get())
                        .build();
            }
        }

        return TradeSetup.builder().active(false).build();
    }

    private boolean isHigherTimeframeBearish(
            Timeframe timeframe, StockPrice price, StockTechnicals techs) {
        boolean belowEma =
                stockPriceHelperService.isBelowEma20(price, techs)
                        && stockPriceHelperService.isBelowEma50(price, techs);

        boolean trendStructure = CandleStickUtils.isLowerHighAndLowerLow(price);
        boolean adxBearish = adxIndicatorService.isBearish(techs);
        boolean rsiBearish = rsiTrendService.isBearish(techs);
        boolean isBullishCandleStick =
                candleStickHelperService.isBullishConfirmed(timeframe, price, techs);

        if (!belowEma) log.debug("Failed EMA alignment (bearish)");
        if (!trendStructure) log.debug("Failed LH-LL structure");
        if (!adxBearish) log.debug("Failed ADX bearish condition");
        if (!rsiBearish) log.debug("Failed RSI bearish condition");

        return belowEma && trendStructure && adxBearish && rsiBearish && !isBullishCandleStick;
    }

    private boolean isCurrentTimeframeBearish(
            Trend trend,
            Timeframe tf,
            StockPrice price,
            StockTechnicals techs,
            AtomicReference<ResearchTechnical.SubStrategy> subRef) {
        boolean action = isBearishAction(trend, tf, price, techs, subRef);

        if (!action) log.debug("No bearish action detected");

        return action;
    }

    private boolean isBullishAction(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            AtomicReference<ResearchTechnical.SubStrategy> subStrategyRef) {

        boolean isUpperWickSizeConfirmed =
                candleStickHelperService.isUpperWickSizeConfirmed(
                        timeframe, stockPrice, stockTechnicals);

        if (!isUpperWickSizeConfirmed) {
            return false;
        }

        Trend.Phase phase = trend.getMomentum();

        boolean isBullishCandleStick =
                candleStickHelperService.isBullishConfirmed(timeframe, stockPrice, stockTechnicals);
        boolean isBearishCandleStick =
                candleStickHelperService.isBearishConfirmed(timeframe, stockPrice, stockTechnicals);
        boolean isVolumeSurge =
                volumeIndicatorService.isBullish(stockPrice, stockTechnicals, timeframe);

        if (isBullishCandleStick
                || ((EnumSet.of(Trend.Phase.DIP, Trend.Phase.PULLBACK, Trend.Phase.CORRECTION)
                                .contains(phase))
                        && !isBearishCandleStick
                        && isVolumeSurge)) {

            subStrategyRef.set(
                    isBullishCandleStick
                            ? ResearchTechnical.SubStrategy.STRONG_SWING
                            : ResearchTechnical.SubStrategy.WEAK_SWING);
            return true;
        }

        return false;
    }

    private boolean isBearishAction(
            Trend trend,
            Timeframe timeframe,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            AtomicReference<ResearchTechnical.SubStrategy> subStrategyRef) {

        boolean isLowerWickSizeConfirmed =
                candleStickHelperService.isLowerWickSizeConfirmed(
                        timeframe, stockPrice, stockTechnicals);

        if (!isLowerWickSizeConfirmed) {
            return false;
        }

        Trend.Phase phase = trend.getMomentum();

        boolean isBearishCandleStick =
                candleStickHelperService.isBearishConfirmed(timeframe, stockPrice, stockTechnicals);

        boolean isBullishCandleStick =
                candleStickHelperService.isBullishConfirmed(timeframe, stockPrice, stockTechnicals);
        boolean isVolumeSurge =
                volumeIndicatorService.isBullish(stockPrice, stockTechnicals, timeframe);

        if (isBearishCandleStick
                || ((EnumSet.of(Trend.Phase.RECOVERY, Trend.Phase.ADVANCE, Trend.Phase.TOP)
                                .contains(phase))
                        && !isBullishCandleStick
                        && isVolumeSurge)) {

            subStrategyRef.set(
                    isBearishCandleStick
                            ? ResearchTechnical.SubStrategy.STRONG_SWING
                            : ResearchTechnical.SubStrategy.WEAK_SWING);
            return true;
        }
        /*
        if (relevanceService.isBreakdown(trend, timeframe, stockPrice, stockTechnicals)) {
            if (isBearishCandleStick
                    || (higherTrend.getMomentum().isHigherPriorityThan(trend.getMomentum())
                    && !isBullishCandleStick
                            && isVolumeSurge)) {*/

        return false;
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
                        if (stockPriceHelperService.isCloseAboveEma(stock, timeframe)) {
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
                if (stockPriceHelperService.isTmaDivergence(stock, timeframe, trend)) {
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
                    if (stockPriceHelperService.isCloseBelowEma(stock, timeframe)) {
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
                if (stockPriceHelperService.isTmaConvergence(stock, timeframe, trend)) {
                    return Boolean.TRUE;
                }
            }
        }

        return Boolean.FALSE;
    }
}
