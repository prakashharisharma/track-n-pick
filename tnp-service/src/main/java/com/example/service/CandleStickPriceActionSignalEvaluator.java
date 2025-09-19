package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.TradeSetup;
import com.example.service.impl.CandleStickConfirmationServiceImpl;
import com.example.service.utils.*;
import com.example.util.FormulaService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service("candleStickPriceActionSignalEvaluator")
public class CandleStickPriceActionSignalEvaluator implements TradeSignalEvaluator {

    private final StockPriceService<StockPrice> stockPriceService;
    private final FormulaService formulaService;

    private final RsiIndicatorService rsiIndicatorService;

    private final SignalEvaluatorHelperService signalEvaluatorHelperService;

    private final CandleStickConfirmationServiceImpl candleStickConfirmationService;
    private final DynamicMovingAverageSupportResolverService
            dynamicMovingAverageSupportResolverService;

    @Override
    public TradeSetup evaluateEntry(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();
        if (TrendDirectionUtil.findDirection(stockPrice) == Trend.Direction.DOWN
                || TrendDirectionUtil.findDirection(
                                stockPriceService.buildPrevSessionStockPrice(stockPrice))
                        == Trend.Direction.DOWN) {
            boolean isBullishConfirmed =
                    candleStickConfirmationService.isBullishConfirmed(
                            stockPrice.getTimeframe(), stockPrice, stockTechnicals, false);
            if (isBullishConfirmed) {
                Optional<MAEvaluationResult> evaluationResultOptional =
                        dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                                timeframe, stockPrice, stockTechnicals, true);
                if (evaluationResultOptional.isPresent()) {
                    MAEvaluationResult evaluationResult = evaluationResultOptional.get();
                    if (evaluationResult.isBreakout() || evaluationResult.isNearSupport()) {
                        subStrategyRef =
                                confirmBreakout(
                                        timeframe,
                                        stock,
                                        stockPrice,
                                        stockTechnicals,
                                        evaluationResult);
                    }
                }
            }
        }
        if (subStrategyRef.isPresent()) {

            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchTechnical.Strategy.CANDLESTICK)
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

        log.debug("Confirming breakout for stock={} timeframe={}", stock.getNseSymbol(), timeframe);

        if (timeframe != Timeframe.MONTHLY) {
            if (!signalEvaluatorHelperService.isHigherTimeframeConfirmed(stockTechnicals, false)) {
                return Optional.empty();
            }
        }

        if (evaluationResult.isBreakout()
                && !(stockPrice.getPrevOpen() < evaluationResult.getPrevValue()
                        && stockPrice.getPrevClose() < evaluationResult.getPrevValue())) {
            return Optional.empty();
        }

        if (evaluationResult.isNearSupport()) {
            if (stockPrice.getPrevOpen() <= evaluationResult.getPrevValue()
                    || stockPrice.getPrevClose() <= evaluationResult.getPrevValue()) {
                return Optional.empty();
            }
        }

        StockPrice prevStockPrice = stockPriceService.buildPrevSessionStockPrice(stockPrice);

        boolean isPrevLowerHighAndLowerLow =
                CandleStickUtils.isLowerHigh(prevStockPrice)
                        && CandleStickUtils.isLowerLow(prevStockPrice);

        if (!isPrevLowerHighAndLowerLow) {
            return Optional.empty();
        }

        int incrMACount = MovingAverageUtil.increasingMaCount(stockTechnicals);

        if (incrMACount == 5) {
            if (rsiIndicatorService.rsi(stockTechnicals) > 60) {
                return Optional.empty();
            }
        }

        boolean isStrongBody =
                CandleStickUtils.isStrongBody(
                        stockPrice.getTimeframe(), stockPrice, stockTechnicals);

        boolean isStrongRange =
                CandleStickUtils.isStrongRange(
                        stockPrice.getTimeframe(), stockPrice, stockTechnicals);

        if (isStrongBody || isStrongRange) {

            if (stockTechnicals.getVolume() > stockTechnicals.getPrevVolume()
                    && stockTechnicals.getPrevVolume() > stockTechnicals.getPrev2Volume()) {
                if (stockTechnicals.getVolume() > stockTechnicals.getPrevVolume() * 1.5) {
                    if (stockTechnicals.getVolume() > stockTechnicals.getVolumeAvg20() * 1.5) {
                        return SubStrategyHelper.resolveByName("support");
                    }
                }
            } else if (stockTechnicals.getVolumeAvg20() > stockTechnicals.getPrevVolumeAvg20()
                    && stockTechnicals.getPrevVolumeAvg20()
                            > stockTechnicals.getPrev2VolumeAvg20()) {
                if (stockTechnicals.getVolume() > stockTechnicals.getVolumeAvg20() * 1.5) {
                    return SubStrategyHelper.resolveByName("support");
                } else if ((stockTechnicals.getVolume() > stockTechnicals.getVolumeAvg20() * 1.15)
                        && (stockTechnicals.getVolume() > stockTechnicals.getPrevVolume() * 1.15)) {
                    return SubStrategyHelper.resolveByName("support");
                } else if ((stockTechnicals.getVolume() > stockTechnicals.getVolumeAvg20())
                        && (stockTechnicals.getPrevVolume() > stockTechnicals.getPrevVolumeAvg20())
                        && (stockTechnicals.getPrev2Volume()
                                > stockTechnicals.getPrev2VolumeAvg20())) {
                    return SubStrategyHelper.resolveByName("support");
                }
            } else if (stockTechnicals.getVolumeAvg20() > stockTechnicals.getPrevVolumeAvg20()) {
                if (stockTechnicals.getVolume() > stockTechnicals.getPrevVolume() * 5) {
                    if (stockTechnicals.getVolume() > stockTechnicals.getVolumeAvg20() * 2) {
                        return SubStrategyHelper.resolveByName("support");
                    }
                }
            } else if (stockTechnicals.getVolume() > stockTechnicals.getVolumeAvg20() * 2) {
                if (stockTechnicals.getVolume() > stockTechnicals.getPrevVolume() * 1.5) {
                    return SubStrategyHelper.resolveByName("support");
                }
            } else if (stockTechnicals.getVolume() > stockTechnicals.getVolumeAvg20() * 1.5) {
                if (stockTechnicals.getVolume() > stockTechnicals.getPrevVolume() * 2) {
                    return SubStrategyHelper.resolveByName("support");
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public TradeSetup evaluateExit(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        return null;
    }
}
