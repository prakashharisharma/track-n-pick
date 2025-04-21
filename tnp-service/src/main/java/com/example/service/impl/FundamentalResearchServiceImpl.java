package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.FinancialsSummary;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.service.StockPriceService;
import com.example.service.StockService;
import com.example.util.MiscUtil;
import com.example.util.rules.RulesFundamental;
import com.example.util.rules.RulesResearch;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Transactional
@Service
@Slf4j
public class FundamentalResearchServiceImpl implements FundamentalResearchService {

    @Autowired private RulesFundamental rules;

    @Autowired private RulesResearch researchRules;

    @Autowired private StockService stockService;

    @Autowired private StockPriceService<StockPrice> stockPriceService;

    @Autowired private MiscUtil miscUtil;

    @Override
    public boolean isPriceInRange(Stock stock) {

        StockPrice stockPrice = stockPriceService.get(stock, Timeframe.DAILY);
        if (stockPrice.getClose() > rules.getPricegt()
                && stockPrice.getClose() < rules.getPricelt()) {

            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isMcapInRange(Stock stock) {

        double marketCapInCr = this.marketCap(stock);

        if (marketCapInCr >= rules.getMcap()) {
            return true;
        }

        return false;
    }

    @Override
    public double marketCap(Stock stock) {

        StockPrice stockPrice = stockPriceService.get(stock, Timeframe.DAILY);
        FinancialsSummary financialsSummary = stock.getFinancialsSummary();
        if (financialsSummary != null && stockPrice != null) {
            if (financialsSummary.getIssuedSize() >= 0) {
                double marketCap = financialsSummary.getIssuedSize() * stockPrice.getClose();
                double marketCapInCr = marketCap / 1_00_00_000.0;

                // log.info("{} MarketCap: {} Cr.", stock.getNseSymbol(), marketCapInCr);
                return miscUtil.roundToTwoDecimals(marketCapInCr);
            }
        }

        return 0;
    }

    @Override
    public double calculateValuation(Stock stock) {

        double score = 0.0;

        // double pe = stockService.getPe(stock);
        double pe = 0.0;
        // double pb = stockService.getPb(stock);
        double pb = 0.0;
        double sectorPb = stock.getSector().getSectorPb();

        double sectorPe = stock.getSector().getSectorPe();

        if (pe < 20 && pb < 3) {
            return 100.0;
        }

        if (pe < 20 && pb < sectorPb) {
            return 80.0;
        }

        if (pe < sectorPe && pb < 3) {
            return 65.0;
        }

        if (pe < sectorPe && pb < sectorPb) {
            return 55.0;
        }

        if (pe < sectorPe) {
            return 50.0;
        }

        return score;
    }

    @Override
    public boolean isGoodValuation(Stock stock) {
        return (this.calculateValuation(stock) >= 50.0) ? Boolean.TRUE : Boolean.FALSE;
    }

    @Override
    public boolean isUndervalued(Stock stock) {

        if (this.isFinancialsStable(stock) && this.isGoodValuation(stock)) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    @Override
    public boolean isOvervalued(Stock stock) {

        if (!this.isUndervalued(stock)) {
            // if (!this.isFinancialsStable(stock)) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    private boolean isFinancialsStable(Stock stock) {
        /*

        boolean isUndervalued = false;

        Sector sector = stock.getSector();


        if (stock != null) {

            if (sector != null) {

                String sectorName = stock.getSector().getSectorName();

                if (sectorName != null && !sectorName.isEmpty()) {
                    StockFinancials stockFinancials = stock.getFactor();
                    if (stockFinancials != null) {

                        if (sectorName.equalsIgnoreCase("Financial Institution")
                                || sectorName.equalsIgnoreCase("Non Banking Financial Company NBFC")
                                || sectorName.equalsIgnoreCase("Other Bank")
                                || sectorName.equalsIgnoreCase("Private Sector Bank")
                                || sectorName.equalsIgnoreCase("Public Sector Bank")) {

                            if (this.isMcapInRange(stock)
                                    && stock.getFactor().getDividend() >= rules.getDividend()
                                    && stock.getFactor().getReturnOnEquity() >= rules.getRoe()
                                    && stock.getFactor().getReturnOnCapital() >= rules.getRoce()
                                    && stock.getFactor().getCurrentRatio()
                                            >= rules.getCurrentRatio()
                                    && stock.getFactor().getQuickRatio()
                                            >= rules.getQuickRatioBanks()) {
                                isUndervalued = true;
                            }

                        } else {

                            if (this.isMcapInRange(stock)
                                    && stock.getFactor().getDebtEquity() < rules.getDebtEquity()
                                    && stock.getFactor().getDividend() >= rules.getDividend()
                                    && stock.getFactor().getReturnOnEquity() >= rules.getRoe()
                                    && stock.getFactor().getReturnOnCapital() >= rules.getRoce()
                                    && stock.getFactor().getCurrentRatio()
                                            >= rules.getCurrentRatio()) {

                                isUndervalued = true;
                            }
                        }
                    }
                }
            }
        }
        return isUndervalued;
         */
        return false;
    }
}
