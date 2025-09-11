package com.example.service;

import com.example.data.common.type.Timeframe;
import com.example.data.transactional.entities.ResearchTechnical;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockPrice;
import com.example.data.transactional.entities.StockTechnicals;
import com.example.data.transactional.entities.Trade;
import com.example.data.transactional.view.ResearchTechnicalResult;
import com.example.dto.common.TradeSetup;
import com.example.dto.response.ResearchTechnicalDetailsCurrentResponse;
import com.example.dto.response.ResearchTechnicalDetailsHistoryResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface ResearchTechnicalService<T extends ResearchTechnical> {

    public static final double MAX_RISK = 6.0;
    public static final double MIN_RISK = 2.0;

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

    public List<ResearchTechnical> getLatestBuyResearch(LocalDate sessionDate);

    public List<ResearchTechnical> getAllResearchWithinAYear(LocalDate sessionDate);

    public List<ResearchTechnical> getLatestInvestmentBuyResearch(LocalDate sessionDate);

    public List<ResearchTechnical> getLatestCandleStickBuyResearch(LocalDate sessionDate);

    public List<ResearchTechnical> getRecentHybridBuyResearch(LocalDate sessionDate);

    public List<ResearchTechnical> getRecentDynamicBuyResearch(LocalDate sessionDate);

    public List<ResearchTechnical> getRecentBasicBuyResearch(LocalDate sessionDate);

    public List<ResearchTechnical> getLatestSellResearch(LocalDate sessionDate);

    Page<ResearchTechnicalResult> searchHistory(
            int page,
            int size,
            Trade.Type type,
            Timeframe timeframe,
            String sortBy,
            String direction);

    Page<ResearchTechnicalResult> searchCurrent(
            int page,
            int size,
            Trade.Type type,
            Timeframe timeframe,
            String sortBy,
            String direction);

    ResearchTechnicalDetailsCurrentResponse getCurrentDetails(
            Long userId, Long reserachTechnicalId);

    ResearchTechnicalDetailsHistoryResponse getHistoryDetails(Long reserachTechnicalId);

    public void updateScore(ResearchTechnical researchTechnical);

    public Optional<ResearchTechnical> getLatest(Stock stock);

    public double getTickSize(Stock stock);
}
