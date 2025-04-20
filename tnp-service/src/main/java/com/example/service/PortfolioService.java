package com.example.service;

import com.example.data.transactional.view.PortfolioResult;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;

public interface PortfolioService {

    public BigDecimal getTotalInvestmentValue(Long userId);

    public void buyStock(Long userId, Long stockId, long quantity, BigDecimal price);

    public void sellStock(Long userId, Long stockId, long quantity, BigDecimal price);

    public Page<PortfolioResult> get(
            Long userId, int page, int size, String sortBy, String direction);

    public PortfolioResult stats(Long userId);
}
