package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.TradeSetup;
import com.example.service.utils.*;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service("rangePriceActionSignalEvaluator")
public class RangePriceActionSignalEvaluator implements TradeSignalEvaluator {

    private final SignalEvaluatorHelperService signalEvaluatorHelperService;

    private final StockPriceService<StockPrice> stockPriceService;

    private final VolumeIndicatorService volumeIndicatorService;
    private final RsiIndicatorService rsiIndicatorService;

    @Override
    public TradeSetup evaluateEntry(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();
        double researchPrice = 0.0;
        Trend.Direction direction =
                TrendDirectionUtil.findDirection(
                        stockPriceService.buildPrevSessionStockPrice(stockPrice));

        if (direction == Trend.Direction.DOWN) {
            StockPrice htStockPrice =
                    stockPriceService.get(stock, timeframe.getHigher().getHigher());
            MArketConditionUtils.MarketCondition marketCondition =
                    MArketConditionUtils.detectCombinedMarketCondition(htStockPrice);
            if (marketCondition == MArketConditionUtils.MarketCondition.RANGING) {
                SupportResistanceZones supportResistanceZones =
                        SupportResistanceZoneUtils.calculateSupportResistanceZones(htStockPrice);

                boolean isFallsIntoSupportAndBouncesUp =
                        RangeActionUtils.fallsIntoSupportAndBouncesUp(
                                stockPrice, supportResistanceZones.getSupport());
                boolean isClosesAboveSupportZone =
                        RangeActionUtils.closesAboveSupportZone(
                                stockPrice, supportResistanceZones.getSupport());

                if (isFallsIntoSupportAndBouncesUp || isClosesAboveSupportZone) {
                    subStrategyRef = confirmSupport(timeframe, stock, stockPrice, stockTechnicals);
                }

                if (subStrategyRef.isPresent()) {

                    researchPrice =
                            signalEvaluatorHelperService.calculateEntryPrice(
                                    timeframe,
                                    stockPrice,
                                    stockTechnicals,
                                    isFallsIntoSupportAndBouncesUp
                                            ? supportResistanceZones.getSupport().getEnd()
                                            : supportResistanceZones.getSupport().getStart());

                    return TradeSetup.builder()
                            .active(Boolean.TRUE)
                            .strategy(ResearchTechnical.Strategy.RANGE)
                            .subStrategy(subStrategyRef.get())
                            .researchPrice(researchPrice)
                            .build();
                }
            }
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    private Optional<ResearchTechnical.SubStrategy> confirmSupport(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        log.debug("Confirming support for stock={} timeframe={}", stock.getNseSymbol(), timeframe);

        if (rsiIndicatorService.isOverBought(stockTechnicals)
                || CandleStickUtils.isUpperWickDominant(stockPrice)) {
            return Optional.empty();
        }

        boolean isBullishCandle =
                signalEvaluatorHelperService.isBullishCandle(stockPrice, stockTechnicals);
        boolean isVolumeSurge = volumeIndicatorService.isVolumeSurge(stockTechnicals);
        boolean isHighestMovingAverageDiffValid =
                signalEvaluatorHelperService.isLowestAndHighestMovingAverageDiffValid(
                        timeframe, stockPrice, stockTechnicals, MAInteractionType.SUPPORT, true);

        if (isBullishCandle && isVolumeSurge && isHighestMovingAverageDiffValid) {
            return SubStrategyHelper.resolveByName(timeframe.getHigher().getHigher() + "_support");
        }

        return Optional.empty();
    }

    @Override
    public TradeSetup evaluateExit(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();
        Trend.Direction direction =
                TrendDirectionUtil.findDirection(
                        stockPriceService.buildPrevSessionStockPrice(stockPrice));

        if (direction == Trend.Direction.UP) {

            StockPrice htStockPrice =
                    stockPriceService.get(stock, timeframe.getHigher().getHigher());
            MArketConditionUtils.MarketCondition marketCondition =
                    MArketConditionUtils.detectCombinedMarketCondition(htStockPrice);
            if (marketCondition == MArketConditionUtils.MarketCondition.RANGING) {
                SupportResistanceZones supportResistanceZones =
                        SupportResistanceZoneUtils.calculateSupportResistanceZones(htStockPrice);

                boolean isRisesIntoResistanceAndGetsRejected =
                        RangeActionUtils.risesIntoResistanceAndGetsRejected(
                                stockPrice, supportResistanceZones.getResistance());
                boolean isClosesBelowResistanceZone =
                        RangeActionUtils.closesBelowResistanceZone(
                                stockPrice, supportResistanceZones.getResistance());

                if (isRisesIntoResistanceAndGetsRejected || isClosesBelowResistanceZone) {
                    subStrategyRef =
                            confirmResistance(timeframe, stock, stockPrice, stockTechnicals);
                }

                if (subStrategyRef.isPresent()) {

                    return TradeSetup.builder()
                            .active(Boolean.TRUE)
                            .strategy(ResearchTechnical.Strategy.RANGE)
                            .subStrategy(subStrategyRef.get())
                            .build();
                }
            }
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    private Optional<ResearchTechnical.SubStrategy> confirmResistance(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {
        log.debug(
                "Confirming resistance for stock={} timeframe={}", stock.getNseSymbol(), timeframe);

        if (rsiIndicatorService.isOverSold(stockTechnicals)
                || CandleStickUtils.isLowerWickDominant(stockPrice)) {
            return Optional.empty();
        }

        boolean isBearishCandle =
                signalEvaluatorHelperService.isBearishCandle(stockPrice, stockTechnicals);
        boolean isVolumeSurge = volumeIndicatorService.isVolumeSurge(stockTechnicals);

        if (isBearishCandle && isVolumeSurge) {
            return SubStrategyHelper.resolveByName(
                    timeframe.getHigher().getHigher() + "_resistance");
        }

        return Optional.empty();
    }
}
