package com.example.transactional.repo.stocks;


import com.example.data.common.type.Timeframe;
import com.example.transactional.model.stocks.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockPriceRepository<T extends StockPrice> extends JpaRepository<T, Long> {

    // Fetch stock price by Stock ID and Timeframe
    @Query("SELECT sp FROM #{#entityName} sp WHERE sp.stock.id = :stockId AND sp.timeframe = :timeframe ORDER BY sp.id DESC")
    Optional<T> findByStockIdAndTimeframe(@Param("stockId") Long stockId, @Param("timeframe") Timeframe timeframe);

    // Fetch stock price by Stock Symbol and Timeframe
    @Query("SELECT sp FROM #{#entityName} sp WHERE sp.stock.nseSymbol = :stockSymbol AND sp.timeframe = :timeframe ORDER BY sp.id DESC")
    Optional<T> findByStockSymbolAndTimeframe(@Param("stockSymbol") String stockSymbol, @Param("timeframe") Timeframe timeframe);
}