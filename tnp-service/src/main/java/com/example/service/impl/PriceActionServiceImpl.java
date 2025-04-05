package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.BreakoutLedger;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.TradeSetup;
import com.example.service.*;
import com.example.service.StockPriceService;
import com.example.service.StockTechnicalsService;
import com.example.util.FormulaService;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PriceActionServiceImpl implements PriceActionService {

    @Autowired private SupportResistanceUtilService supportResistanceService;
    @Autowired private CandleStickConfirmationService candleStickHelperService;

    @Autowired private CandleStickService candleStickService;

    @Autowired private BreakoutLedgerService breakoutLedgerService;

    @Autowired private StockPriceServiceOld stockPriceServiceOld;
    @Autowired private RelevanceService relevanceService;
    @Autowired private FormulaService formulaService;
    @Autowired private TrendService trendService;

    @Autowired private RsiIndicatorService rsiIndicatorService;

    @Autowired private StockPriceService<StockPrice> stockPriceService;

    @Autowired private StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    @Override
    public TradeSetup breakOut(Stock stock, Timeframe timeframe) {

        StockPrice stockPrice = stockPriceService.get(stock, timeframe);
        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);
        if (candleStickService.isDead(stockPrice)) {
            return TradeSetup.builder().active(Boolean.FALSE).build();
        }

        boolean isCandleActive = Boolean.FALSE;
        Trend trend = trendService.detect(stock, timeframe);

        log.info(
                "{} [{}]: Scanning breakout | Direction: {}, Momentum: {}",
                stock.getNseSymbol(),
                timeframe,
                trend.getDirection(),
                trend.getMomentum());

        AtomicReference<ResearchTechnical.SubStrategy> subStrategyRef = new AtomicReference<>();

        if (trend.getDirection() != Trend.Direction.INVALID) {

            isCandleActive =
                    this.isBullishAction(
                            trend, timeframe, stockPrice, stockTechnicals, subStrategyRef);
        }

        if (isCandleActive) {
            log.info(
                    "{} bullish candlestick confirmed using {}:{}",
                    stock.getNseSymbol(),
                    ResearchTechnical.Strategy.PRICE,
                    subStrategyRef.get());

            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchTechnical.Strategy.PRICE)
                    .subStrategy(subStrategyRef.get())
                    .build();
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
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

        Trend.Momentum momentum = trend.getMomentum();

        boolean isBullishCandleStick =
                candleStickHelperService.isBullishConfirmed(timeframe, stockPrice, stockTechnicals);

        if (relevanceService.isNearSupport(trend, timeframe, stockPrice, stockTechnicals)) {
            if (isBullishCandleStick
                    || EnumSet.of(
                                    Trend.Momentum.DIP,
                                    Trend.Momentum.PULLBACK,
                                    Trend.Momentum.CORRECTION)
                            .contains(momentum)) {
                subStrategyRef.set(
                        isBullishCandleStick
                                ? ResearchTechnical.SubStrategy.STRONG_SUPPORT
                                : ResearchTechnical.SubStrategy.WEAK_SUPPORT);
                return true;
            }
        }

        if (relevanceService.isBreakout(trend, timeframe, stockPrice, stockTechnicals)) {
            if (isBullishCandleStick
                    || EnumSet.of(
                                    Trend.Momentum.RECOVERY,
                                    Trend.Momentum.ADVANCE,
                                    Trend.Momentum.TOP)
                            .contains(momentum)) {
                subStrategyRef.set(
                        isBullishCandleStick
                                ? ResearchTechnical.SubStrategy.STRONG_BREAKOUT
                                : ResearchTechnical.SubStrategy.WEAK_BREAKOUT);
                return true;
            }
        }

        if (relevanceService.isBullishIndicator(trend, timeframe, stockPrice, stockTechnicals)) {
            subStrategyRef.set(ResearchTechnical.SubStrategy.BULLISH_INDICATORS);
            return true;
        }

        return false;
    }

    @Override
    public TradeSetup breakDown(Stock stock, Timeframe timeframe) {

        StockPrice stockPrice = stockPriceService.get(stock, timeframe);
        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);
        if (candleStickService.isDead(stockPrice)) {
            return TradeSetup.builder().active(Boolean.FALSE).build();
        }

        boolean isCandleActive = Boolean.FALSE;

        Trend trend = trendService.detect(stock, timeframe);
        log.info(
                "{} [{}]: Scanning breakdown | Direction: {}, Momentum: {}",
                stock.getNseSymbol(),
                timeframe,
                trend.getDirection(),
                trend.getMomentum());

        AtomicReference<ResearchTechnical.SubStrategy> subStrategyRef = new AtomicReference<>();

        if (trend.getDirection() != Trend.Direction.INVALID) {
            isCandleActive =
                    this.isBearishAction(
                            trend, timeframe, stockPrice, stockTechnicals, subStrategyRef);
        }

        if (isCandleActive) {
            log.info(
                    "{} bearish candlestick confirmed using {}:{}",
                    stock.getNseSymbol(),
                    ResearchTechnical.Strategy.PRICE,
                    subStrategyRef.get());

            breakoutLedgerService.addNegative(
                    stock, timeframe, BreakoutLedger.BreakoutCategory.BREAKDOWN_CANDLESTICK);
            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchTechnical.Strategy.PRICE)
                    .subStrategy(subStrategyRef.get())
                    .build();
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
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

        Trend.Momentum momentum = trend.getMomentum();

        boolean isBearishCandleStick =
                candleStickHelperService.isBearishConfirmed(timeframe, stockPrice, stockTechnicals);

        if (relevanceService.isNearResistance(trend, timeframe, stockPrice, stockTechnicals)) {
            if (isBearishCandleStick
                    || EnumSet.of(
                                    Trend.Momentum.RECOVERY,
                                    Trend.Momentum.ADVANCE,
                                    Trend.Momentum.TOP)
                            .contains(momentum)) {
                subStrategyRef.set(
                        isBearishCandleStick
                                ? ResearchTechnical.SubStrategy.STRONG_RESISTANCE
                                : ResearchTechnical.SubStrategy.WEAK_RESISTANCE);
                return true;
            }
        }

        if (relevanceService.isBreakdown(trend, timeframe, stockPrice, stockTechnicals)) {
            if (isBearishCandleStick
                    || EnumSet.of(
                                    Trend.Momentum.DIP,
                                    Trend.Momentum.PULLBACK,
                                    Trend.Momentum.CORRECTION)
                            .contains(momentum)) {
                subStrategyRef.set(
                        isBearishCandleStick
                                ? ResearchTechnical.SubStrategy.STRONG_BREAKDOWN
                                : ResearchTechnical.SubStrategy.WEAK_BREAKDOWN);
                return true;
            }
        }

        if (relevanceService.isBearishIndicator(trend, timeframe, stockPrice, stockTechnicals)) {
            subStrategyRef.set(ResearchTechnical.SubStrategy.BEARISH_INDICATORS);
            return true;
        }

        return false;
    }
}
