package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.TradeSetup;
import com.example.service.utils.*;
import com.example.util.FormulaService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service("basicPriceActionSignalEvaluator")
public class BasicPriceActionSignalEvaluator implements TradeSignalEvaluator {

    private final ResistanceValidationService resistanceValidationService;
    private final TimeframeSupportResistanceService timeframeSupportResistanceService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    private final StockPriceService<StockPrice> stockPriceService;
    private final SignalEvaluatorHelperService signalEvaluatorHelperService;
    private final RsiIndicatorService rsiIndicatorService;

    private final AdxIndicatorService adxIndicatorService;

    private final DynamicMovingAverageSupportResolverService
            dynamicMovingAverageSupportResolverService;

    private final FormulaService formulaService;

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

            subStrategyRef =
                    confirmBreakout(
                            timeframe,
                            stock,
                            stockPrice,
                            stockTechnicals,
                            evaluationResultOptional.get());

            double ema5 = stockTechnicals.getEma5();
            double avgPrice = (ema5 + stockPrice.getClose()) / 2;
            double weightedAvgPrice = formulaService.applyPercentChange(avgPrice, 0.5);
            double weightedClosePrice =
                    formulaService.applyPercentChange(
                            Math.max(stockPrice.getClose(), stockPrice.getOpen()), 0.25);
            researchPrice = Math.min(weightedAvgPrice, weightedClosePrice);
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

        if (timeframe == Timeframe.DAILY) {
            return Optional.empty();
        }

        if (MovingAverageUtil.increasingMaCount(stockTechnicals) < 3) {
            return Optional.empty();
        }

        if (!MovingAverageUtil.isAllMaAlignedBullish(
                stockTechnicals.getTimeframe(), stockTechnicals)) {
            return Optional.empty();
        }

        boolean isStrongBody =
                CandleStickUtils.isStrongBody(
                        stockPrice.getTimeframe(), stockPrice, stockTechnicals);

        boolean isStrongRange =
                CandleStickUtils.isStrongRange(
                        stockPrice.getTimeframe(), stockPrice, stockTechnicals);

        boolean isBreakout = false;
        if (evaluationResult.isBreakout()
                && evaluationResult.getLength() == MovingAverageLength.HIGHEST) {
            if ((isStrongBody || isStrongRange)) {

                // if (stockTechnicals.getSma200() < stockTechnicals.getEma50()) {
                // if (stockPrice.getClose() > stockTechnicals.getEma50()) {
                if (stockTechnicals.getVolumeAvg20() > stockTechnicals.getPrevVolumeAvg20()) {
                    if (stockTechnicals.getVolume() > stockTechnicals.getPrevVolume()) {
                        if (stockTechnicals.getVolume() > stockTechnicals.getVolumeAvg20()) {
                            if (stockTechnicals.getVolume()
                                    > stockTechnicals.getVolumeAvg20() * 1.5) {
                                isBreakout = true;
                            } else if (stockTechnicals.getVolume()
                                    > stockTechnicals.getPrevVolume() * 1.5) {

                                isBreakout = true;
                            } else if (stockTechnicals.getPrevVolume()
                                    > stockTechnicals.getPrevVolumeAvg20()) {
                                isBreakout = true;
                            }
                        }
                    }
                }
                // }
                // }
            }
        }

        if (isBreakout) {
            return SubStrategyHelper.resolveByName("breakout");
        }

        return Optional.empty();
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
