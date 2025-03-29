package com.example.transactional.repo.stocks;

import com.example.transactional.model.um.Trade;
import com.example.transactional.model.master.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByUserIdAndStockAndTypeOrderByTimestampAsc(Long userId, Stock stock, Trade.Type type);
}
