package com.example.service;

import com.example.dto.OHLCV;
import com.example.storage.model.StockTechnicals;
import com.example.util.io.model.StockPriceIO;

import java.time.Instant;
import java.util.List;

public interface UpdateTechnicalsService {

    public void updateTechnicals();

    public void updateTechnicals(StockPriceIO stockPriceIO);

    public void updateTechnicalsTxn(StockTechnicals stockTechnicals, StockPriceIO stockPriceIO);

    public StockTechnicals calculate(String nseSymbol, Instant bhavDate, List<OHLCV> ohlcvList, StockTechnicals stockTechnicalsPreviousSession, long tradingDays);
    public StockTechnicals calculate(String nseSymbol, Instant bhavDate, List<OHLCV> ohlcvList);

}
