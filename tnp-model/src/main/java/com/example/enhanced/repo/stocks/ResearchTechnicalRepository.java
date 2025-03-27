package com.example.enhanced.repo.stocks;

import com.example.enhanced.model.research.ResearchTechnical;
import com.example.enhanced.model.stocks.StockTechnicals;
import com.example.enhanced.model.um.Trade;
import com.example.util.io.model.type.Timeframe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResearchTechnicalRepository<T extends ResearchTechnical> extends JpaRepository<T, Long> {

    // Fetch stock technicals by Stock ID and Timeframe
    @Query("SELECT rt FROM #{#entityName} rt WHERE rt.stock.id = :stockId AND rt.timeframe = :timeframe AND rt.type = :type ORDER BY rt.id DESC")
    Optional<T> findByStockIdAndTimeframeAndType(@Param("stockId") Long stockId, @Param("timeframe") Timeframe timeframe, @Param("type") Trade.Type type);

    @Query("SELECT rt FROM #{#entityName} rt WHERE rt.type = :type ORDER BY rt.id DESC")
    List<T> findAllByType(@Param("type") Trade.Type type);

    // Fetch stock technicals by Stock Symbol and Timeframe
    @Query("SELECT rt FROM #{#entityName} rt WHERE rt.stock.nseSymbol = :stockSymbol AND rt.timeframe = :timeframe ORDER BY rt.id DESC")
    Optional<T> findByStockSymbolAndTimeframe(@Param("stockSymbol") String stockSymbol, @Param("timeframe") Timeframe timeframe);

}
