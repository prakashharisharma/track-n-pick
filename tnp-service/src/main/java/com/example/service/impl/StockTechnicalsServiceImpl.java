package com.example.service.impl;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.data.transactional.entities.StockTechnicalsDaily;
import com.example.data.transactional.entities.StockTechnicalsMonthly;
import com.example.data.transactional.entities.StockTechnicalsQuarterly;
import com.example.data.transactional.entities.StockTechnicalsWeekly;
import com.example.data.transactional.entities.StockTechnicalsYearly;
import com.example.data.transactional.repo.StockRepository;
import com.example.data.transactional.repo.StockTechnicalsRepository;
import com.example.service.StockTechnicalsService;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockTechnicalsServiceImpl implements StockTechnicalsService {

    private final StockTechnicalsRepository<StockTechnicals> stockTechnicalsRepository;
    private final StockRepository stockRepository;

    // Map to create instances dynamically based on timeframe
    private static final Map<Timeframe, Supplier<StockTechnicals>> STOCK_TECHNICALS_CREATORS =
            Map.of(
                    Timeframe.DAILY, StockTechnicalsDaily::new,
                    Timeframe.WEEKLY, StockTechnicalsWeekly::new,
                    Timeframe.MONTHLY, StockTechnicalsMonthly::new,
                    Timeframe.QUARTERLY, StockTechnicalsQuarterly::new,
                    Timeframe.YEARLY, StockTechnicalsYearly::new);

    @Override
    public StockTechnicals create(
            Long stockId,
            Timeframe timeframe,
            Double ema5,
            Double ema10,
            Double ema20,
            Double ema50,
            Double ema100,
            Double ema200,
            Double sma5,
            Double sma10,
            Double sma20,
            Double sma50,
            Double sma100,
            Double sma200,
            Double rsi,
            Double macd,
            Double signal,
            Long obv,
            Long obvAvg,
            Long volume,
            Long volumeAvg5,
            Long volumeAvg10,
            Long volumeAvg20,
            Double adx,
            Double plusDi,
            Double minusDi,
            Double atr,
            LocalDate sessionDate) {
        Stock stock = getStockById(stockId);
        return this.create(
                stock,
                timeframe,
                ema5,
                ema10,
                ema20,
                ema50,
                ema100,
                ema200,
                sma5,
                sma10,
                sma20,
                sma50,
                sma100,
                sma200,
                rsi,
                macd,
                signal,
                obv,
                obvAvg,
                volume,
                volumeAvg5,
                volumeAvg10,
                volumeAvg20,
                adx,
                plusDi,
                minusDi,
                atr,
                sessionDate);
    }

    @Override
    public StockTechnicals create(
            Stock stock,
            Timeframe timeframe,
            Double ema5,
            Double ema10,
            Double ema20,
            Double ema50,
            Double ema100,
            Double ema200,
            Double sma5,
            Double sma10,
            Double sma20,
            Double sma50,
            Double sma100,
            Double sma200,
            Double rsi,
            Double macd,
            Double signal,
            Long obv,
            Long obvAvg,
            Long volume,
            Long volumeAvg5,
            Long volumeAvg10,
            Long volumeAvg20,
            Double adx,
            Double plusDi,
            Double minusDi,
            Double atr,
            LocalDate sessionDate) {
        Optional<StockTechnicals> existingStockTechnicals =
                stockTechnicalsRepository.findByStockIdAndTimeframe(stock.getStockId(), timeframe);

        // If a record with the same sessionDate exists, do nothing (return existing record)
        if (existingStockTechnicals.isPresent()) {
            log.info(
                    "Skipping creation. A StockTechnicals record already exists for stockId: {},"
                            + " timeframe: {}",
                    stock.getStockId(),
                    timeframe);
            return existingStockTechnicals.get();
        }

        StockTechnicals newStockTechnicals =
                STOCK_TECHNICALS_CREATORS
                        .getOrDefault(
                                timeframe,
                                () -> {
                                    throw new IllegalArgumentException(
                                            "Unsupported timeframe: " + timeframe);
                                })
                        .get();

        newStockTechnicals.setStock(stock);
        newStockTechnicals.setTimeframe(timeframe);

        // Set all the technical indicators
        newStockTechnicals.setEma5(ema5);
        newStockTechnicals.setEma10(ema10);
        newStockTechnicals.setEma20(ema20);
        newStockTechnicals.setEma50(ema50);
        newStockTechnicals.setEma100(ema100);
        newStockTechnicals.setEma200(ema200);

        newStockTechnicals.setSma5(sma5);
        newStockTechnicals.setSma10(sma10);
        newStockTechnicals.setSma20(sma20);
        newStockTechnicals.setSma50(sma50);
        newStockTechnicals.setSma100(sma100);
        newStockTechnicals.setSma200(sma200);

        newStockTechnicals.setRsi(rsi);
        newStockTechnicals.setMacd(macd);
        newStockTechnicals.setSignal(signal);

        newStockTechnicals.setObv(obv);
        newStockTechnicals.setObvAvg(obvAvg);

        newStockTechnicals.setVolume(volume);
        newStockTechnicals.setVolumeAvg5(volumeAvg5);
        newStockTechnicals.setVolumeAvg10(volumeAvg10);
        newStockTechnicals.setVolumeAvg20(volumeAvg20);

        newStockTechnicals.setAdx(adx);
        newStockTechnicals.setPlusDi(plusDi);
        newStockTechnicals.setMinusDi(minusDi);
        newStockTechnicals.setAtr(atr);

        newStockTechnicals.setSessionDate(sessionDate);

        log.info("Creating new {} StockTechnicals for stockId: {}", timeframe, stock.getStockId());
        return stockTechnicalsRepository.save(newStockTechnicals);
    }

    @Override
    public StockTechnicals update(
            Long stockId,
            Timeframe timeframe,
            Double ema5,
            Double ema10,
            Double ema20,
            Double ema50,
            Double ema100,
            Double ema200,
            Double sma5,
            Double sma10,
            Double sma20,
            Double sma50,
            Double sma100,
            Double sma200,
            Double rsi,
            Double macd,
            Double signal,
            Long obv,
            Long obvAvg,
            Long volume,
            Long volumeAvg5,
            Long volumeAvg10,
            Long volumeAvg20,
            Double adx,
            Double plusDi,
            Double minusDi,
            Double atr,
            LocalDate sessionDate) {
        Stock stock = getStockById(stockId);
        return this.update(
                stock,
                timeframe,
                ema5,
                ema10,
                ema20,
                ema50,
                ema100,
                ema200,
                sma5,
                sma10,
                sma20,
                sma50,
                sma100,
                sma200,
                rsi,
                macd,
                signal,
                obv,
                obvAvg,
                volume,
                volumeAvg5,
                volumeAvg10,
                volumeAvg20,
                adx,
                plusDi,
                minusDi,
                atr,
                sessionDate);
    }

    @Override
    public StockTechnicals update(
            Stock stock,
            Timeframe timeframe,
            Double ema5,
            Double ema10,
            Double ema20,
            Double ema50,
            Double ema100,
            Double ema200,
            Double sma5,
            Double sma10,
            Double sma20,
            Double sma50,
            Double sma100,
            Double sma200,
            Double rsi,
            Double macd,
            Double signal,
            Long obv,
            Long obvAvg,
            Long volume,
            Long volumeAvg5,
            Long volumeAvg10,
            Long volumeAvg20,
            Double adx,
            Double plusDi,
            Double minusDi,
            Double atr,
            LocalDate sessionDate) {
        StockTechnicals stockTechnicals =
                stockTechnicalsRepository
                        .findByStockIdAndTimeframe(stock.getStockId(), timeframe)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "StockTechnicals not found for stockId: "
                                                        + stock.getStockId()
                                                        + " and timeframe: "
                                                        + timeframe));

        // If sessionDate is the same or earlier, do nothing
        if (!sessionDate.isAfter(stockTechnicals.getSessionDate())) {
            log.info(
                    "Skipping update. Provided sessionDate: {} is not after existing sessionDate:"
                            + " {} for stockId: {}, timeframe: {}",
                    sessionDate,
                    stockTechnicals.getSessionDate(),
                    stock.getStockId(),
                    timeframe);
            return stockTechnicals; // Return existing record without modification
        }

        this.shiftPreviousTechnicals(stockTechnicals);

        stockTechnicals.setEma5(ema5 != null ? ema5 : 0.0);
        stockTechnicals.setEma10(ema10 != null ? ema10 : 0.0);
        stockTechnicals.setEma20(ema20 != null ? ema20 : 0.0);
        stockTechnicals.setEma50(ema50 != null ? ema50 : 0.0);
        stockTechnicals.setEma100(ema100 != null ? ema100 : 0.0);
        stockTechnicals.setEma200(ema200 != null ? ema200 : 0.0);

        stockTechnicals.setSma5(sma5 != null ? sma5 : 0.0);
        stockTechnicals.setSma10(sma10 != null ? sma10 : 0.0);
        stockTechnicals.setSma20(sma20 != null ? sma20 : 0.0);
        stockTechnicals.setSma50(sma50 != null ? sma50 : 0.0);
        stockTechnicals.setSma100(sma100 != null ? sma100 : 0.0);
        stockTechnicals.setSma200(sma200 != null ? sma200 : 0.0);

        stockTechnicals.setRsi(rsi != null ? rsi : 0.0);
        stockTechnicals.setMacd(macd != null ? macd : 0.0);
        stockTechnicals.setSignal(signal != null ? signal : 0.0);

        stockTechnicals.setObv(obv != null ? obv : 0);
        stockTechnicals.setObvAvg(obvAvg != null ? obvAvg : 0);

        stockTechnicals.setVolume(volume != null ? volume : 0);
        stockTechnicals.setVolumeAvg5(volumeAvg5 != null ? volumeAvg5 : 0);
        stockTechnicals.setVolumeAvg10(volumeAvg10 != null ? volumeAvg10 : 0);
        stockTechnicals.setVolumeAvg20(volumeAvg20 != null ? volumeAvg20 : 0);

        stockTechnicals.setAdx(adx != null ? adx : 0.0);
        stockTechnicals.setPlusDi(plusDi != null ? plusDi : 0.0);
        stockTechnicals.setMinusDi(minusDi != null ? minusDi : 0.0);
        stockTechnicals.setAtr(atr != null ? atr : 0.0);
        stockTechnicals.setSessionDate(sessionDate);

        log.info("Updating {} StockTechnicals for stockId: {}", timeframe, stock.getStockId());
        return stockTechnicalsRepository.save(stockTechnicals);
    }

    @Override
    public StockTechnicals createOrUpdate(
            Long stockId,
            Timeframe timeframe,
            Double ema5,
            Double ema10,
            Double ema20,
            Double ema50,
            Double ema100,
            Double ema200,
            Double sma5,
            Double sma10,
            Double sma20,
            Double sma50,
            Double sma100,
            Double sma200,
            Double rsi,
            Double macd,
            Double signal,
            Long obv,
            Long obvAvg,
            Long volume,
            Long volumeAvg5,
            Long volumeAvg10,
            Long volumeAvg20,
            Double adx,
            Double plusDi,
            Double minusDi,
            Double atr,
            LocalDate sessionDate) {
        StockTechnicals existingStockTechnicals = get(stockId, timeframe);

        if (existingStockTechnicals != null) {
            StockTechnicals stockTechnicals = existingStockTechnicals;

            // If sessionDate is the same, do nothing
            if (sessionDate.equals(stockTechnicals.getSessionDate())) {
                log.info(
                        "stockTechnicals already exists for stockId: {}, timeframe: {},"
                                + " sessionDate: {}. No action taken.",
                        stockId,
                        timeframe,
                        sessionDate);
                return stockTechnicals;
            }

            // If sessionDate is after, update it
            if (sessionDate.isAfter(stockTechnicals.getSessionDate())) {
                return update(
                        stockId,
                        timeframe,
                        ema5,
                        ema10,
                        ema20,
                        ema50,
                        ema100,
                        ema200,
                        sma5,
                        sma10,
                        sma20,
                        sma50,
                        sma100,
                        sma200,
                        rsi,
                        macd,
                        signal,
                        obv,
                        obvAvg,
                        volume,
                        volumeAvg5,
                        volumeAvg10,
                        volumeAvg20,
                        adx,
                        plusDi,
                        minusDi,
                        atr,
                        sessionDate);
            }

            // If sessionDate is before, do nothing
            log.info(
                    "Skipping update. Provided sessionDate: {} is before existing sessionDate: {}"
                            + " for stockId: {}, timeframe: {}",
                    sessionDate,
                    stockTechnicals.getSessionDate(),
                    stockId,
                    timeframe);
            return stockTechnicals;
        }

        // No existing record, create a new one
        return create(
                stockId,
                timeframe,
                ema5,
                ema10,
                ema20,
                ema50,
                ema100,
                ema200,
                sma5,
                sma10,
                sma20,
                sma50,
                sma100,
                sma200,
                rsi,
                macd,
                signal,
                obv,
                obvAvg,
                volume,
                volumeAvg5,
                volumeAvg10,
                volumeAvg20,
                adx,
                plusDi,
                minusDi,
                atr,
                sessionDate);
    }

    @Override
    public StockTechnicals createOrUpdate(
            Stock stock,
            Timeframe timeframe,
            Double ema5,
            Double ema10,
            Double ema20,
            Double ema50,
            Double ema100,
            Double ema200,
            Double sma5,
            Double sma10,
            Double sma20,
            Double sma50,
            Double sma100,
            Double sma200,
            Double rsi,
            Double macd,
            Double signal,
            Long obv,
            Long obvAvg,
            Long volume,
            Long volumeAvg5,
            Long volumeAvg10,
            Long volumeAvg20,
            Double adx,
            Double plusDi,
            Double minusDi,
            Double atr,
            LocalDate sessionDate) {
        StockTechnicals existingStockTechnicals = get(stock, timeframe);

        if (existingStockTechnicals != null) {
            StockTechnicals stockTechnicals = existingStockTechnicals;

            // If sessionDate is the same, do nothing
            if (sessionDate.equals(stockTechnicals.getSessionDate())) {
                log.info(
                        "StockPrice already exists for stockId: {}, timeframe: {}, sessionDate: {}."
                                + " No action taken.",
                        stock.getStockId(),
                        timeframe,
                        sessionDate);
                return stockTechnicals;
            }

            // If sessionDate is after, update it
            if (sessionDate.isAfter(stockTechnicals.getSessionDate())) {
                return update(
                        stock,
                        timeframe,
                        ema5,
                        ema10,
                        ema20,
                        ema50,
                        ema100,
                        ema200,
                        sma5,
                        sma10,
                        sma20,
                        sma50,
                        sma100,
                        sma200,
                        rsi,
                        macd,
                        signal,
                        obv,
                        obvAvg,
                        volume,
                        volumeAvg5,
                        volumeAvg10,
                        volumeAvg20,
                        adx,
                        plusDi,
                        minusDi,
                        atr,
                        sessionDate);
            }

            // If sessionDate is before, do nothing
            log.info(
                    "Skipping update. Provided sessionDate: {} is before existing sessionDate: {}"
                            + " for stockId: {}, timeframe: {}",
                    sessionDate,
                    stockTechnicals.getSessionDate(),
                    stock.getStockId(),
                    timeframe);
            return stockTechnicals;
        }

        // No existing record, create a new one
        return create(
                stock,
                timeframe,
                ema5,
                ema10,
                ema20,
                ema50,
                ema100,
                ema200,
                sma5,
                sma10,
                sma20,
                sma50,
                sma100,
                sma200,
                rsi,
                macd,
                signal,
                obv,
                obvAvg,
                volume,
                volumeAvg5,
                volumeAvg10,
                volumeAvg20,
                adx,
                plusDi,
                minusDi,
                atr,
                sessionDate);
    }

    @Override
    public StockTechnicals get(Long stockId, Timeframe timeframe) {
        return stockTechnicalsRepository
                .findByStockIdAndTimeframe(stockId, timeframe)
                .orElseThrow(
                        () ->
                                new EntityNotFoundException(
                                        "StockTechnicals not found for stockId: "
                                                + stockId
                                                + " and timeframe: "
                                                + timeframe));
    }

    @Override
    public StockTechnicals get(Stock stock, Timeframe timeframe) {
        return stockTechnicalsRepository
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

    private void shiftPreviousTechnicals(StockTechnicals stockTechnicals) {
        stockTechnicals.setPrev2Sma5(stockTechnicals.getPrevSma5());
        stockTechnicals.setPrev2Sma10(stockTechnicals.getPrevSma10());
        stockTechnicals.setPrev2Sma20(stockTechnicals.getPrevSma20());
        stockTechnicals.setPrev2Sma50(stockTechnicals.getPrevSma50());
        stockTechnicals.setPrev2Sma100(stockTechnicals.getPrevSma100());
        stockTechnicals.setPrev2Sma200(stockTechnicals.getPrevSma200());
        stockTechnicals.setPrev2Ema5(stockTechnicals.getPrevEma5());
        stockTechnicals.setPrev2Ema10(stockTechnicals.getPrevEma10());
        stockTechnicals.setPrev2Ema20(stockTechnicals.getPrevEma20());
        stockTechnicals.setPrev2Ema50(stockTechnicals.getPrevEma50());
        stockTechnicals.setPrev2Ema100(stockTechnicals.getPrevEma100());
        stockTechnicals.setPrev2Ema200(stockTechnicals.getPrevEma200());
        stockTechnicals.setPrev2Rsi(stockTechnicals.getPrevRsi());
        stockTechnicals.setPrev2Macd(stockTechnicals.getPrevMacd());
        stockTechnicals.setPrev2Signal(stockTechnicals.getPrevSignal());
        stockTechnicals.setPrev2Obv(stockTechnicals.getPrevObv());
        stockTechnicals.setPrev2ObvAvg(stockTechnicals.getPrevObvAvg());
        stockTechnicals.setPrev2Volume(stockTechnicals.getPrevVolume());
        stockTechnicals.setPrev2VolumeAvg5(stockTechnicals.getPrevVolumeAvg5());
        stockTechnicals.setPrev2VolumeAvg10(stockTechnicals.getPrevVolumeAvg10());
        stockTechnicals.setPrev2VolumeAvg20(stockTechnicals.getPrevVolumeAvg20());
        stockTechnicals.setPrev2Adx(stockTechnicals.getPrevAdx());
        stockTechnicals.setPrev2PlusDi(stockTechnicals.getPrevPlusDi());
        stockTechnicals.setPrev2MinusDi(stockTechnicals.getPrevMinusDi());
        stockTechnicals.setPrev2Atr(stockTechnicals.getPrevAtr());

        stockTechnicals.setPrevSma5(stockTechnicals.getSma5());
        stockTechnicals.setPrevSma10(stockTechnicals.getSma10());
        stockTechnicals.setPrevSma20(stockTechnicals.getSma20());
        stockTechnicals.setPrevSma50(stockTechnicals.getSma50());
        stockTechnicals.setPrevSma100(stockTechnicals.getSma100());
        stockTechnicals.setPrevSma200(stockTechnicals.getSma200());
        stockTechnicals.setPrevEma5(stockTechnicals.getEma5());
        stockTechnicals.setPrevEma10(stockTechnicals.getEma10());
        stockTechnicals.setPrevEma20(stockTechnicals.getEma20());
        stockTechnicals.setPrevEma50(stockTechnicals.getEma50());
        stockTechnicals.setPrevEma100(stockTechnicals.getEma100());
        stockTechnicals.setPrevEma200(stockTechnicals.getEma200());
        stockTechnicals.setPrevRsi(stockTechnicals.getRsi());
        stockTechnicals.setPrevMacd(stockTechnicals.getMacd());
        stockTechnicals.setPrevSignal(stockTechnicals.getSignal());
        stockTechnicals.setPrevObv(stockTechnicals.getObv());
        stockTechnicals.setPrevObvAvg(stockTechnicals.getObvAvg());
        stockTechnicals.setPrevVolume(stockTechnicals.getVolume());
        stockTechnicals.setPrevVolumeAvg5(stockTechnicals.getVolumeAvg5());
        stockTechnicals.setPrevVolumeAvg10(stockTechnicals.getVolumeAvg10());
        stockTechnicals.setPrevVolumeAvg20(stockTechnicals.getVolumeAvg20());
        stockTechnicals.setPrevAdx(stockTechnicals.getAdx());
        stockTechnicals.setPrevPlusDi(stockTechnicals.getPlusDi());
        stockTechnicals.setPrevMinusDi(stockTechnicals.getMinusDi());
        stockTechnicals.setPrevAtr(stockTechnicals.getAtr());
    }
}
