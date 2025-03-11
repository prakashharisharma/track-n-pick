package com.example.service.impl;

import com.example.dto.TradeSetup;
import com.example.model.ledger.BreakoutLedger;
import com.example.model.ledger.ResearchLedgerTechnical;
import com.example.model.ledger.ValuationLedger;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.service.*;
import com.example.util.FormulaService;
import com.example.util.io.model.ResearchIO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;

@Slf4j
@Service
@Transactional
public class ResearchExecutorServiceImpl implements ResearchExecutorService {

    private static final double MAX_RISK = 10.0;
    @Autowired
    private ResearchLedgerFundamentalService researchLedgerFundamentalService;

    @Autowired
    private ResearchLedgerTechnicalService researchLedgerTechnicalService;
    @Autowired
    private FundamentalResearchService fundamentalResearchService;
    @Autowired
    private CandleStickExecutorService candleStickExecutorService;
    @Autowired
    private TechnicalsResearchService technicalsResearchService;

    @Autowired
    private MovingAverageActionService movingAverageActionService;
    @Autowired
    private ValuationLedgerService valuationLedgerService;
    @Autowired
    private BreakoutLedgerService breakoutLedgerService;
    @Autowired
    private VolumeActionService volumeActionService;
    @Autowired
    private SwingActionService swingActionService;
    @Autowired
    private PriceActionService priceActionService;
    @Autowired
    private CandleStickService candleStickService;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private FormulaService formulaService;

    @Autowired
    private VolumeService volumeService;
    @Override
    public void executeFundamental(Stock stock) {

        log.info("Executing fundamental research {}", stock.getNseSymbol());

        if(researchLedgerFundamentalService.isResearchActive(stock)){
            this.fundamentalSell(stock);
        }else {
            this.fundamentalBuy(stock);
        }
        log.info("Executed fundamental research {}", stock.getNseSymbol());
    }

    @Override
    public void executeTechnical(Stock stock) {

        log.info("Executing technical research {}", stock.getNseSymbol());

       ResearchLedgerTechnical researchLedgerTechnical = researchLedgerTechnicalService.getActiveResearch(stock);

       if( researchLedgerTechnical != null){
            this.technicalSell(stock, researchLedgerTechnical);
       }else {
           this.technicalBuy(stock);

       }
        log.info("Executed technical research {}", stock.getNseSymbol());
    }

    private void fundamentalBuy(Stock stock){
        log.info("Executing fundamental buy {}", stock.getNseSymbol());
        if(fundamentalResearchService.isUndervalued(stock) && stock.getSeries().equalsIgnoreCase("EQ")){

            ValuationLedger entryValuation = valuationLedgerService.addUndervalued(stock);

            researchLedgerFundamentalService.addResearch(stock, entryValuation);
        }
        log.info("Executed fundamental buy {}", stock.getNseSymbol());
    }

    private void fundamentalSell(Stock stock){

        if(fundamentalResearchService.isOvervalued(stock) || stock.getSeries().equalsIgnoreCase("BE")){
            log.info("Executing fundamental sell {}", stock.getNseSymbol());
            ValuationLedger exitValuation = valuationLedgerService.addOvervalued(stock);

            researchLedgerFundamentalService.updateResearch(stock, exitValuation);
            log.info("Executed fundamental sell {}", stock.getNseSymbol());
        }
    }

    private void technicalBuy(Stock stock){
        log.info("Executing technical buy {}", stock.getNseSymbol());
        if (fundamentalResearchService.isMcapInRange(stock)) {
            if (stock.getSeries().equalsIgnoreCase("EQ")) {

                TradeSetup tradeSetup = swingActionService.breakOut(stock);

                if(!tradeSetup.isActive()){
                    tradeSetup = priceActionService.breakOut(stock);
                }

                if(!tradeSetup.isActive()){
                    tradeSetup = volumeActionService.breakOut(stock);
                }


                if (tradeSetup.isActive()) {
                        researchLedgerTechnicalService.addResearch(stock, tradeSetup);
                }
            }
        }
        log.info("Executed technical buy {}", stock.getNseSymbol());
    }

    private void technicalSell(Stock stock, ResearchLedgerTechnical researchLedgerTechnical) {

        log.info("Executing technical sell {}", stock.getNseSymbol());

        StockPrice stockPrice = stock.getStockPrice();

        boolean isUpdation = Boolean.FALSE;


        /*if(volumeActionService.breakOut(stock).isActive() && candleStickService.isGreen(stock) ){
                researchLedgerTechnical.setVolume(stock.getTechnicals().getVolume());
                researchLedgerTechnical.setVolumeAvg5(stock.getTechnicals().getVolumeAvg5());
                researchLedgerTechnical.setVolumeAvg20(stock.getTechnicals().getVolumeAvg20());
                researchLedgerTechnical.setStopLoss(stockPrice.getLow());
                researchLedgerTechnical.setStrategy(ResearchLedgerTechnical.Strategy.VOLUME_ACTION);
                researchLedgerTechnical.setSubStrategy(ResearchLedgerTechnical.SubStrategy.HIGH_VOLUME);
                researchLedgerTechnical.setTarget(formulaService.calculateTarget(stockPrice.getHigh(), stockPrice.getLow(),VolumeActionService.VOLUME_ACTION_RISK_REWARD ));
                researchLedgerTechnical.setNextTradingDate(calendarService.nextTradingDate(stockPrice.getBhavDate()));
                isUpdation = Boolean.TRUE;

        }else */
         if (candleStickService.isRed(stock) && this.isTargetAchieved(researchLedgerTechnical, stockPrice)) {
            log.info("Verifying target {}", stock.getNseSymbol());
            this.buildSell(researchLedgerTechnical, stockPrice);
            isUpdation = Boolean.TRUE;
            log.info("Verified target {}", stock.getNseSymbol());
        }
        else if (candleStickService.isRed(stock) && this.isStopLossTriggered(researchLedgerTechnical, stockPrice)) {
            log.info("Verifying stop loss {}", stock.getNseSymbol());
            this.buildSell(researchLedgerTechnical, stockPrice);
            isUpdation = Boolean.TRUE;
            log.info("Verified stop loss {}", stock.getNseSymbol());
        }/*
        else if(stockPrice.getBhavDate().isEqual(researchLedgerTechnical.getNextTradingDate()) && researchLedgerTechnical.getStrategy() == ResearchLedgerTechnical.Strategy.VOLUME_ACTION){
            log.info("Updating stop loss {}", stock.getNseSymbol());
            if(candleStickService.isRed(stock) && candleStickService.isCloseBelowPrevClose(stock)) {
                    if(candleStickService.isLowerHigh(stock)) {
                        researchLedgerTechnical.setStopLoss(stockPrice.getLow());
                        isUpdation = Boolean.TRUE;
                    }
            }
            log.info("Updated stop loss {}", stock.getNseSymbol());
        }*/ else if (priceActionService.breakDown(stock).isActive() || movingAverageActionService.breakDown(stock).isActive()) {
            this.buildSell(researchLedgerTechnical, stockPrice);
            isUpdation = Boolean.TRUE;
        }/*
        else if(candleStickService.isRed(stock) && technicalsResearchService.isBreakDownOnTop(stock)){
            this.buildSell(researchLedgerTechnical, stockPrice);
            isUpdation = Boolean.TRUE;
        }
        */
        /*
        else if(researchLedgerTechnical.getStrategy() == ResearchLedgerTechnical.Strategy.MA_WITH_MACD && candleStickService.isRed(stock) && technicalsResearchService.isHistogramBreakDown(stock)){
            this.buildSell(researchLedgerTechnical, stockPrice);
            isUpdation = Boolean.TRUE;
        }*/

        if (isUpdation) {
            researchLedgerTechnicalService.update(researchLedgerTechnical);
        }

        log.info("Executed technical sell {}", stock.getNseSymbol());
    }

    private void buildSell(ResearchLedgerTechnical researchLedgerTechnical, StockPrice stockPrice){
        researchLedgerTechnical.setResearchStatus(ResearchIO.ResearchTrigger.SELL);
        researchLedgerTechnical.setExitDate(stockPrice.getBhavDate());
        researchLedgerTechnical.setExitPrice(stockPrice.getClose());
    }


    private boolean isStopLossTriggered(ResearchLedgerTechnical researchLedgerTechnical, StockPrice stockPrice){

        Stock stock= researchLedgerTechnical.getStock();

            if (researchLedgerTechnical.getStopLoss() > stockPrice.getClose()) {
                breakoutLedgerService.addNegative(stock, BreakoutLedger.BreakoutCategory.STOPLOSS_TRIGGERED);
                log.info("Stop loss triggered {} stoploss {}", stock.getNseSymbol(), researchLedgerTechnical.getStopLoss());
                return Boolean.TRUE;
            }

        return Boolean.FALSE;
    }

    private boolean isTargetAchieved(ResearchLedgerTechnical researchLedgerTechnical, StockPrice stockPrice){

             if (researchLedgerTechnical.getTarget() <= stockPrice.getClose()) {
                 Stock stock= researchLedgerTechnical.getStock();
                 breakoutLedgerService.addNegative(stock, BreakoutLedger.BreakoutCategory.TARGET_ACHIEVED);
                 log.info("Target achieved {} target {}", stock.getNseSymbol(), researchLedgerTechnical.getTarget());
                 return Boolean.TRUE;
            }

        return Boolean.FALSE;
    }

}
