package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.storage.documents.StockTechnicals;
import com.example.data.transactional.entities.Stock;
import com.example.dto.common.OHLCV;
import com.example.dto.io.StockPriceIO;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface UpdateTechnicalsService {

    public void updateTechnicals(Timeframe timeframe, Stock stock, LocalDate onDate);

    public void updateTechnicals(Stock stock, StockPriceIO stockPriceIO);

    public void updateTechnicals(Timeframe timeframe, Stock stock, StockPriceIO stockPriceIO);

    public StockTechnicals calculate(
            String nseSymbol,
            Instant bhavDate,
            List<OHLCV> ohlcvList,
            StockTechnicals stockTechnicalsPreviousSession,
            long tradingDays);

    public StockTechnicals calculate(String nseSymbol, Instant bhavDate, List<OHLCV> ohlcvList);

    public void updateTechnicals(Timeframe timeframe, Stock stock, StockTechnicals stockTechnicals);

    public StockTechnicals build(Timeframe timeframe, Stock stock, LocalDate sessionDate);
}
