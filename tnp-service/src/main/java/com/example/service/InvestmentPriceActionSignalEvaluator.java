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
@Service("investmentPriceActionSignalEvaluator")
public class InvestmentPriceActionSignalEvaluator implements TradeSignalEvaluator {

    private final SignalEvaluatorHelperService signalEvaluatorHelperService;

    private final StockPriceService<StockPrice> stockPriceService;

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
            if (stockPrice.getClose()
                    < MovingAverageUtil.getMovingAverage5(timeframe, stockTechnicals)) {
                if (stockPrice.getClose()
                        < MovingAverageUtil.getMovingAverage200(timeframe, stockTechnicals)) {
                    subStrategyRef = confirmBreakout(timeframe, stock, stockPrice, stockTechnicals);
                }
            }
        }

        if (subStrategyRef.isPresent()) {

            double researchPrice = stockPrice.getHigh() + stockPrice.getLow() / 2;

            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchTechnical.Strategy.INVESTMENT)
                    .subStrategy(subStrategyRef.get())
                    .researchPrice(researchPrice)
                    .build();
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    private Optional<ResearchTechnical.SubStrategy> confirmBreakout(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        log.debug("Confirming breakout for stock={} timeframe={}", stock.getNseSymbol(), timeframe);

        boolean isLowestAndHighestMovingAverageDiffValid =
                signalEvaluatorHelperService.isLowestAndHighestMovingAverageDiffValid(
                        timeframe, stockPrice, stockTechnicals, MAInteractionType.BREAKOUT, true);

        boolean isStrongRange =
                CandleStickUtils.isStrongRange(timeframe, stockPrice, stockTechnicals);
        boolean isStrongBody =
                CandleStickUtils.isStrongBody(timeframe, stockPrice, stockTechnicals);

        boolean isHighestAndHighMovingAverageDiffValid =
                signalEvaluatorHelperService.isHighAndHighestMovingAverageDiffValid(
                        timeframe, stockPrice, stockTechnicals, MAInteractionType.BREAKOUT, true);

        if (!isHighestAndHighMovingAverageDiffValid) {
            return Optional.empty();
        }

        if (isLowestAndHighestMovingAverageDiffValid && (isStrongRange || isStrongBody)) {

            if (CandleStickUtils.isStrongLowerWick(stockPrice)
                    || CandleStickUtils.isLowerWickDominant(stockPrice)
                    || CandleStickUtils.isLowerWickLongerThanUpperWick(stockPrice)) {
                if (stockTechnicals.getVolumeAvg20() > stockTechnicals.getPrevVolumeAvg20()) {
                    if (stockTechnicals.getVolume() > 1.5 * stockTechnicals.getVolumeAvg20()) {
                        if ((stockTechnicals.getVolume() > 1.5 * stockTechnicals.getPrevVolume())
                                || (stockTechnicals.getVolume() > stockTechnicals.getPrevVolume()
                                        && stockTechnicals.getPrevVolume()
                                                > 1.5 * stockTechnicals.getPrevVolumeAvg20())) {
                            return SubStrategyHelper.resolveByName("support");
                        }
                    }
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
