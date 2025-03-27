package com.example.enhanced.repo.stocks;

import com.example.enhanced.model.um.Portfolio;
import com.example.model.master.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Optional<Portfolio> findByUserIdAndStock(Long userId, Stock stock);
}
