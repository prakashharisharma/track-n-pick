package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockPriceDaily;
import com.example.data.transactional.entities.StockPriceMonthly;
import com.example.data.transactional.entities.StockPriceQuarterly;
import com.example.data.transactional.entities.StockPriceWeekly;
import com.example.data.transactional.entities.StockPriceYearly;
import com.example.data.transactional.repo.StockPriceRepository;
import com.example.data.transactional.repo.StockRepository;
import com.example.service.StockPriceService;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Supplier;
import javax.persistence.EntityNotFoundException;

import com.example.service.utils.PivotPointUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockPriceServiceImpl implements StockPriceService {

    private final StockPriceRepository<StockPrice> stockPriceRepository;
    private final StockRepository stockRepository;

    // Map to create instances dynamically based on timeframe
    private static final Map<Timeframe, Supplier<StockPrice>> STOCK_PRICE_CREATORS =
            Map.of(
                    Timeframe.DAILY, StockPriceDaily::new,
                    Timeframe.WEEKLY, StockPriceWeekly::new,
                    Timeframe.MONTHLY, StockPriceMonthly::new,
                    Timeframe.QUARTERLY, StockPriceQuarterly::new,
                    Timeframe.YEARLY, StockPriceYearly::new);

    @Override
    public StockPrice create(
            Long stockId,
            Timeframe timeframe,
            Double open,
            Double high,
            Double low,
            Double close,
            LocalDate sessionDate) {
        Stock stock = getStockById(stockId);
        return create(stock, timeframe, open, high, low, close, sessionDate);
    }

    @Override
    public StockPrice create(
            Stock stock,
            Timeframe timeframe,
            Double open,
            Double high,
            Double low,
            Double close,
            LocalDate sessionDate) {
        StockPrice existingStockPrice =
                stockPriceRepository
                        .findByStockIdAndTimeframe(stock.getStockId(), timeframe)
                        .orElse(null);

        // If a record with the same sessionDate exists, do nothing (return existing record)
        if (existingStockPrice != null
                && !sessionDate.isAfter(existingStockPrice.getSessionDate())) {
            log.info(
                    "Skipping creation. A StockPrice record already exists for stockId: {},"
                            + " timeframe: {}, sessionDate: {}",
                    stock.getStockId(),
                    timeframe,
                    sessionDate);
            return existingStockPrice;
        }

        StockPrice newStockPrice =
                STOCK_PRICE_CREATORS
                        .getOrDefault(
                                timeframe,
                                () -> {
                                    throw new IllegalArgumentException(
                                            "Unsupported timeframe: " + timeframe);
                                })
                        .get();

        newStockPrice.setStock(stock);
        newStockPrice.setTimeframe(timeframe);
        newStockPrice.setOpen(open);
        newStockPrice.setHigh(high);
        newStockPrice.setLow(low);
        newStockPrice.setClose(close);
        newStockPrice.setSessionDate(sessionDate);

        log.info("Creating new {} StockPrice for stockId: {}", timeframe, stock.getStockId());
        return stockPriceRepository.save(newStockPrice);
    }

    @Override
    public StockPrice update(
            Long stockId,
            Timeframe timeframe,
            Double open,
            Double high,
            Double low,
            Double close,
            LocalDate sessionDate) {
        Stock stock = getStockById(stockId);
        return update(stock, timeframe, open, high, low, close, sessionDate);
    }

    @Override
    public StockPrice update(
            Stock stock,
            Timeframe timeframe,
            Double open,
            Double high,
            Double low,
            Double close,
            LocalDate sessionDate) {
        StockPrice stockPrice =
                stockPriceRepository
                        .findByStockIdAndTimeframe(stock.getStockId(), timeframe)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "StockPrice not found for stockId: "
                                                        + stock.getStockId()
                                                        + " and timeframe: "
                                                        + timeframe));

        // If sessionDate is the same or earlier, do nothing
        if (!sessionDate.isAfter(stockPrice.getSessionDate())) {
            log.info(
                    "Skipping update. Provided sessionDate: {} is not after existing sessionDate:"
                            + " {} for stockId: {}, timeframe: {}",
                    sessionDate,
                    stockPrice.getSessionDate(),
                    stock.getStockId(),
                    timeframe);
            return stockPrice; // Return existing record without modification
        }

        // Shift Price before updating
        shiftPreviousPrice(stockPrice);

        stockPrice.setOpen(open);
        stockPrice.setHigh(high);
        stockPrice.setLow(low);
        stockPrice.setClose(close);

        // Calculate and set Pivot levels before updating
        setPivotLevels(stockPrice);

        stockPrice.setSessionDate(sessionDate);

        log.info("Updating {} StockPrice for stockId: {}", timeframe, stock.getStockId());
        return stockPriceRepository.save(stockPrice);
    }

    @Override
    public StockPrice createOrUpdate(
            Long stockId,
            Timeframe timeframe,
            Double open,
            Double high,
            Double low,
            Double close,
            LocalDate sessionDate) {
        StockPrice existingStockPrice = get(stockId, timeframe);

        if (existingStockPrice != null) {

            StockPrice stockPrice = existingStockPrice;

            // If sessionDate is the same, do nothing
            if (sessionDate.equals(stockPrice.getSessionDate())) {
                log.info(
                        "StockPrice already exists for stockId: {}, timeframe: {}, sessionDate: {}."
                                + " No action taken.",
                        stockId,
                        timeframe,
                        sessionDate);
                return stockPrice;
            }

            // If sessionDate is after, update it
            if (sessionDate.isAfter(stockPrice.getSessionDate())) {
                return update(stockId, timeframe, open, high, low, close, sessionDate);
            }

            // If sessionDate is before, do nothing
            log.info(
                    "Skipping update. Provided sessionDate: {} is before existing sessionDate: {}"
                            + " for stockId: {}, timeframe: {}",
                    sessionDate,
                    stockPrice.getSessionDate(),
                    stockId,
                    timeframe);
            return stockPrice;
        }

        // No existing record, create a new one
        return create(stockId, timeframe, open, high, low, close, sessionDate);
    }

    @Override
    public StockPrice createOrUpdate(
            Stock stock,
            Timeframe timeframe,
            Double open,
            Double high,
            Double low,
            Double close,
            LocalDate sessionDate) {
        StockPrice existingStockPrice = get(stock, timeframe);

        if (existingStockPrice != null) {
            StockPrice stockPrice = existingStockPrice;

            // If sessionDate is the same, do nothing
            if (sessionDate.equals(stockPrice.getSessionDate())) {
                log.info(
                        "StockPrice already exists for stockId: {}, timeframe: {}, sessionDate: {}."
                                + " No action taken.",
                        stock.getStockId(),
                        timeframe,
                        sessionDate);
                return stockPrice;
            }

            // If sessionDate is after, update it
            if (sessionDate.isAfter(stockPrice.getSessionDate())) {
                return update(stock, timeframe, open, high, low, close, sessionDate);
            }

            // If sessionDate is before, do nothing
            log.info(
                    "Skipping update. Provided sessionDate: {} is before existing sessionDate: {}"
                            + " for stockId: {}, timeframe: {}",
                    sessionDate,
                    stockPrice.getSessionDate(),
                    stock.getStockId(),
                    timeframe);
            return stockPrice;
        }

        // No existing record, create a new one
        return create(stock, timeframe, open, high, low, close, sessionDate);
    }

    @Override
    public StockPrice get(Long stockId, Timeframe timeframe) {
        return stockPriceRepository
                .findByStockIdAndTimeframe(stockId, timeframe)
                .orElseThrow(
                        () ->
                                new EntityNotFoundException(
                                        "StockPrice not found for stockId: "
                                                + stockId
                                                + " and timeframe: "
                                                + timeframe));
    }

    @Override
    public StockPrice get(Stock stock, Timeframe timeframe) {
        return stockPriceRepository
                .findByStockIdAndTimeframe(stock.getStockId(), timeframe)
                .orElse(null);
    }

    // Helper method to avoid redundant stock fetching logic
    private Stock getStockById(Long stockId) {
        return stockRepository
                .findById(stockId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Stock not found with id: " + stockId));
    }

    private void shiftPreviousPrice(StockPrice stockPrice) {

        stockPrice.setPrev6Open(stockPrice.getPrev5Open());
        stockPrice.setPrev6High(stockPrice.getPrev5High());
        stockPrice.setPrev6Low(stockPrice.getPrev5Low());
        stockPrice.setPrev6Close(stockPrice.getPrev5Close());

        stockPrice.setPrev5Open(stockPrice.getPrev4Open());
        stockPrice.setPrev5High(stockPrice.getPrev4High());
        stockPrice.setPrev5Low(stockPrice.getPrev4Low());
        stockPrice.setPrev5Close(stockPrice.getPrev4Close());

        stockPrice.setPrev4Open(stockPrice.getPrev3Open());
        stockPrice.setPrev4High(stockPrice.getPrev3High());
        stockPrice.setPrev4Low(stockPrice.getPrev3Low());
        stockPrice.setPrev4Close(stockPrice.getPrev3Close());

        stockPrice.setPrev3Open(stockPrice.getPrev2Open());
        stockPrice.setPrev3High(stockPrice.getPrev2High());
        stockPrice.setPrev3Low(stockPrice.getPrev2Low());
        stockPrice.setPrev3Close(stockPrice.getPrev2Close());

        stockPrice.setPrev2Open(stockPrice.getPrevOpen());
        stockPrice.setPrev2High(stockPrice.getPrevHigh());
        stockPrice.setPrev2Low(stockPrice.getPrevLow());
        stockPrice.setPrev2Close(stockPrice.getPrevClose());

        stockPrice.setPrevOpen(stockPrice.getOpen());
        stockPrice.setPrevHigh(stockPrice.getHigh());
        stockPrice.setPrevLow(stockPrice.getLow());
        stockPrice.setPrevClose(stockPrice.getClose());
    }

    private void setPivotLevels(StockPrice stockPrice){

        PivotPointUtils.PivotLevels pivotLevels = PivotPointUtils.calculate(stockPrice.getHigh(), stockPrice.getLow(), stockPrice.getClose());

        stockPrice.setPivot(pivotLevels.getPivot());

        stockPrice.setResistance1(pivotLevels.getResistance1());
        stockPrice.setResistance2(pivotLevels.getResistance2());
        stockPrice.setResistance3(pivotLevels.getResistance3());

        stockPrice.setSupport1(pivotLevels.getSupport1());
        stockPrice.setSupport2(pivotLevels.getSupport2());
        stockPrice.setSupport3(pivotLevels.getSupport3());
    }
}
