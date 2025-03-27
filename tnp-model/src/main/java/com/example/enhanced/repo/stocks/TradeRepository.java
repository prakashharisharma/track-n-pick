package com.example.enhanced.repo.stocks;

import com.example.enhanced.model.um.Trade;
import com.example.model.master.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByUserIdAndStockAndTypeOrderByTimestampAsc(Long userId, Stock stock, Trade.Type type);
}
