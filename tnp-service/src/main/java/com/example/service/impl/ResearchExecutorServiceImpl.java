package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.EvaluationLog;
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
import com.example.service.utils.CandleStickUtils;
import com.example.service.utils.MovingAverageUtil;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class ResearchExecutorServiceImpl implements ResearchExecutorService {

    private static final double MAX_RISK = 10.0;

    @Autowired private MiscUtil miscUtil;
    @Autowired private ResearchLedgerFundamentalService researchLedgerFundamentalService;
    @Autowired private StockTechnicalsService<StockTechnicals> stockTechnicalsService;

    @Autowired private ResistanceValidationService resistanceValidationService;
    @Autowired private FundamentalResearchService fundamentalResearchService;

    @Autowired private MovingAverageActionService movingAverageActionService;
    @Autowired private StockPriceService<StockPrice> stockPriceService;
    @Autowired private ValuationLedgerService valuationLedgerService;
    @Autowired private EvaluationLogService evaluationLogService;
    @Autowired private CandleStickService candleStickService;
    @Autowired private ResearchTechnicalService<ResearchTechnical> researchTechnicalService;
    @Autowired private CalendarService calendarService;
    @Autowired private FormulaService formulaService;
    @Autowired private VolumeIndicatorService volumeIndicatorService;

    @Autowired
    @Qualifier("candleStickPriceActionSignalEvaluator")
    private TradeSignalEvaluator candleStickPriceActionSignalEvaluator;

    @Autowired
    @Qualifier("basicPriceActionSignalEvaluator")
    private TradeSignalEvaluator basicPriceActionSignalEvaluator;

    @Autowired
    @Qualifier("dynamicPriceActionSignalEvaluator")
    private TradeSignalEvaluator dynamicPriceActionSignalEvaluator;

    @Autowired
    @Qualifier("hybridPriceActionSignalEvaluator")
    private TradeSignalEvaluator hybridPriceActionSignalEvaluator;

    @Autowired
    @Qualifier("simplePriceActionSignalEvaluator")
    private TradeSignalEvaluator simplePriceActionSignalEvaluator;

    @Autowired
    @Qualifier("pivotPriceActionSignalEvaluator")
    private TradeSignalEvaluator pivotPriceActionSignalEvaluator;

    @Autowired
    @Qualifier("rangePriceActionSignalEvaluator")
    private RangePriceActionSignalEvaluator rangePriceActionSignalEvaluator;

    @Autowired
    @Qualifier("investmentPriceActionSignalEvaluator")
    private InvestmentPriceActionSignalEvaluator investmentPriceActionSignalEvaluator;

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
    @Transactional
    public void executeTechnical(Timeframe timeframe, Stock stock, LocalDate sessionDate) {
        log.info("{} Executing technical research", stock.getNseSymbol());

        StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, timeframe);
        StockPrice stockPrice = stockPriceService.get(stock, timeframe);

        // Execute buy in its own transaction
        executeBuyOperation(timeframe, stock, stockPrice, stockTechnicals, sessionDate);

        // Execute sell in its own transaction
        executeSellOperation(timeframe, stock, stockPrice, stockTechnicals, sessionDate);

        log.info("{} Executed technical research", stock.getNseSymbol());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void executeBuyOperation(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            LocalDate sessionDate) {
        this.technicalBuy(timeframe, stock, stockPrice, stockTechnicals, sessionDate);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void executeSellOperation(
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            LocalDate sessionDate) {
        ResearchTechnical researchTechnical =
                researchTechnicalService.get(stock, timeframe, Trade.Type.BUY);
        if (researchTechnical != null) {
            this.technicalSell(
                    timeframe, stock, stockPrice, stockTechnicals, researchTechnical, sessionDate);
        }
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

        if (fundamentalResearchService.isPriceInRange(stock)
                && fundamentalResearchService.isMcapInRange(stock)
                && volumeIndicatorService.isTradingValueSufficient(
                        timeframe, stockPrice, stockTechnicals)) {
            if (stock.getSeries().equalsIgnoreCase("EQ")) {
                log.info("{} Found EQ stock ", stock.getNseSymbol());

                TradeSetup tradeSetup =
                        basicPriceActionSignalEvaluator.evaluateEntry(
                                timeframe, stock, stockPrice, stockTechnicals);

                if (!tradeSetup.isActive()) {
                    tradeSetup =
                            dynamicPriceActionSignalEvaluator.evaluateEntry(
                                    timeframe, stock, stockPrice, stockTechnicals);
                }

                if (!tradeSetup.isActive()) {
                    tradeSetup =
                            hybridPriceActionSignalEvaluator.evaluateEntry(
                                    timeframe, stock, stockPrice, stockTechnicals);
                }

                if (!tradeSetup.isActive()) {
                    tradeSetup =
                            investmentPriceActionSignalEvaluator.evaluateEntry(
                                    timeframe, stock, stockPrice, stockTechnicals);
                }

                if (!tradeSetup.isActive()) {
                    tradeSetup =
                            candleStickPriceActionSignalEvaluator.evaluateEntry(
                                    timeframe, stock, stockPrice, stockTechnicals);
                }

                /*
                if (!tradeSetup.isActive()) {
                    tradeSetup =
                            simplePriceActionSignalEvaluator.evaluateEntry(
                                    timeframe, stock, stockPrice, stockTechnicals);
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
        if (this.isTargetAchieved(
                researchTechnical, timeframe, stock, stockPrice, stockTechnicals)) {
            tradeSetup.setActive(true);
            tradeSetup.setStrategy(ResearchTechnical.Strategy.TARGET);
            tradeSetup.setSubStrategy(ResearchTechnical.SubStrategy.TARGET_ACHIEVED);
            isUpdation = Boolean.TRUE;

        } else if (candleStickService.isRed(stockPrice)
                && this.isStopLossTriggered(
                        researchTechnical, timeframe, stock, stockPrice, stockTechnicals)) {

            tradeSetup.setActive(true);
            tradeSetup.setStrategy(ResearchTechnical.Strategy.STOP_LOSS);
            tradeSetup.setSubStrategy(ResearchTechnical.SubStrategy.STOP_LOSS_TRIGGERED);
            isUpdation = Boolean.TRUE;

        } else if (researchTechnical.getEntryPrice() < stockPrice.getClose()) {

            tradeSetup =
                    basicPriceActionSignalEvaluator.evaluateExit(
                            timeframe, stock, stockPrice, stockTechnicals);
            /*
            if (!tradeSetup.isActive()) {
                tradeSetup =
                        simplePriceActionSignalEvaluator.evaluateExit(
                                timeframe, stock, stockPrice, stockTechnicals);
            }*/

            if (!tradeSetup.isActive()) {
                tradeSetup =
                        dynamicPriceActionSignalEvaluator.evaluateExit(
                                timeframe, stock, stockPrice, stockTechnicals);
            }

            /*
            if (!tradeSetup.isActive()) {
                tradeSetup =
                        hybridPriceActionSignalEvaluator.evaluateExit(
                                timeframe, stock, stockPrice, stockTechnicals);
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
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        if (CandleStickUtils.isLowerWickDominant(stockPrice)
                || CandleStickUtils.isStrongLowerWick(stockPrice)) {
            if (stockTechnicals.getVolumeAvg20() > stockTechnicals.getPrevVolumeAvg20()) {
                return false;
            }
        }

        if (MovingAverageUtil.getMovingAverage200(timeframe, stockTechnicals)
                > MovingAverageUtil.getPrevMovingAverage200(timeframe, stockTechnicals)) {
            if (MovingAverageUtil.getMovingAverage100(timeframe, stockTechnicals)
                    > MovingAverageUtil.getPrevMovingAverage100(timeframe, stockTechnicals)) {
                if (MovingAverageUtil.getMovingAverage100(timeframe, stockTechnicals)
                        > MovingAverageUtil.getMovingAverage200(timeframe, stockTechnicals)) {
                    if (MovingAverageUtil.getMovingAverage50(timeframe, stockTechnicals)
                            > MovingAverageUtil.getMovingAverage100(timeframe, stockTechnicals)) {
                        if (CandleStickUtils.isLowerWickLongerThanUpperWick(stockPrice)) {
                            return false;
                        }
                    }
                }
            }
        }

        if (researchTechnical.getStopLoss() > stockPrice.getClose()
                && stockPrice.getClose()
                        < Math.min(stockPrice.getPrevOpen(), stockPrice.getPrevClose())) {
            evaluationLogService.add(
                    stockPrice,
                    EvaluationLog.Type.NEGATIVE,
                    EvaluationLog.BreakoutCategory.STOPLOSS_TRIGGERED.name());
            log.info(
                    "{} Stop loss triggered, stopLoss {}",
                    stock.getNseSymbol(),
                    researchTechnical.getStopLoss());
            if (resistanceValidationService.isOutsideSupportZone(stockPrice)) {
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }

    private boolean isTargetAchieved(
            ResearchTechnical researchTechnical,
            Timeframe timeframe,
            Stock stock,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals) {

        boolean isLowerHighLowerLow =
                CandleStickUtils.isLowerHigh(stockPrice) && CandleStickUtils.isLowerLow(stockPrice);

        boolean isHigherHighHigherLow =
                CandleStickUtils.isHigherHigh(stockPrice)
                        && CandleStickUtils.isHigherLow(stockPrice);

        MovingAverageResult highestMovingAverageResult =
                MovingAverageUtil.getMovingAverage(
                        MovingAverageLength.HIGHEST, timeframe, stockTechnicals, false);

        boolean isCloseBelowHighest = highestMovingAverageResult.getValue() > stockPrice.getClose();

        boolean isUpperWickStrong =
                CandleStickUtils.isStrongUpperWick(stockPrice)
                        || CandleStickUtils.isUpperWickDominant(stockPrice)
                        || CandleStickUtils.isUpperWickLongerThanLowerWick(stockPrice);

        if ((CandleStickUtils.isRed(stockPrice) && isLowerHighLowerLow && isCloseBelowHighest)
                || (isUpperWickStrong && isHigherHighHigherLow)) {

            if (researchTechnical.getTarget() <= stockPrice.getHigh()) {
                evaluationLogService.add(
                        stockPrice,
                        EvaluationLog.Type.POSITIVE,
                        EvaluationLog.BreakoutCategory.TARGET_ACHIEVED.name());
                log.info(
                        "{} Target achieved, target {}",
                        stock.getNseSymbol(),
                        researchTechnical.getTarget());
                return Boolean.TRUE;
            }
        }

        return Boolean.FALSE;
    }
}
