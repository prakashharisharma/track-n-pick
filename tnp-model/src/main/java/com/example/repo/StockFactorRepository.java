package com.example.repo;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.stocks.StockFactor;

@Transactional
@Repository
public interface StockFactorRepository extends JpaRepository<StockFactor, Long> {

}
