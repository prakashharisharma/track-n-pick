package com.example.service.strategy;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.TradeSetup;
import com.example.service.*;
import com.example.service.utils.*;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 1. Breakout - MONTHLY - EMA5, WEEKLY - EMA20, DAILY - EMA50 2. Aligned Bullish 3. Higher
 * Timeframe Aligned Bullish
 */
@Slf4j
@RequiredArgsConstructor
@Service("basicPriceActionSignalEvaluator")
public class BasicPriceActionSignalEvaluator implements TradeSignalEvaluator {

    private final DynamicMovingAverageSupportResolverService
            dynamicMovingAverageSupportResolverService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    private final StockPriceService<StockPrice> stockPriceService;

    @Override
    public TradeSetup evaluateEntry(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();
        double researchPrice = 0.0;

        Optional<MAEvaluationResult> evaluationResultOptional =
                dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                        stockPrice.getTimeframe(), stockPrice, stockTechnicals, false);

        if (evaluationResultOptional.isPresent()) {

            MAEvaluationResult evaluationResult = evaluationResultOptional.get();

            if (evaluationResult.isBreakout()) {
                subStrategyRef =
                        confirmBreakout(
                                timeframe, stock, stockPrice, stockTechnicals, evaluationResult);
            }
        }

        if (subStrategyRef.isPresent()) {
            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .researchPrice(researchPrice)
                    .strategy(ResearchTechnical.Strategy.BASIC)
                    .subStrategy(subStrategyRef.get())
                    .build();
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    @Override
    public TradeSetup evaluateExit(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();

        Optional<MAEvaluationResult> evaluationResultOptional =
                dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                        stockPrice.getTimeframe(), stockPrice, stockTechnicals, false);

        if (evaluationResultOptional.isPresent()) {

            subStrategyRef =
                    confirmBreakdown(
                            timeframe,
                            stock,
                            stockPrice,
                            stockTechnicals,
                            evaluationResultOptional.get());
        }

        if (subStrategyRef.isPresent()) {
            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchTechnical.Strategy.BASIC)
                    .subStrategy(subStrategyRef.get())
                    .build();
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    private Optional<ResearchTechnical.SubStrategy> confirmBreakout(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult) {

        log.debug(
                "Confirming breakout for stock={} timeframe={}",
                stock.getNseSymbol(),
                stockPrice.getTimeframe());

        if (timeframe == Timeframe.DAILY
                && evaluationResult.getLength() == MovingAverageLength.HIGHEST) {
            return Optional.empty();
        }

        if (timeframe == Timeframe.WEEKLY
                && evaluationResult.getLength() == MovingAverageLength.HIGHEST) {
            return Optional.empty();
        }

        if ((MovingAverageUtil.isAllMaAlignedBullish(
                        stockTechnicals.getTimeframe(), stockTechnicals)
                && MovingAverageUtil.increasingMaCount(stockTechnicals) >= 3)) {

            if (CandleStickUtils.isPrevSessionRed(stockPrice)) {

                StockTechnicals stockTechnicalsHt =
                        stockTechnicalsService.get(stock, timeframe.getHigher());
                StockPrice stockPriceHt = stockPriceService.get(stock, timeframe.getHigher());

                if (stockPriceHt == null || stockTechnicalsHt == null) {
                    return Optional.empty();
                }
                // Higher timeframe aligned bullish and incr count = 4
                boolean isHigherMaAlignedBullish =
                        MovingAverageUtil.isAllMaAlignedBullish(
                                timeframe.getHigher(), stockTechnicalsHt);
                int higherMAIncreasingCount =
                        MovingAverageUtil.increasingMaCount(stockTechnicalsHt);

                boolean isHtMAAlignBullishWithIncrCount =
                        isHigherMaAlignedBullish && higherMAIncreasingCount > 3;

                if (isHtMAAlignBullishWithIncrCount) {
                    boolean isStrongBody =
                            CandleStickUtils.isStrongBody(
                                    stockPrice.getTimeframe(), stockPrice, stockTechnicals);

                    boolean isStrongRange =
                            CandleStickUtils.isStrongRange(
                                    stockPrice.getTimeframe(), stockPrice, stockTechnicals);
                    if (isStrongBody || isStrongRange) {
                        if (this.isVolumeSurge(stockTechnicals)) {
                            return SubStrategyHelper.resolveByName(
                                    evaluationResult.getLength().name() + "_breakout");
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    private boolean isVolumeSurge(StockTechnicals stockTechnicals) {

        long avgVolume = stockTechnicals.getVolumeAvg20();
        long prevAvgVolume = stockTechnicals.getPrevVolumeAvg20();
        long volume = stockTechnicals.getVolume();
        long prevVolume = stockTechnicals.getPrevVolume();

        if (avgVolume > prevAvgVolume) {
            if (volume > prevVolume) {
                if (volume > avgVolume) {
                    if (volume > avgVolume * 1.5) {
                        return true;
                    } else if (volume > prevVolume * 1.5) {
                        return true;
                    } else if (prevVolume > prevAvgVolume) {
                        return true;
                    }
                }
            } else if (prevVolume > prevAvgVolume) {
                if (prevVolume >= volume * 1.5) {
                    return true;
                }
            }
        }

        return false;
    }

    private Optional<ResearchTechnical.SubStrategy> confirmBreakdown(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            MAEvaluationResult evaluationResult) {
        log.debug(
                "Confirming breakdown for stock={} timeframe={}", stock.getNseSymbol(), timeframe);

        if (timeframe == Timeframe.DAILY) {
            return Optional.empty();
        }

        if (CandleStickUtils.isLowerWickDominant(stockPrice)
                || CandleStickUtils.isStrongLowerWick(stockPrice)) {
            if (CandleStickUtils.isPrevSessionRed(stockPrice)) {
                return Optional.empty();
            }
        }

        if ((evaluationResult.isBreakdown()
                        || MovingAverageUtil.getMovingAverage5(timeframe, stockTechnicals)
                                > stockPrice.getClose())
                && (stockPrice.getPrevOpen() > stockPrice.getPrevClose())
                && (stockPrice.getOpen() > stockPrice.getClose())) {
            return SubStrategyHelper.resolveByName("breakdown");
        }

        return Optional.empty();
    }
}
