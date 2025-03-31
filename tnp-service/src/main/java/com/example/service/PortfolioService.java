package com.example.service;



import com.example.data.transactional.entities.Stock;

import java.math.BigDecimal;

public interface PortfolioService {

    public void buyStock(Long userId, Stock stock, long quantity, BigDecimal price);
    public void sellStock(Long userId, Stock stock, long quantity, BigDecimal price);

}
