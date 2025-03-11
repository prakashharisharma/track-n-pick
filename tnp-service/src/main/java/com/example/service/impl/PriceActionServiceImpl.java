package com.example.service.impl;

import com.example.dto.TradeSetup;
import com.example.model.ledger.BreakoutLedger;
import com.example.model.ledger.ResearchLedgerTechnical;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.service.*;
import com.example.util.FormulaService;
import com.example.util.io.model.type.Momentum;
import com.example.util.io.model.type.Trend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.util.io.model.type.Trend.Strength.INVALID;

@Slf4j
@Service
public class PriceActionServiceImpl implements PriceActionService {

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
        //Momentum momentum = trendService.scanBullish(stock);
        ResearchLedgerTechnical.SubStrategy subStrategy = ResearchLedgerTechnical.SubStrategy.SRTF;
        double riskRewardRatio = 0.0;
        if (trend.getStrength() != INVALID) {
            if (candleStickHelperService.isBullishConfirmed(stock, Boolean.TRUE)){
                //log.info("{} bullish candlestick active", stock.getNseSymbol());
                 if (relevanceService.isBullishIndicator(stock, trend)) {
                    subStrategy = ResearchLedgerTechnical.SubStrategy.RMAO;
                    riskRewardRatio = RiskReward.PRICE_RMAO;
                    isCandleActive = Boolean.TRUE;
                }else if (relevanceService.isBullishTimeFrame(stock, trend, 1.5)) {
                     subStrategy = ResearchLedgerTechnical.SubStrategy.SRTF;
                     riskRewardRatio = RiskReward.PRICE_SRTF;
                     isCandleActive = Boolean.TRUE;
                }else if (relevanceService.isBullishMovingAverage(stock, trend, 1.5)) {
                     subStrategy = ResearchLedgerTechnical.SubStrategy.SRMA;
                     riskRewardRatio = RiskReward.PRICE_SRMA;
                     isCandleActive = Boolean.TRUE;
                 }
            }
        }


        if (isCandleActive) {
            log.info("{} bullish candlestick confirmed using {}:{}", stock.getNseSymbol(), ResearchLedgerTechnical.Strategy.PRICE, subStrategy);
            StockPrice stockPrice = stock.getStockPrice();

            double entryPrice = stockPrice.getHigh();
            double stopLossPrice = stockPrice.getLow();
            double targetPrice = formulaService.calculateTarget(entryPrice, stopLossPrice, riskRewardRatio);

            double risk = Math.abs(formulaService.calculateChangePercentage(entryPrice, stopLossPrice));
            double correction = stockPriceService.correction(stock);
            log.info("{} bullish price action active for trend:{}, momentum:{}, entryPrice:{}, targetPrice:{}, stopLossPrice:{}, risk {}, correction {}"
                    , stock.getNseSymbol(), trend.getStrength(), trend.getMomentum(), entryPrice, targetPrice, stopLossPrice, risk, correction);

            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchLedgerTechnical.Strategy.PRICE)
                    .subStrategy(subStrategy)
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
        //Momentum momentum = trendService.scanBearish(stock);
        ResearchLedgerTechnical.SubStrategy subStrategy = ResearchLedgerTechnical.SubStrategy.SRTF;
        if (trend.getStrength() != INVALID) {
            if (candleStickHelperService.isBearishConfirmed(stock, Boolean.TRUE)){
                //log.info("{} bearish candlestick active {}", stock.getNseSymbol());
                if (relevanceService.isBearishIndicator(stock, trend)) {
                    subStrategy = ResearchLedgerTechnical.SubStrategy.RMAO;
                    isCandleActive = Boolean.TRUE;
                }else if (relevanceService.isBearishTimeFrame(stock, trend, 1.5)) {
                    subStrategy = ResearchLedgerTechnical.SubStrategy.SRTF;
                    isCandleActive = Boolean.TRUE;
                }else if (relevanceService.isBearishMovingAverage(stock, trend, 1.5)) {
                    subStrategy = ResearchLedgerTechnical.SubStrategy.SRMA;
                    isCandleActive = Boolean.TRUE;
                }
            }
        }

        if (isCandleActive) {
            log.info("{} bearish candlestick confirmed using {}:{}", stock.getNseSymbol(), ResearchLedgerTechnical.Strategy.PRICE, subStrategy);
            StockPrice stockPrice = stock.getStockPrice();

            double entryPrice = stockPrice.getLow();
            double stopLossPrice = stockPrice.getHigh();
            double targetPrice = formulaService.calculateTarget(entryPrice, stopLossPrice, RiskReward.PRICE_SRTF);
            double risk = 0.0;
            double correction = 0.0;
            log.info("{} bearish price action active for trend:{}, momentum:{}, entryPrice:{}, targetPrice:{}, stopLossPrice:{}, risk {}, correction {}"
                    , stock.getNseSymbol(), trend.getStrength(), trend.getMomentum(), entryPrice, targetPrice, stopLossPrice, risk, correction);
            breakoutLedgerService.addNegative(stock, BreakoutLedger.BreakoutCategory.BREAKDOWN_CANDLESTICK);
            return TradeSetup.builder()
                    .active(Boolean.TRUE)
                    .strategy(ResearchLedgerTechnical.Strategy.PRICE)
                    .subStrategy(subStrategy)
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
