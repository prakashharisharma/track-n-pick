package com.example.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chyl.service.CylhService;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.repo.StockPriceRepository;
import com.example.repo.StockRepository;

@Transactional
@Service
public class StockService {

	@Autowired
	private StockRepository stockRepository;
	
	@Autowired
	private StockPriceRepository stockPriceRepository;
	
	@Autowired
	private CylhService cylhService;
	
	
	public Stock getStockByIsinCode(String isinCode) {
		return stockRepository.findByIsinCode(isinCode);
	}
	
	public Stock getStockByNseSymbol(String nseSymbol) {
		return stockRepository.findByNseSymbol(nseSymbol);
	}
	
	public List<Stock> getActiveStocks(){
		return stockRepository.findByActive(true);
	}
	
	public Stock save(Stock stock) {
		return stockRepository.save(stock);
	}
	
	public void updateCurrentPrice(Stock stock, double currentPrice) {
		
		StockPrice stockPrice = stock.getStockPrice();
		
		if(stockPrice != null) {
			stockPrice.setCurrentPrice(currentPrice);
		}else {
			stockPrice = new StockPrice();
			stockPrice.setCurrentPrice(currentPrice);
			stockPrice.setStock(stock);
		}
		
		stock.setStockPrice(stockPrice);
		
		stockRepository.save(stock);
	}
	
	public void updateCylhPrice(Stock stock) {
		
		StockPrice stockPrice = cylhService.getChylPrice(stock);
		
		stockPriceRepository.save(stockPrice);
	}
}
