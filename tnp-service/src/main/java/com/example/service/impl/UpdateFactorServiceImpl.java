package com.example.service.impl;

import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockFactor;
import com.example.service.StockService;
import com.example.service.UpdateFactorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UpdateFactorServiceImpl implements UpdateFactorService {

    @Autowired private StockService stockService;

    @Autowired private FundamentalResearchService fundamentalResearchService;

    @Override
    public void updateFactors(Stock stock) {

        log.info("{} Starting factors update.", stock.getNseSymbol());
        try {
            this.updateFactorsTxn(stock);
        } catch (Exception e) {
            log.error("An error occured while updating factors {}", stock.getNseSymbol(), e);
        }
        log.info("{} Completed factors update.", stock.getNseSymbol());
    }

    private void updateFactorsTxn(Stock stock) {

        log.info("{} Updating transactional factors.", stock.getNseSymbol());

        // Stock stock = stockService.getStockByNseSymbol(stockPriceIO.getNseSymbol());

        StockFactor prevStockFactor = null;

        StockFactor newStockFactor = null;

        prevStockFactor = stock.getFactor();

        newStockFactor = stockService.updateFactor(stock);

        if (prevStockFactor != null) {

            if (newStockFactor.getQuarterEnded().isAfter(prevStockFactor.getQuarterEnded())) {

                // TODO: UPdate history

                log.info("{} Updated transactional factors.", stock.getNseSymbol());
            }
        }
    }
}
