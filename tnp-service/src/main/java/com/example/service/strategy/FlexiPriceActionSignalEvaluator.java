package com.example.service.strategy;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.dto.common.TradeSetup;
import com.example.service.*;
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
import com.example.service.utils.SubStrategyHelper;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service("flexiPriceActionSignalEvaluator")
public class FlexiPriceActionSignalEvaluator implements TradeSignalEvaluator {
    private final DynamicMovingAverageSupportResolverService
            dynamicMovingAverageSupportResolverService;
    private final StockPriceService<StockPrice> stockPriceService;
    private final StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    private final VolumeIndicatorService volumeIndicatorService;

    @Override
    public TradeSetup evaluateEntry(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        Optional<MAEvaluationResult> evaluationResultOptional =
                dynamicMovingAverageSupportResolverService.evaluateSingleInteractionSmart(
                        timeframe, stockPrice, stockTechnicals, false);
        Optional<ResearchTechnical.SubStrategy> subStrategyRef = Optional.empty();

        if (evaluationResultOptional.isPresent()
                && evaluationResultOptional.get().isBreakout()
                && evaluationResultOptional.get().getLength() == MovingAverageLength.HIGHEST) {
            subStrategyRef =
                    confirmBreakout(
                            timeframe,
                            stock,
                            stockPrice,
                            stockTechnicals,
                            evaluationResultOptional.get());
        }

        if (subStrategyRef.isPresent()) {
            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchTechnical.Strategy.FLEXI)
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
        StockPrice stockPriceHT = stockPriceService.get(stock, timeframe.getHigher());
        StockTechnicals stockTechnicalsHT =
                stockTechnicalsService.get(stock, timeframe.getHigher());

        if (stockPriceHT != null && stockTechnicalsHT != null) {
            if (MovingAverageUtil.isAllMaAlignedBullish(
                    stockPriceHT.getTimeframe(), stockTechnicalsHT)) {
                if (MovingAverageUtil.isAllMaAlignedBearish(
                        stockPrice.getTimeframe(), stockTechnicals)) {

                    if (CandleStickUtils.isGreen(stockPrice)) {
                        if (volumeIndicatorService.isBullish(
                                stockPrice, stockTechnicals, stockPrice.getTimeframe())) {

                            return SubStrategyHelper.resolveByName("ma5" + "_breakout");
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
        return TradeSetup.builder().active(Boolean.FALSE).build();
    }
}
