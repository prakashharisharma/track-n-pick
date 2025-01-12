package com.example.service.impl;

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

    @Autowired
    private ResearchLedgerFundamentalService researchLedgerFundamentalService;

    @Autowired
    private ResearchLedgerTechnicalService researchLedgerTechnicalService;

    @Autowired
    private FundamentalResearchService fundamentalResearchService;

    @Autowired
    private TechnicalsResearchService technicalsResearchService;

    @Autowired
    private ValuationLedgerService valuationLedgerService;

    @Autowired
    private BreakoutLedgerService breakoutLedgerService;

    @Autowired
    private CandleStickService candleStickService;

    @Autowired
    private FormulaService formulaService;

    @Autowired
    private CalendarService calendarService;


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

       if( researchLedgerTechnical!= null){
            this.technicalSell(stock, researchLedgerTechnical);
       }else {
           this.technicalBuy(stock);

       }
        log.info("Executed technical research {}", stock.getNseSymbol());
    }

    private void fundamentalBuy(Stock stock){
        log.info("Executing fundamental buy {}", stock.getNseSymbol());
        if(fundamentalResearchService.isUndervalued(stock)){

            ValuationLedger entryValuation = valuationLedgerService.addUndervalued(stock);

            researchLedgerFundamentalService.addResearch(stock, entryValuation);
        }
        log.info("Executed fundamental buy {}", stock.getNseSymbol());
    }

    private void fundamentalSell(Stock stock){

        if(fundamentalResearchService.isOvervalued(stock)){
            log.info("Executing fundamental sell {}", stock.getNseSymbol());
            ValuationLedger exitValuation = valuationLedgerService.addOvervalued(stock);

            researchLedgerFundamentalService.updateResearch(stock, exitValuation);
            log.info("Executed fundamental sell {}", stock.getNseSymbol());
        }
    }

    private void technicalBuy(Stock stock){
        log.info("Executing technical buy {}", stock.getNseSymbol());

        if (fundamentalResearchService.isMcapInRange(stock) && candleStickService.isGreen(stock)) {

            if(stock.getStockPrice().getClose() > stock.getTechnicals().getEma200()) {
                if(technicalsResearchService.isMovingAverageBreakout(stock)){
                    researchLedgerTechnicalService.addResearch(stock, Boolean.FALSE, ResearchLedgerTechnical.Strategy.MA_WITH_MACD);
                }else if (technicalsResearchService.isPriceActionBreakOut(stock)) {
                    researchLedgerTechnicalService.addResearch(stock, Boolean.FALSE, ResearchLedgerTechnical.Strategy.PRICE_ACTION);
                }
            }
        }
        log.info("Executed technical buy {}", stock.getNseSymbol());
    }

    private void technicalSell(Stock stock, ResearchLedgerTechnical researchLedgerTechnical){

        log.info("Executing technical sell {}", stock.getNseSymbol());

        StockPrice stockPrice = stock.getStockPrice();
        boolean isUpdation = Boolean.FALSE;

        if(technicalsResearchService.isPriceActionBreakOut(stock) && candleStickService.isGreen(stock) ){
            if( stock.getTechnicals().getVolume() > researchLedgerTechnical.getVolume()) {
                researchLedgerTechnical.setVolume(stock.getTechnicals().getVolume());
                researchLedgerTechnical.setWeeklyVolume(stock.getTechnicals().getWeeklyVolume());
                researchLedgerTechnical.setStopLoss(stockPrice.getLow());
                researchLedgerTechnical.setStrategy(ResearchLedgerTechnical.Strategy.PRICE_ACTION);
                researchLedgerTechnical.setTarget(formulaService.calculateTarget(stockPrice.getHigh(), stockPrice.getLow()));
                researchLedgerTechnical.setNextTradingDate(calendarService.nextTradingDate(stockPrice.getBhavDate()));
                isUpdation = Boolean.TRUE;
            }
        }else if(this.isStopLossTriggered(researchLedgerTechnical, stockPrice)){
            log.info("Verifying stop loss {}", stock.getNseSymbol());
            this.buildSell(researchLedgerTechnical, stockPrice);
            isUpdation = Boolean.TRUE;
            log.info("Verified stop loss {}", stock.getNseSymbol());
        }else if(stockPrice.getBhavDate().isEqual(researchLedgerTechnical.getNextTradingDate()) && researchLedgerTechnical.getStrategy() == ResearchLedgerTechnical.Strategy.PRICE_ACTION){
            log.info("Updating stop loss {}", stock.getNseSymbol());
            if(candleStickService.isRed(stock) && (stockPrice.getClose() < stockPrice.getPrevClose())) {
                    if(stockPrice.getHigh() <= stockPrice.getPrevHigh() ) {
                        researchLedgerTechnical.setStopLoss(stockPrice.getLow());
                        isUpdation = Boolean.TRUE;
                    }
            }
            log.info("Updated stop loss {}", stock.getNseSymbol());
        }
        else if(this.isTargetAchieved(researchLedgerTechnical, stockPrice)){
            log.info("Verifying target {}", stock.getNseSymbol());
            this.buildSell(researchLedgerTechnical, stockPrice);
            isUpdation = Boolean.TRUE;
            log.info("Verified target {}", stock.getNseSymbol());
        }else if(candleStickService.isRed(stock) && technicalsResearchService.isBreakDown(stock)){
            this.buildSell(researchLedgerTechnical, stockPrice);
            isUpdation = Boolean.TRUE;
        }else if(technicalsResearchService.isBreakDownOnTop(stock)){
            this.buildSell(researchLedgerTechnical, stockPrice);
            isUpdation = Boolean.TRUE;
        }
        /*
        else if(researchLedgerTechnical.getStrategy() == ResearchLedgerTechnical.Strategy.MA_WITH_MACD && candleStickService.isRed(stock) && technicalsResearchService.isHistogramBreakDown(stock)){
            this.buildSell(researchLedgerTechnical, stockPrice);
            isUpdation = Boolean.TRUE;
        }*/

        if(isUpdation) {
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
        double score = 0.0;

        if(candleStickService.isRed(stock) && !candleStickService.isBuyingWickPresent(stock)) {
            if (researchLedgerTechnical.getStopLoss() > stockPrice.getClose()) {
                score = 10.0;
            }
        }

        if(score > 1.0) {
            breakoutLedgerService.addNegative(stock, BreakoutLedger.BreakoutCategory.STOPLOSS_TRIGGERED);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    private boolean isTargetAchieved(ResearchLedgerTechnical researchLedgerTechnical, StockPrice stockPrice){
        Stock stock= researchLedgerTechnical.getStock();
        double score = 0.0;

         if(stockPrice.getClose() < stock.getTechnicals().getEma10()) {
             if (researchLedgerTechnical.getTarget() < stockPrice.getClose()) {
                score = 100.0;
            }
        }
        if(score > 1.0) {
            breakoutLedgerService.addNegative(stock, BreakoutLedger.BreakoutCategory.TARGET_ACHIEVED);
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

}
