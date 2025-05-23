package com.example.service;

import com.example.data.transactional.entities.Sector;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockFinancials;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.repo.StockFinancialsRepository;
import com.example.data.transactional.repo.StockRepository;
import com.example.data.transactional.view.StockSearchResult;
import com.example.external.factor.FactorRediff;
import com.example.model.type.Exchange;
import com.example.model.type.IndiceType;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import com.example.util.rules.RulesNotification;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Transactional
@Service
public class StockService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StockService.class);

    @Autowired private StockRepository stockRepository;

    @Autowired private StockFinancialsRepository stockFinancialsRepository;

    @Autowired private StockPriceService<StockPrice> stockPriceService;

    @Autowired private FactorRediff factorRediff;
    @Autowired private FormulaService formulaService;
    @Autowired private RulesNotification notificationRules;

    @Autowired private MiscUtil miscUtil;

    private static List<Stock> allstocks = null;

    public Stock getStockByIsinCode(String isinCode) {
        return stockRepository.findByIsinCode(isinCode);
    }

    public Stock getStockByNseSymbol(String nseSymbol) {
        return stockRepository.findFirstByNseSymbol(nseSymbol);
    }

    public Stock getStockById(long stockId) {
        return stockRepository
                .findByStockId(stockId)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Stock with ID " + stockId + " not found."));
    }

    public List<Stock> getActiveStocks() {
        return stockRepository.findByActive(true);
    }

    public Stock save(Stock stock) {
        return stockRepository.save(stock);
    }

    public List<Stock> nifty500() {

        List<IndiceType> indiceList = new ArrayList<>();

        indiceList.add(IndiceType.NIFTY50);
        indiceList.add(IndiceType.NIFTY100);
        indiceList.add(IndiceType.NIFTY250);
        indiceList.add(IndiceType.NIFTY500);

        List<Stock> stockList = stockRepository.findByActiveAndPrimaryIndiceIn(true, indiceList);

        return stockList;
    }

    public List<Stock> nifty50() {

        List<IndiceType> indiceList = new ArrayList<>();

        indiceList.add(IndiceType.NIFTY50);

        List<Stock> stockList = stockRepository.findByActiveAndPrimaryIndiceIn(true, indiceList);

        return stockList;
    }

    public List<Stock> nifty100() {

        List<IndiceType> indiceList = new ArrayList<>();

        indiceList.add(IndiceType.NIFTY50);
        indiceList.add(IndiceType.NIFTY100);

        List<Stock> stockList = stockRepository.findByActiveAndPrimaryIndiceIn(true, indiceList);

        return stockList;
    }

    public List<Stock> activeStocks() {

        if (allstocks == null) {
            allstocks = stockRepository.findByActive(true);
        }

        return allstocks;
    }

    public List<Stock> getForActivity() {
        return stockRepository.findByActivityCompleted(Boolean.FALSE);
    }

    public void setInactive(List<Stock> discontinueList) {

        for (Stock stock : discontinueList) {
            stock.setActive(false);
            stockRepository.save(stock);
        }
    }

    public void resetMaster() {
        List<Stock> stocksList = stockRepository.findByActive(true);
        for (Stock stock : stocksList) {
            stock.setActive(false);
            stock.setPrimaryIndice(IndiceType.NIFTY1500);
            stockRepository.save(stock);
        }
    }

    public Stock add(
            Exchange exchange,
            String series,
            String isinCode,
            String companyName,
            String nseSymbol,
            String bseCode,
            IndiceType primaryIndice,
            Sector sectorName) {

        Stock stock =
                new Stock(exchange, isinCode, companyName, nseSymbol, primaryIndice, sectorName);

        if (bseCode != null && !bseCode.isBlank()) {
            stock.setBseCode(bseCode);
        }

        stock.setSeries(series);
        stock.setActive(Boolean.TRUE);

        stock = stockRepository.save(stock);

        // this.updatePrice(stock);

        // this.updateFactor(stock);

        return stock;
    }

    public boolean isActive(String nseSymbol) {

        boolean isActive = false;

        Stock stock = this.getStockByNseSymbol(nseSymbol);

        if (stock != null && stock.isActive()) {
            isActive = true;
        }

        return isActive;
    }

    public StockFinancials updateFactor(Stock stock) {
        /*

        LOGGER.info("Updating Factor for : " + stock.getNseSymbol());

        StockFinancials stockFinancials = stock.getFactor();

        if (stock.getFactor() != null) {

            if (DAYS.between(stock.getFactor().getLastModified(), LocalDate.now())
                    > notificationRules.getFactorIntervalDays()) {

                if (DownloadCounterUtil.get() < notificationRules.getApiCallCounter()) {

                    stockFinancials = factorRediff.getFactor(stock);

                    stockFinancialsRepository.save(stockFinancials);

                    DownloadCounterUtil.increment();
                } else {
                    LOGGER.warn(
                            "Counter exceeds {} for {} ",
                            DownloadCounterUtil.get(),
                            stock.getNseSymbol());
                }

            } else {
                LOGGER.info("Factors recently updated.. {}", stock.getNseSymbol());
            }
        } else {

            stockFinancials = factorRediff.getFactor(stock);

            stockFinancialsRepository.save(stockFinancials);
        }

        return stockFinancials;
        */
        return null;
    }

    public List<StockSearchResult> searchStocks(String query) {
        if (query == null || query.trim().length() < 2) {
            return Collections.emptyList(); // or maybe fetch top gainers/most active instead?
        }
        return stockRepository.searchStocks(query);
    }
}
