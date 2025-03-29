package com.example.transactional.service;

import com.example.dto.TradeSetup;
import com.example.transactional.model.research.ResearchTechnical;
import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.model.stocks.StockTechnicals;
import com.example.transactional.model.um.Trade;
import com.example.transactional.model.master.Stock;
import com.example.util.io.model.type.Timeframe;

import java.time.LocalDate;
import java.util.List;

public interface ResearchTechnicalService<T extends ResearchTechnical> {

    T entry(Stock stock, Timeframe timeframe, TradeSetup tradeSetup, StockPrice stockPrice, StockTechnicals stockTechnicals, LocalDate sessionDate);
    T exit(Stock stock, Timeframe timeframe, TradeSetup tradeSetup, StockPrice stockPrice, StockTechnicals stockTechnicals, LocalDate sessionDate);
    T get(Stock stock, Timeframe timeframe, Trade.Type type);

    List<ResearchTechnical> getAll(Trade.Type type);
}
