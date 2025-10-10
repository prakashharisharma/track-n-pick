package com.example.service.strategy;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.*;
import com.example.dto.common.OHLCV;
import com.example.dto.common.TradeSetup;
import com.example.service.*;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
import com.example.service.utils.SubStrategyHelper;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** 1. Breakout EMA5 2. Align Bearish 3. Higher Timeframe Align Bullish */
@Slf4j
@RequiredArgsConstructor
@Service("bottomPriceActionSignalEvaluator")
public class BottomPriceActionSignalEvaluator implements TradeSignalEvaluator {

    private final DynamicMovingAverageSupportResolverService
            dynamicMovingAverageSupportResolverService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    private final MonthlySupportResistanceService monthlySupportResistanceService;
    private final WeeklySupportResistanceService weeklySupportResistanceService;
    private final StockPriceService<StockPrice> stockPriceService;
    private final EvaluationLogService evaluationLogService;

    @Override
    public TradeSetup evaluateEntry(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        Optional<MAEvaluationResult> evaluationResultOptional =
                dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                        timeframe, stockPrice, stockTechnicals, true);

        if (evaluationResultOptional.isPresent()) {
            MAEvaluationResult evaluationResult = evaluationResultOptional.get();
            // Breakout EMA5
            if (evaluationResult.isBreakout()) {
                Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();
                evaluationLogService.add(
                        stockPrice,
                        EvaluationLog.Type.POSITIVE,
                        timeframe.name()
                                + "-"
                                + ResearchTechnical.Strategy.BOTTOM.name()
                                + " breakout found"
                                + " on "
                                + evaluationResult.getLength());

                subStrategyRef =
                        confirmBreakout(
                                timeframe, stock, stockPrice, stockTechnicals, evaluationResult);

                if (subStrategyRef.isPresent()) {
                    return TradeSetup.builder()
                            .active(Boolean.TRUE)
                            .strategy(ResearchTechnical.Strategy.BOTTOM)
                            .subStrategy(subStrategyRef.get())
                            .build();
                }
            }
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

        // MA aligned Bearish
        if (MovingAverageUtil.isAllMaAlignedBearish(timeframe, stockTechnicals)) {
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
            int higherMAIncreasingCount = MovingAverageUtil.increasingMaCount(stockTechnicalsHt);

            boolean isHtMAAlignBullishWithIncrCount =
                    isHigherMaAlignedBullish && higherMAIncreasingCount > 3;

            if (isHtMAAlignBullishWithIncrCount
                    || this.isHigherTimeframeOpenGreaterThanEqualClose(stock, stockPriceHt)) {
                boolean isStrongBody =
                        CandleStickUtils.isStrongBody(
                                stockPrice.getTimeframe(), stockPrice, stockTechnicals);

                boolean isStrongRange =
                        CandleStickUtils.isStrongRange(
                                stockPrice.getTimeframe(), stockPrice, stockTechnicals);

                if (isStrongBody || isStrongRange) {
                    int decreasingMACount = MovingAverageUtil.decreasingMaCount(stockTechnicals);
                    if (this.isVolumeSurge(stockTechnicals) && decreasingMACount >= 3) {
                        System.out.println("Found Bottom " + stock.getNseSymbol());
                        return SubStrategyHelper.resolveByName(
                                evaluationResult.getLength().name() + "_breakout");
                    }
                }
            }
        }

        return Optional.empty();
    }

    private boolean isHigherTimeframeOpenGreaterThanEqualClose(
            Stock stock, StockPrice stockPriceHt) {

        OHLCV ohlcv = null;
        if (stockPriceHt.getTimeframe() == Timeframe.MONTHLY) {
            LocalDate firstOfMonth = LocalDate.now().withDayOfMonth(1);
            ohlcv =
                    monthlySupportResistanceService.supportAndResistance(
                            stock.getNseSymbol(), firstOfMonth, LocalDate.now());
        } else if (stockPriceHt.getTimeframe() == Timeframe.WEEKLY) {
            LocalDate firstDayOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
            ohlcv =
                    weeklySupportResistanceService.supportAndResistance(
                            stock.getNseSymbol(), firstDayOfWeek, LocalDate.now());
        }

        return ohlcv != null && ohlcv.getOpen() >= stockPriceHt.getClose();
    }

    private boolean isVolumeSurge(StockTechnicals stockTechnicals) {
        long volume = stockTechnicals.getVolume();
        long prevVolume = stockTechnicals.getPrevVolume();
        long avgVolume = stockTechnicals.getVolumeAvg20();
        long prevAvgVolume = stockTechnicals.getPrevVolumeAvg20();

        if (volume >= avgVolume * 2) {
            if (avgVolume > prevAvgVolume) {
                if (volume > prevVolume) {
                    return true;
                }
            }
        } else if (volume >= avgVolume * 1.5) {
            if (volume >= 2 * prevVolume) {
                return true;
            }
        } else if (volume >= avgVolume) {
            if (volume >= 3 * prevVolume) {
                return true;
            }
        } else if (avgVolume > prevAvgVolume) {
            if (prevVolume > prevAvgVolume) {
                if (prevVolume >= volume * 1.5) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public TradeSetup evaluateExit(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        return TradeSetup.builder().active(Boolean.FALSE).build();
    }
}
