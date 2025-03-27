package com.example.repo.stocks;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.model.master.Stock;
import com.example.model.stocks.StockPriceOld;

@Transactional
@Repository
public interface StockPriceRepositoryOld extends JpaRepository<StockPriceOld, Long> {
	
	StockPriceOld findByStock(Stock stock);
}
