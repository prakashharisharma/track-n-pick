package com.example.service.impl;

import com.example.data.transactional.entities.Stock;
import com.example.service.StockFinancialsService;
import com.example.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class StockFinancialsServiceImpl implements StockFinancialsService {

    @Autowired private StockService stockService;

    @Autowired private FundamentalResearchService fundamentalResearchService;

    @Override
    public void update(Stock stock) {

        log.info("{} Starting factors update.", stock.getNseSymbol());
        try {
            this.updateFinancialsTxn(stock);
        } catch (Exception e) {
            log.error("An error occured while updating factors {}", stock.getNseSymbol(), e);
        }
        log.info("{} Completed factors update.", stock.getNseSymbol());
    }

    private void updateFinancialsTxn(Stock stock) {
        /*

        log.info("{} Updating transactional factors.", stock.getNseSymbol());

        StockFinancials prevStockFinancials = null;

        StockFinancials newStockFinancials = null;

        prevStockFinancials = stock.getFactor();

        newStockFinancials = stockService.updateFactor(stock);

        if (prevStockFinancials != null) {

            if (newStockFinancials.getQuarterEnded().isAfter(prevStockFinancials.getQuarterEnded())) {

                // TODO: UPdate history

                log.info("{} Updated transactional factors.", stock.getNseSymbol());
            }
        }*/
    }
}
