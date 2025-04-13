package com.example.service;

import java.math.BigDecimal;

public interface PortfolioService {

    void buyStock(Long userId, Long stockId, long quantity, BigDecimal price);

    void sellStock(Long userId, Long stockId, long quantity, BigDecimal price);
}
