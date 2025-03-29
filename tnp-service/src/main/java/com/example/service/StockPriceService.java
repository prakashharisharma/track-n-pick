package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.model.master.Stock;

import java.time.LocalDate;

public interface StockPriceService<T extends StockPrice> {
    T create(Long stockId, Timeframe timeframe, Double open, Double high, Double low, Double close, LocalDate sessionDate);
    T create(Stock stock, Timeframe timeframe, Double open, Double high, Double low, Double close, LocalDate sessionDate);
    T update(Long stockId, Timeframe timeframe, Double open, Double high, Double low, Double close, LocalDate sessionDate);
    T update(Stock stock, Timeframe timeframe, Double open, Double high, Double low, Double close, LocalDate sessionDate);
    // Create a new StockPrice if it doesn't exist, otherwise update the existing one
    T createOrUpdate(Long stockId, Timeframe timeframe, Double open, Double high, Double low, Double close, LocalDate sessionDate);
    T createOrUpdate(Stock stock, Timeframe timeframe, Double open, Double high, Double low, Double close, LocalDate sessionDate);
    T get(Long stockId, Timeframe timeframe);
    T get(Stock stock, Timeframe timeframe);
}
