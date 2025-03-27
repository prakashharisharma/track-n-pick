package com.example.enhanced.service;

import com.example.dto.TradeSetup;
import com.example.enhanced.model.research.ResearchTechnical;
import com.example.enhanced.model.stocks.StockPrice;
import com.example.enhanced.model.stocks.StockTechnicals;
import com.example.enhanced.model.um.Trade;
import com.example.model.master.Stock;
import com.example.util.io.model.type.Timeframe;

import java.time.LocalDate;
import java.util.List;

public interface ResearchTechnicalService<T extends ResearchTechnical> {

    T entry(Stock stock, Timeframe timeframe, TradeSetup tradeSetup, StockPrice stockPrice, StockTechnicals stockTechnicals, LocalDate sessionDate);
    T exit(Stock stock, Timeframe timeframe, TradeSetup tradeSetup, StockPrice stockPrice, StockTechnicals stockTechnicals, LocalDate sessionDate);
    T get(Stock stock, Timeframe timeframe, Trade.Type type);

    List<ResearchTechnical> getAll(Trade.Type type);
}
