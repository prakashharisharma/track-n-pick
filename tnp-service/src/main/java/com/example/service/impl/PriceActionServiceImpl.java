package com.example.service.impl;

import com.example.dto.TradeSetup;
import com.example.model.ledger.BreakoutLedger;
import com.example.model.ledger.ResearchLedgerTechnical;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.service.*;
import com.example.service.util.StockPriceUtil;
import com.example.util.FormulaService;
import com.example.util.io.model.type.Momentum;
import com.example.util.io.model.type.Trend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PriceActionServiceImpl implements PriceActionService {

    private static final double PRICE_ACTION_RISK_REWARD = 3.0;

    @Autowired
    private SupportResistanceUtilService supportResistanceService;
    @Autowired
    private CandleStickHelperService candleStickHelperService;
    @Autowired
    private VolumeService volumeService;

    @Autowired
    private CandleStickService candleStickService;

    @Autowired
    private BreakoutLedgerService breakoutLedgerService;

    @Autowired
    private StockPriceService stockPriceService;
    @Autowired
    private RelevanceService relevanceService;
    @Autowired
    private FormulaService formulaService;
    @Autowired
    private TrendService trendService;

    @Autowired
    private RsiIndicatorService rsiIndicatorService;

    @Override
    public TradeSetup breakOut(Stock stock) {
        if (candleStickService.isDead(stock)) {
            return TradeSetup.builder().active(Boolean.FALSE).build();
        }

        boolean isCandleActive = Boolean.FALSE;
        Trend trend = trendService.isDownTrend(stock);
        Momentum momentum = trendService.scanBullish(stock);
        if (trend != Trend.INVALID) {
            if (candleStickHelperService.isBullishConfirmed(stock, Boolean.TRUE)){
                log.info("Candlestick active {}", stock.getNseSymbol());
                if (relevanceService.isBullish(stock, trend,momentum, 1.5)) {
                    log.info("Candlestick confirmed {}", stock.getNseSymbol());
                    isCandleActive = Boolean.TRUE;
                }
            }
        }


        if (isCandleActive) {
            StockPrice stockPrice = stock.getStockPrice();

            double entryPrice = stockPrice.getHigh();
            double stopLossPrice = stockPrice.getLow();
            double targetPrice = formulaService.calculateTarget(entryPrice, stopLossPrice, PRICE_ACTION_RISK_REWARD);

            double risk = formulaService.calculateChangePercentage(stopLossPrice, entryPrice);
            double correction = stockPriceService.correction(stock);
            log.info("Price Action active for {}, entryPrice:{}, targetPrice:{}, stopLossPrice:{}, risk {}, correction {}"
                    , stock.getNseSymbol(), entryPrice, targetPrice, stopLossPrice, risk, correction);

            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchLedgerTechnical.Strategy.PRICE_ACTION)
                    .subStrategy(ResearchLedgerTechnical.SubStrategy.CANDLESTICK)
                    .entryPrice(entryPrice)
                    .stopLossPrice(stopLossPrice)
                    .targetPrice(targetPrice)
                    .risk(risk)
                    .correction(correction)
                    .build();
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }


    @Override
    public TradeSetup breakDown(Stock stock) {
        if (candleStickService.isDead(stock)) {
            return TradeSetup.builder().active(Boolean.FALSE).build();
        }

        boolean isCandleActive = Boolean.FALSE;

        Trend trend = trendService.isUpTrend(stock);
        Momentum momentum = trendService.scanBearish(stock);
        if (trend!=Trend.INVALID) {
            if (candleStickHelperService.isBearishConfirmed(stock, Boolean.TRUE)){
                log.info("Candlestick active {}", stock.getNseSymbol());
                if (relevanceService.isBearish(stock, trend,momentum, 1.5)) {
                    isCandleActive = Boolean.TRUE;
                    log.info("Candlestick confirmed {}", stock.getNseSymbol());
                }
            }
        }

        if (isCandleActive) {
            StockPrice stockPrice = stock.getStockPrice();

            double entryPrice = stockPrice.getLow();
            double stopLossPrice = stockPrice.getHigh();
            double targetPrice = formulaService.calculateTarget(entryPrice, stopLossPrice, PRICE_ACTION_RISK_REWARD);
            double risk = 0.0;
            double correction = 0.0;
            log.info("Price Action active for {}, entryPrice:{}, targetPrice:{}, stopLossPrice:{}"
                    , stock.getNseSymbol(), entryPrice, targetPrice, stopLossPrice);
            breakoutLedgerService.addNegative(stock, BreakoutLedger.BreakoutCategory.BREAKDOWN_CANDLESTICK);
            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchLedgerTechnical.Strategy.PRICE_ACTION)
                    .subStrategy(ResearchLedgerTechnical.SubStrategy.CANDLESTICK)
                    .entryPrice(entryPrice)
                    .stopLossPrice(stopLossPrice)
                    .targetPrice(targetPrice)
                    .risk(risk)
                    .correction(correction)
                    .build();
        }

        return TradeSetup.builder().active(Boolean.FALSE).build();
    }


}
