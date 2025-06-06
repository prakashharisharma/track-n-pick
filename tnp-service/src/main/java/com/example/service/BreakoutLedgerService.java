package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.BreakoutLedger;
import com.example.data.transactional.entities.BreakoutLedger.BreakoutCategory;
import com.example.data.transactional.entities.BreakoutLedger.BreakoutType;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.repo.BreakoutLedgerRepository;
import java.time.LocalDate;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Transactional
@Service
public class BreakoutLedgerService {
    @Autowired private BreakoutLedgerRepository breakoutLedgerRepository;

    @Autowired private StockPriceService<StockPrice> stockPriceService;

    public void addPositive(Stock stock, Timeframe timeframe, BreakoutCategory breakoutCategory) {

        LocalDate bhavDate = stockPriceService.get(stock, timeframe).getSessionDate();

        BreakoutLedger breakoutLedger =
                breakoutLedgerRepository
                        .findByStockIdAndBreakoutTypeAndBreakoutCategoryAndBreakoutDate(
                                stock,
                                BreakoutLedger.BreakoutType.POSITIVE,
                                breakoutCategory,
                                bhavDate);

        if (breakoutLedger == null) {

            breakoutLedger =
                    new BreakoutLedger(
                            stock,
                            bhavDate,
                            BreakoutLedger.BreakoutType.POSITIVE,
                            breakoutCategory);

            breakoutLedgerRepository.save(breakoutLedger);
        }
    }

    public void addNegative(Stock stock, Timeframe timeframe, BreakoutCategory breakoutCategory) {

        LocalDate bhavDate = stockPriceService.get(stock, timeframe).getSessionDate();

        BreakoutLedger breakoutLedger =
                breakoutLedgerRepository
                        .findByStockIdAndBreakoutTypeAndBreakoutCategoryAndBreakoutDate(
                                stock,
                                BreakoutLedger.BreakoutType.NEGATIVE,
                                breakoutCategory,
                                bhavDate);

        if (breakoutLedger == null) {

            breakoutLedger =
                    new BreakoutLedger(
                            stock,
                            bhavDate,
                            BreakoutLedger.BreakoutType.NEGATIVE,
                            breakoutCategory);

            breakoutLedgerRepository.save(breakoutLedger);
        }
    }

    public List<BreakoutLedger> getBreakouts(Stock stock) {

        List<BreakoutLedger> breakoutList =
                breakoutLedgerRepository.findByStockIdOrderByBreakoutDateDesc(stock);

        return breakoutList;
    }

    public boolean isBreakout(
            Stock stock, BreakoutType breakoutType, BreakoutCategory breakoutCategory) {

        boolean isBreakout = false;

        BreakoutLedger breakoutLedger =
                breakoutLedgerRepository
                        .findByStockIdAndBreakoutTypeAndBreakoutCategoryAndBreakoutDate(
                                stock, breakoutType, breakoutCategory, LocalDate.now());

        if (breakoutLedger != null) {
            isBreakout = true;
        }

        return isBreakout;
    }

    public BreakoutLedger get(
            Stock stock, BreakoutType breakoutType, BreakoutCategory breakoutCategory) {
        return breakoutLedgerRepository.findByStockIdAndBreakoutTypeAndBreakoutCategory(
                stock, breakoutType, breakoutCategory);
    }

    public void update(BreakoutLedger breakoutLedger) {
        breakoutLedgerRepository.save(breakoutLedger);
    }
}
