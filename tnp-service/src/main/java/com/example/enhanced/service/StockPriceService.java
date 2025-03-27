package com.example.enhanced.service;

import com.example.enhanced.model.stocks.StockPrice;
import com.example.enhanced.model.stocks.StockTechnicals;
import com.example.model.master.Stock;
import com.example.util.io.model.type.Timeframe;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
