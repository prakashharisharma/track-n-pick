package com.example.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.master.Stock;
import com.example.repo.StockRepository;

@Transactional
@Service
public class StockService {

	@Autowired
	private StockRepository stockRepository;
	
	public Stock getStockByIsinCode(String isinCode) {
		return stockRepository.findByIsinCode(isinCode);
	}
	
	public Stock getStockByNseSymbol(String nseSymbol) {
		return stockRepository.findByNseSymbol(nseSymbol);
	}
	
	public List<Stock> getActiveStocks(){
		return stockRepository.findAll();
	}
	
	public Stock save(Stock stock) {
		return stockRepository.save(stock);
	}
}
