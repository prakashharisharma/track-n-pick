package com.example.transactional.repo.stocks;

import com.example.transactional.model.um.Portfolio;
import com.example.transactional.model.master.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    Optional<Portfolio> findByUserIdAndStock(Long userId, Stock stock);
}
