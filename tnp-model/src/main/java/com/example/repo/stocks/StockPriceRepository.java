package com.example.repo.stocks;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;

@Transactional
@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {
	
	StockPrice findByStock(Stock stock);
}
