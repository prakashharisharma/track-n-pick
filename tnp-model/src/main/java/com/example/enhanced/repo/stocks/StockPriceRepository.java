package com.example.enhanced.repo.stocks;


import com.example.enhanced.model.stocks.StockPrice;
import com.example.util.io.model.type.Timeframe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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