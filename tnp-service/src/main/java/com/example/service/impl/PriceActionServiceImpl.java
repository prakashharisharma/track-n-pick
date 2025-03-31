package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.dto.TradeSetup;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.service.StockPriceService;
import com.example.service.StockTechnicalsService;
import com.example.data.transactional.entities.BreakoutLedger;
import com.example.service.*;
import com.example.util.FormulaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PriceActionServiceImpl implements PriceActionService {

    @Autowired
    private SupportResistanceUtilService supportResistanceService;
    @Autowired
    private CandleStickConfirmationService candleStickHelperService;

    @Autowired
    private CandleStickService candleStickService;

    @Autowired
    private BreakoutLedgerService breakoutLedgerService;

    @Autowired
    private StockPriceServiceOld stockPriceServiceOld;
    @Autowired
    private RelevanceService relevanceService;
    @Autowired
    private FormulaService formulaService;
    @Autowired
    private TrendService trendService;

    @Autowired
    private RsiIndicatorService rsiIndicatorService;


    @Autowired
    private StockPriceService<StockPrice> stockPriceService;

    @Autowired
    private StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    @Override
    public TradeSetup breakOut(Stock stock,
                               Timeframe timeframe) {


        StockPrice stockPrice = stockPriceService.get(stock, timeframe);
        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);
        if (candleStickService.isDead(stockPrice)) {
            return TradeSetup.builder().active(Boolean.FALSE).build();
        }

        boolean isCandleActive = Boolean.FALSE;
        Trend trend = trendService.detect(stock, timeframe);
        ResearchTechnical.SubStrategy subStrategy = ResearchTechnical.SubStrategy.SRTF;
        if (trend.getDirection() != Trend.Direction.INVALID) {
            if (candleStickHelperService.isBullishConfirmed(timeframe,stockPrice, stockTechnicals)){
                 if (relevanceService.isBullishIndicator(trend, timeframe, stockPrice, stockTechnicals)) {
                    subStrategy = ResearchTechnical.SubStrategy.RMAO;
                    isCandleActive = Boolean.TRUE;
                }else if (relevanceService.isBullishTimeFrame(trend, timeframe, stockPrice, stockTechnicals, 1.5)) {
                     subStrategy = ResearchTechnical.SubStrategy.SRTF;
                     isCandleActive = Boolean.TRUE;
                }else if (relevanceService.isBullishMovingAverage(trend, timeframe, stockPrice, stockTechnicals, 1.5)) {
                     subStrategy = ResearchTechnical.SubStrategy.SRMA;
                     isCandleActive = Boolean.TRUE;
                 }
            }
        }


        if (isCandleActive) {
            log.info("{} bullish candlestick confirmed using {}:{}", stock.getNseSymbol(), ResearchTechnical.Strategy.PRICE, subStrategy);

            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchTechnical.Strategy.PRICE)
                    .subStrategy(subStrategy)
                    .build();
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
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
        ResearchTechnical.SubStrategy subStrategy = ResearchTechnical.SubStrategy.SRTF;
        if (trend.getDirection() != Trend.Direction.INVALID) {
            if (candleStickHelperService.isBearishConfirmed(timeframe, stockPrice, stockTechnicals)){
                if (relevanceService.isBearishIndicator(trend, timeframe, stockPrice, stockTechnicals)) {
                    subStrategy = ResearchTechnical.SubStrategy.RMAO;
                    isCandleActive = Boolean.TRUE;
                }else if (relevanceService.isBearishTimeFrame(trend, timeframe, stockPrice, stockTechnicals, 1.5)) {
                    subStrategy = ResearchTechnical.SubStrategy.SRTF;
                    isCandleActive = Boolean.TRUE;
                }else if (relevanceService.isBearishMovingAverage(trend, timeframe, stockPrice, stockTechnicals, 1.5)) {
                    subStrategy = ResearchTechnical.SubStrategy.SRMA;
                    isCandleActive = Boolean.TRUE;
                }
            }
        }

        if (isCandleActive) {
            log.info("{} bearish candlestick confirmed using {}:{}", stock.getNseSymbol(), ResearchTechnical.Strategy.PRICE, subStrategy);

            breakoutLedgerService.addNegative(stock, timeframe, BreakoutLedger.BreakoutCategory.BREAKDOWN_CANDLESTICK);
            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchTechnical.Strategy.PRICE)
                    .subStrategy(subStrategy)
                    .build();
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }

}
