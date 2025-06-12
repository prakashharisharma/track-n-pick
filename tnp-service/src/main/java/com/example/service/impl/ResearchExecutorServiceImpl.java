package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.BreakoutLedger;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.data.transactional.entities.Trade;
import com.example.data.transactional.entities.ValuationLedger;
import com.example.dto.common.TradeSetup;
import com.example.service.*;
import com.example.service.ResearchTechnicalService;
import com.example.service.StockPriceService;
import com.example.service.StockTechnicalsService;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import java.time.LocalDate;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
public class ResearchExecutorServiceImpl implements ResearchExecutorService {

    private static final double MAX_RISK = 10.0;

    @Autowired private MiscUtil miscUtil;

    @Autowired private ResearchLedgerFundamentalService researchLedgerFundamentalService;
    @Autowired private StockTechnicalsService<StockTechnicals> stockTechnicalsService;
    @Autowired private FundamentalResearchService fundamentalResearchService;

    @Autowired private MovingAverageActionService movingAverageActionService;
    @Autowired private StockPriceService<StockPrice> stockPriceService;
    @Autowired private ValuationLedgerService valuationLedgerService;
    @Autowired private BreakoutLedgerService breakoutLedgerService;
    @Autowired private SwingActionService swingActionService;
    @Autowired private PriceActionService priceActionService;

    @Autowired private DynamicPriceActionService dynamicPriceActionService;
    @Autowired private CandleStickService candleStickService;

    @Autowired private ResearchTechnicalService<ResearchTechnical> researchTechnicalService;

    @Autowired private CalendarService calendarService;

    @Autowired private FormulaService formulaService;

    @Autowired private VolumeIndicatorService volumeIndicatorService;

    @Autowired
    @Qualifier("basicPriceActionSignalEvaluator")
    private TradeSignalEvaluator basicPriceActionSignalEvaluator;

    @Autowired
    @Qualifier("dynamicPriceActionSignalEvaluator")
    private TradeSignalEvaluator dynamicPriceActionSignalEvaluator;

    @Autowired
    @Qualifier("simplePriceActionSignalEvaluator")
    private TradeSignalEvaluator simplePriceActionSignalEvaluator;

    @Override
    public void executeFundamental(Stock stock) {

        log.info("{} Executing fundamental research", stock.getNseSymbol());

        if (researchLedgerFundamentalService.isResearchActive(stock)) {
            this.fundamentalSell(stock);
        } else {
            this.fundamentalBuy(stock);
        }
        log.info("{} Executed fundamental research", stock.getNseSymbol());
    }

    @Override
    public void executeTechnical(Timeframe timeframe, Stock stock, LocalDate sessionDate) {
        log.info("{} Executing technical research", stock.getNseSymbol());

        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);

        StockPrice stockPrice = stockPriceService.get(stock, timeframe);

        ResearchTechnical researchTechnical =
                researchTechnicalService.get(stock, timeframe, Trade.Type.BUY);

        if (researchTechnical != null) {
            this.technicalSell(
                    timeframe, stock, stockPrice, stockTechnicals, researchTechnical, sessionDate);
        } else {
            log.info("{} No existing research, executing buy", stock.getNseSymbol());
            this.technicalBuy(timeframe, stock, stockPrice, stockTechnicals, sessionDate);
            log.info("{} executed buy", stock.getNseSymbol());
        }

        log.info("{} Executed technical research", stock.getNseSymbol());
    }

    private void fundamentalBuy(Stock stock) {
        log.info("{} Executing fundamental buy", stock.getNseSymbol());
        if (fundamentalResearchService.isUndervalued(stock)
                && stock.getSeries().equalsIgnoreCase("EQ")) {

            ValuationLedger entryValuation = valuationLedgerService.addUndervalued(stock);

            researchLedgerFundamentalService.addResearch(stock, entryValuation);
        }
        log.info("{} Executed fundamental buy", stock.getNseSymbol());
    }

    private void fundamentalSell(Stock stock) {

        if (fundamentalResearchService.isOvervalued(stock)
                || !stock.getSeries().equalsIgnoreCase("EQ")) {
            log.info("{} Executing fundamental sell", stock.getNseSymbol());
            ValuationLedger exitValuation = valuationLedgerService.addOvervalued(stock);

            researchLedgerFundamentalService.updateResearch(stock, exitValuation);
            log.info("{} Executed fundamental sell", stock.getNseSymbol());
        }
    }

    private void technicalBuy(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            LocalDate sessionDate) {

        log.info("{} Executing technical buy", stock.getNseSymbol());

        if (fundamentalResearchService.isMcapInRange(stock)
                && volumeIndicatorService.isTradingValueSufficient(
                        timeframe, stockPrice, stockTechnicals)) {
            if (stock.getSeries().equalsIgnoreCase("EQ")) {
                log.info("{} Found EQ stock ", stock.getNseSymbol());

                TradeSetup tradeSetup =
                        dynamicPriceActionSignalEvaluator.evaluateEntry(
                                timeframe, stock, stockPrice, stockTechnicals);

                if (!tradeSetup.isActive()) {
                    tradeSetup =
                            simplePriceActionSignalEvaluator.evaluateEntry(
                                    timeframe, stock, stockPrice, stockTechnicals);
                }

                if (!tradeSetup.isActive()) {
                    tradeSetup =
                            basicPriceActionSignalEvaluator.evaluateEntry(
                                    timeframe, stock, stockPrice, stockTechnicals);
                }

                /*
                TradeSetup tradeSetup = dynamicPriceActionService.breakOut(stock, timeframe);

                if (!tradeSetup.isActive()) {
                    tradeSetup = priceActionService.breakOut(stock, timeframe);
                }

                if (!tradeSetup.isActive()) {
                    tradeSetup = swingActionService.breakOut(stock, timeframe);
                }
                */

                if (tradeSetup.isActive()) {
                    log.info(
                            "{} Bullish Trade active timeframe: {}, strategy:{}, subStrategy:{} ",
                            stock.getNseSymbol(),
                            timeframe,
                            tradeSetup.getStrategy(),
                            tradeSetup.getSubStrategy());
                    researchTechnicalService.entry(
                            stock, timeframe, tradeSetup, stockPrice, stockTechnicals, sessionDate);
                }
            }
        }

        log.info("{} Executed technical buy", stock.getNseSymbol());
    }

    private void technicalSell(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            ResearchTechnical researchTechnical,
            LocalDate sessionDate) {

        log.info("{} Executing technical sell", stock.getNseSymbol());

        boolean isUpdation = Boolean.FALSE;
        TradeSetup tradeSetup = TradeSetup.builder().build();
        // if (candleStickService.isRed(stockPrice)) {
        if (candleStickService.isRed(stockPrice)
                && this.isTargetAchieved(researchTechnical, timeframe, stock, stockPrice)) {
            tradeSetup.setActive(true);
            tradeSetup.setStrategy(ResearchTechnical.Strategy.TARGET);
            tradeSetup.setSubStrategy(ResearchTechnical.SubStrategy.TARGET_ACHIEVED);
            isUpdation = Boolean.TRUE;

        } else if (candleStickService.isRed(stockPrice)
                && this.isStopLossTriggered(researchTechnical, timeframe, stock, stockPrice)) {

            tradeSetup.setActive(true);
            tradeSetup.setStrategy(ResearchTechnical.Strategy.STOP_LOSS);
            tradeSetup.setSubStrategy(ResearchTechnical.SubStrategy.STOP_LOSS_TRIGGERED);
            isUpdation = Boolean.TRUE;

        } else {

            tradeSetup =
                    dynamicPriceActionSignalEvaluator.evaluateExit(
                            timeframe, stock, stockPrice, stockTechnicals);

            if (!tradeSetup.isActive()) {
                tradeSetup =
                        simplePriceActionSignalEvaluator.evaluateExit(
                                timeframe, stock, stockPrice, stockTechnicals);
            }

            if (!tradeSetup.isActive()) {
                tradeSetup =
                        basicPriceActionSignalEvaluator.evaluateExit(
                                timeframe, stock, stockPrice, stockTechnicals);
            }

            /*
            tradeSetup = dynamicPriceActionService.breakDown(stock, timeframe);


            if (!tradeSetup.isActive()) {
                tradeSetup = priceActionService.breakDown(stock, timeframe);
            }

            if (!tradeSetup.isActive()) {
                tradeSetup = swingActionService.breakDown(stock, timeframe);
            }

            if (!tradeSetup.isActive()) {
                tradeSetup = movingAverageActionService.breakDown(stock, timeframe);
                double close = stockPrice.getClose();
                if (researchTechnical.getEntryPrice() >= close
                        && researchTechnical.getTarget() >= close) {
                    tradeSetup.setActive(false);
                }
            }
            */

            if (tradeSetup.isActive()) {
                isUpdation = Boolean.TRUE;
            }
        }

        if (isUpdation) {
            researchTechnicalService.exit(
                    stock, timeframe, tradeSetup, stockPrice, stockTechnicals, sessionDate);
        }

        log.info("{} Executed technical sell", stock.getNseSymbol());
    }

    private boolean isStopLossTriggered(
            ResearchTechnical researchTechnical,
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice) {

        if (researchTechnical.getStopLoss() > stockPrice.getClose()) {
            breakoutLedgerService.addNegative(
                    stock, timeframe, BreakoutLedger.BreakoutCategory.STOPLOSS_TRIGGERED);
            log.info(
                    "{} Stop loss triggered, stopLoss {}",
                    stock.getNseSymbol(),
                    researchTechnical.getStopLoss());
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    private boolean isTargetAchieved(
            ResearchTechnical researchTechnical,
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice) {

        if (researchTechnical.getTarget() <= stockPrice.getClose()) {
            breakoutLedgerService.addNegative(
                    stock, timeframe, BreakoutLedger.BreakoutCategory.TARGET_ACHIEVED);
            log.info(
                    "{} Target achieved, target {}",
                    stock.getNseSymbol(),
                    researchTechnical.getTarget());
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }
}
