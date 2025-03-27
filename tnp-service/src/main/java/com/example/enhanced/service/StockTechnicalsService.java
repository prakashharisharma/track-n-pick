package com.example.enhanced.service;

import com.example.enhanced.model.stocks.StockPrice;
import com.example.enhanced.model.stocks.StockTechnicals;
import com.example.model.master.Stock;
import com.example.util.io.model.type.Timeframe;

import java.time.LocalDate;
import java.util.Optional;

public interface StockTechnicalsService<T extends StockTechnicals> {

    T create(Long stockId, Timeframe timeframe,
             Double ema5, Double ema10, Double ema20, Double ema50, Double ema100, Double ema200,
             Double sma5, Double sma10, Double sma20, Double sma50, Double sma100, Double sma200,
             Double rsi, Double macd, Double signal,
             Long obv, Long obvAvg,
             Long volume, Long volumeAvg5, Long volumeAvg10, Long volumeAvg20,
             Double adx, Double plusDi, Double minusDi, Double atr, LocalDate sessionDate);

    T create(Stock stock, Timeframe timeframe,
             Double ema5, Double ema10, Double ema20, Double ema50, Double ema100, Double ema200,
             Double sma5, Double sma10, Double sma20, Double sma50, Double sma100, Double sma200,
             Double rsi, Double macd, Double signal,
             Long obv, Long obvAvg,
             Long volume, Long volumeAvg5, Long volumeAvg10, Long volumeAvg20,
             Double adx, Double plusDi, Double minusDi, Double atr, LocalDate sessionDate);

    T update(Long stockId, Timeframe timeframe,
             Double ema5, Double ema10, Double ema20, Double ema50, Double ema100, Double ema200,
             Double sma5, Double sma10, Double sma20, Double sma50, Double sma100, Double sma200,
             Double rsi, Double macd, Double signal,
             Long obv, Long obvAvg,
             Long volume, Long volumeAvg5, Long volumeAvg10, Long volumeAvg20,
             Double adx, Double plusDi, Double minusDi, Double atr, LocalDate sessionDate);

    T update(Stock stock, Timeframe timeframe,
             Double ema5, Double ema10, Double ema20, Double ema50, Double ema100, Double ema200,
             Double sma5, Double sma10, Double sma20, Double sma50, Double sma100, Double sma200,
             Double rsi, Double macd, Double signal,
             Long obv, Long obvAvg,
             Long volume, Long volumeAvg5, Long volumeAvg10, Long volumeAvg20,
             Double adx, Double plusDi, Double minusDi, Double atr, LocalDate sessionDate);

    T createOrUpdate(Long stockId, Timeframe timeframe,
             Double ema5, Double ema10, Double ema20, Double ema50, Double ema100, Double ema200,
                     Double sma5, Double sma10, Double sma20, Double sma50, Double sma100, Double sma200,
             Double rsi, Double macd, Double signal,
             Long obv, Long obvAvg,
             Long volume, Long volumeAvg5, Long volumeAvg10, Long volumeAvg20,
             Double adx, Double plusDi, Double minusDi, Double atr, LocalDate sessionDate);

    T createOrUpdate(Stock stock, Timeframe timeframe,
             Double ema5, Double ema10, Double ema20, Double ema50, Double ema100, Double ema200,
                     Double sma5, Double sma10, Double sma20, Double sma50, Double sma100, Double sma200,
             Double rsi, Double macd, Double signal,
             Long obv, Long obvAvg,
             Long volume, Long volumeAvg5, Long volumeAvg10, Long volumeAvg20,
             Double adx, Double plusDi, Double minusDi, Double atr, LocalDate sessionDate);

    T get(Long stockId, Timeframe timeframe);

    T get(Stock stock, Timeframe timeframe);
}
