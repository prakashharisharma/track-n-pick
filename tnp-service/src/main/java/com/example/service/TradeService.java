package com.example.service;

import com.example.data.transactional.entities.Trade;
import com.example.data.transactional.view.TradeResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.domain.Page;

public interface TradeService {
    public BigDecimal getTotalRealizedPnl(Long userId);

    public Page<TradeResult> getUserTrades(
            Long userId,
            int page,
            int size,
            String q,
            Trade.Type type,
            LocalDate from,
            LocalDate to,
            String sortBy,
            String direction);
}
