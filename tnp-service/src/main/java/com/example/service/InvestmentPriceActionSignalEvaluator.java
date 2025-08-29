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

    private final CandleStickConfirmationService candleStickConfirmationService;

    private final VolumeIndicatorService volumeIndicatorService;

    private final RsiIndicatorService rsiIndicatorService;

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
            // System.out.println("Log1 " + stock.getNseSymbol());
            if (stockPrice.getClose()
                    < MovingAverageUtil.getMovingAverage5(timeframe, stockTechnicals)) {
                if (stockPrice.getClose()
                        < MovingAverageUtil.getMovingAverage200(timeframe, stockTechnicals)) {
                    // System.out.println("Log2 " + stock.getNseSymbol());
                    subStrategyRef = confirmBreakout(timeframe, stock, stockPrice, stockTechnicals);
                }
            }
        }

        if (subStrategyRef.isPresent()) {

            double researchPrice = stockPrice.getHigh() + stockPrice.getLow() / 2;

            researchPrice = Math.min(researchPrice, stockPrice.getClose());

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

        boolean isLowestAndLowMovingAverageDiffValid =
                signalEvaluatorHelperService.isHighAndHighestMovingAverageDiffValid(
                        timeframe, stockPrice, stockTechnicals, MAInteractionType.BREAKOUT, true);

        if (!isLowestAndLowMovingAverageDiffValid) {
            return Optional.empty();
        }

        if (!isHighestAndHighMovingAverageDiffValid) {
            return Optional.empty();
        }

        // System.out.println("Log3 " + stock.getNseSymbol());

        if (stockTechnicals.getVolume() < VolumeIndicatorService.MIN_VOLUME * 2) {
            return Optional.empty();
        }
        // System.out.println("Log4 " + stock.getNseSymbol());

        if (stockTechnicals.getVolumeAvg20() < VolumeIndicatorService.MIN_VOLUME_AVG * 2) {
            return Optional.empty();
        }

        // System.out.println("Log5 " + stock.getNseSymbol());
        if (isLowestAndHighestMovingAverageDiffValid && (isStrongRange || isStrongBody)) {
            // System.out.println("Log6 " + stock.getNseSymbol());
            boolean isBullishConfirmed =
                    candleStickConfirmationService.isBullishConfirmed(
                            timeframe, stockPrice, stockTechnicals, true);

            boolean isOverSold =
                    stockTechnicals.getRsi() < 25.0 || stockTechnicals.getPrevRsi() < 25.0;

            boolean bullishConfirmed =
                    candleStickConfirmationService.isBullishConfirmed(
                            timeframe, stockPrice, stockTechnicals, true);

            if (CandleStickUtils.isStrongLowerWick(stockPrice)
                    || CandleStickUtils.isLowerWickDominant(stockPrice)
                    || CandleStickUtils.isLowerWickLongerThanUpperWick(stockPrice)
                    || isBullishConfirmed
                    || isOverSold
                    || bullishConfirmed) {
                // System.out.println("Log7 " + stock.getNseSymbol());
                if (volumeIndicatorService.isVolumeSurge(stockTechnicals)) {
                    if (stockTechnicals.getVolume() > 1.5 * stockTechnicals.getVolumeAvg20()) {
                        if ((stockTechnicals.getVolume() > 1.5 * stockTechnicals.getPrevVolume())
                                || (stockTechnicals.getVolume() > stockTechnicals.getPrevVolume()
                                        && stockTechnicals.getPrevVolume()
                                                > 1.5 * stockTechnicals.getPrevVolumeAvg20())) {
                            System.out.println("Log8 " + stock.getNseSymbol());
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
