package com.example.service.impl;

import com.example.model.ledger.ValuationLedger;
import com.example.model.master.Stock;
import com.example.service.*;
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
    private CandleStickService candleStickService;

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

        if(researchLedgerTechnicalService.isResearchActive(stock)){
            this.technicalSell(stock);
        }else {
            this.technicalBuy(stock);
        }
        log.info("Executed technical research {}", stock.getNseSymbol());
    }

    private void fundamentalBuy(Stock stock){

        if(fundamentalResearchService.isUndervalued(stock)){

            ValuationLedger entryValuation = valuationLedgerService.addUndervalued(stock);

            researchLedgerFundamentalService.addResearch(stock, entryValuation);
        }
    }

    private void fundamentalSell(Stock stock){

        if(fundamentalResearchService.isOvervalued(stock)){

            ValuationLedger exitValuation = valuationLedgerService.addOvervalued(stock);

            researchLedgerFundamentalService.updateResearch(stock, exitValuation);
        }
    }


    private void technicalBuy(Stock stock){

        if (fundamentalResearchService.isMcapInRange(stock) && technicalsResearchService.isBullishMovingAverage(stock)) {

            double score = technicalsResearchService.breakoutScore(stock);

            if(score > 0.5 && !candleStickService.isSellingWickPresent(stock)) {

                boolean isYearHigh = technicalsResearchService.isYearHigh(stock);

                if(isYearHigh){
                    researchLedgerTechnicalService.addResearch(stock,  score, Boolean.TRUE);
                }else {
                    researchLedgerTechnicalService.addResearch(stock, score);
                }
            }
        }
    }

    private void technicalSell(Stock stock){

        if (technicalsResearchService.isBearishMovingAverage(stock)) {

            double score = technicalsResearchService.breakoutScore(stock);

            if(score > 0.5) {
                researchLedgerTechnicalService.updateResearch(stock, score);
            }
        }
    }
}
