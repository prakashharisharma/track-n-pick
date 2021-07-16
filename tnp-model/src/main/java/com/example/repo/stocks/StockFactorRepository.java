package com.example.repo.stocks;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.stocks.StockFactor;

@Transactional
@Repository
public interface StockFactorRepository extends JpaRepository<StockFactor, Long> {

	public List<StockFactor> findTop100AllByOrderByMarketCapDesc();
	
	public List<StockFactor> findTop250AllByOrderByMarketCapDesc();
	
	public List<StockFactor> findAllByOrderByLastModifiedAsc();
	
}
