package com.example.repo.stocks;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.stocks.StockTechnicals;

@Transactional
@Repository
public interface StockTechnicalsRepository extends JpaRepository<StockTechnicals, Long> {

}
