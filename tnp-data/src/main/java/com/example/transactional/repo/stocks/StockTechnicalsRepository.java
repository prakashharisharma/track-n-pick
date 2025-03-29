package com.example.transactional.repo.stocks;

import com.example.data.common.type.Timeframe;
import com.example.transactional.model.stocks.StockTechnicals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockTechnicalsRepository<T extends StockTechnicals> extends JpaRepository<T, Long> {

    // Fetch stock technicals by Stock ID and Timeframe
    @Query("SELECT st FROM #{#entityName} st WHERE st.stock.id = :stockId AND st.timeframe = :timeframe ORDER BY st.id DESC")
    Optional<T> findByStockIdAndTimeframe(@Param("stockId") Long stockId, @Param("timeframe") Timeframe timeframe);

    // Fetch stock technicals by Stock Symbol and Timeframe
    @Query("SELECT st FROM #{#entityName} st WHERE st.stock.nseSymbol = :stockSymbol AND st.timeframe = :timeframe ORDER BY st.id DESC")
    Optional<T> findByStockSymbolAndTimeframe(@Param("stockSymbol") String stockSymbol, @Param("timeframe") Timeframe timeframe);
}
