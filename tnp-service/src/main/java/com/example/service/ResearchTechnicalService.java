package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.data.transactional.entities.Trade;
import com.example.data.transactional.view.ResearchTechnicalResult;
import com.example.dto.common.TradeSetup;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface ResearchTechnicalService<T extends ResearchTechnical> {

    T entry(
            Stock stock,
            Timeframe timeframe,
            TradeSetup tradeSetup,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            LocalDate sessionDate);

    T exit(
            Stock stock,
            Timeframe timeframe,
            TradeSetup tradeSetup,
            StockPrice stockPrice,
            StockTechnicals stockTechnicals,
            LocalDate sessionDate);

    T get(Stock stock, Timeframe timeframe, Trade.Type type);

    List<ResearchTechnical> getAll(Trade.Type type);

    Page<ResearchTechnicalResult> searchHistory(
            int page,
            int size,
            Trade.Type type,
            Timeframe timeframe,
            LocalDate date,
            String sortBy,
            String direction
    );

    Page<ResearchTechnicalResult> searchCurrent(
            int page,
            int size,
            Trade.Type type,
            Timeframe timeframe,
            String sortBy,
            String direction
    );
}
