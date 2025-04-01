package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.ValuationLedger;
import com.example.data.transactional.entities.ValuationLedger.Status;
import com.example.data.transactional.entities.ValuationLedger.Type;
import com.example.data.transactional.repo.ValuationLedgerRepository;
import com.example.util.MiscUtil;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Transactional
@Service
public class ValuationLedgerService {

    @Autowired private ValuationLedgerRepository valuationLedgerRepository;

    @Autowired private StockService stockService;

    @Autowired private MiscUtil miscUtil;

    @Autowired private StockPriceService<StockPrice> stockPriceService;

    public List<Stock> getCurrentUndervalueStocks() {

        List<ValuationLedger> underValueLedgerList =
                valuationLedgerRepository.findByResearchDate(miscUtil.currentDate());

        return underValueLedgerList.stream()
                .map(uvl -> uvl.getStockId())
                .collect(Collectors.toList());
    }

    public ValuationLedger addUndervalued(Stock stock) {

        StockPrice stockPrice = stockPriceService.get(stock, Timeframe.DAILY);

        ValuationLedger valuationLedger =
                valuationLedgerRepository.findByStockIdAndTypeAndStatus(
                        stock, Type.UNDERVALUE, Status.OPEN);

        if (valuationLedger == null) {
            valuationLedger = new ValuationLedger();

            double pe = stockService.getPe(stock);

            double pb = stockService.getPb(stock);

            valuationLedger.setStockId(stock);
            valuationLedger.setPb(pb);
            valuationLedger.setPe(pe);
            valuationLedger.setResearchDate(stockPrice.getSessionDate());

            valuationLedger.setType(Type.UNDERVALUE);

            valuationLedger.setStatus(Status.OPEN);

            valuationLedger.setPrice(stockPrice.getHigh());

            valuationLedger.setCurrentRatio(stock.getFactor().getCurrentRatio());

            valuationLedger.setDebtEquity(stock.getFactor().getDebtEquity());

            valuationLedger.setDividend(stock.getFactor().getDividend());

            valuationLedger.setSectorPb(stock.getSector().getSectorPb());

            valuationLedger.setSectorPe(stock.getSector().getSectorPe());

            valuationLedgerRepository.save(valuationLedger);
        }

        ValuationLedger valuationLedgerPrevOverValued =
                valuationLedgerRepository.findByStockIdAndTypeAndStatus(
                        stock, Type.OVERVALUE, Status.OPEN);

        if (valuationLedgerPrevOverValued != null) {
            valuationLedgerPrevOverValued.setStatus(Status.CLOSE);
            valuationLedgerPrevOverValued.setLastModified(LocalDate.now());
            valuationLedgerRepository.save(valuationLedgerPrevOverValued);
        }

        return valuationLedger;
    }

    public ValuationLedger addOvervalued(Stock stock) {
        StockPrice stockPrice = stockPriceService.get(stock, Timeframe.DAILY);

        ValuationLedger valuationLedger =
                valuationLedgerRepository.findByStockIdAndTypeAndStatus(
                        stock, Type.OVERVALUE, Status.OPEN);

        if (valuationLedger == null) {
            valuationLedger = new ValuationLedger();

            double pe = stockService.getPe(stock);

            double pb = stockService.getPb(stock);

            valuationLedger.setStockId(stock);
            valuationLedger.setPb(pb);
            valuationLedger.setPe(pe);
            valuationLedger.setResearchDate(stockPrice.getSessionDate());

            // undervalueLedger.setCategory(category);

            valuationLedger.setType(Type.OVERVALUE);

            valuationLedger.setStatus(Status.OPEN);

            valuationLedger.setPrice(stockPrice.getClose());

            valuationLedger.setCurrentRatio(stock.getFactor().getCurrentRatio());

            valuationLedger.setDebtEquity(stock.getFactor().getDebtEquity());

            valuationLedger.setDividend(stock.getFactor().getDividend());

            valuationLedger.setSectorPb(stock.getSector().getSectorPb());

            valuationLedger.setSectorPe(stock.getSector().getSectorPe());

            valuationLedgerRepository.save(valuationLedger);
        }

        ValuationLedger valuationLedgerPrevUnderValued =
                valuationLedgerRepository.findByStockIdAndTypeAndStatus(
                        stock, Type.UNDERVALUE, Status.OPEN);

        if (valuationLedgerPrevUnderValued != null) {
            valuationLedgerPrevUnderValued.setStatus(Status.CLOSE);
            valuationLedgerPrevUnderValued.setLastModified(LocalDate.now());
            valuationLedgerRepository.save(valuationLedgerPrevUnderValued);
        }

        return valuationLedger;
    }
}
