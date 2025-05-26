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
import com.example.service.utils.CandleStickUtils;
import com.example.util.FormulaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MovingAverageActionServiceimpl implements MovingAverageActionService {

    private static double MA_ACTION_RISK_REWARD = 2.0;

    @Autowired private CandleStickService candleStickService;
    @Autowired private BreakoutLedgerService breakoutLedgerService;

    @Autowired private SupportResistanceUtilService supportResistanceService;

    @Autowired private StockPriceService<StockPrice> stockPriceService;

    @Autowired private StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    @Autowired private FormulaService formulaService;

    @Autowired private DynamicTrendService trendService;

    @Autowired
    private DynamicMovingAverageSupportResolverService dynamicMovingAverageSupportResolverService;

    @Override
    public TradeSetup breakDown(Stock stock, Timeframe timeframe) {

        StockPrice stockPrice = stockPriceService.get(stock, timeframe);
        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);

        Trend trend = trendService.detect(stock, timeframe);

        boolean isBreakDown = Boolean.FALSE;

        if (trend.getMomentum() == Trend.Phase.TOP) {
            if (this.isBreakDown(timeframe, stockPrice, stockTechnicals)) {
                isBreakDown = Boolean.TRUE;
            }
        }

        if (isBreakDown) {
            breakoutLedgerService.addNegative(
                    stock, timeframe, BreakoutLedger.BreakoutCategory.BREAKDOWN_EMA20);

            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchTechnical.Strategy.PRICE)
                    .subStrategy(ResearchTechnical.SubStrategy.SRMA)
                    .build();
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

    private boolean isBreakDown(
            Timeframe timeframe, StockPrice stockPrice, StockTechnicals stockTechnicals) {

        boolean isCurrentRed = CandleStickUtils.isRed(stockPrice);
        boolean isPrevRed = CandleStickUtils.isPrevSessionRed(stockPrice);
        MovingAverageSupportResistanceService movingAverageSupportResistanceService =
                dynamicMovingAverageSupportResolverService.resolve(
                        MovingAverageLength.SHORTEST, timeframe, stockTechnicals);

        if (isCurrentRed && isPrevRed) {
            return movingAverageSupportResistanceService.isBreakdown(
                    timeframe, stockPrice, stockTechnicals);
        } else if (!isCurrentRed && !isPrevRed) {
            return movingAverageSupportResistanceService.isNearResistance(
                    timeframe, stockPrice, stockTechnicals);
        }

        return Boolean.FALSE;
    }
}
