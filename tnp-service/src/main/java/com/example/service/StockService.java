package com.example.service;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chyl.service.CylhService;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.repo.stocks.StockPriceRepository;
import com.example.repo.master.StockRepository;

@Transactional
@Service
public class StockService {

	private static final Logger LOGGER = LoggerFactory.getLogger(StockService.class);
	
	@Autowired
	private StockRepository stockRepository;
	
	@Autowired
	private StockPriceRepository stockPriceRepository;
	
	@Autowired
	private CylhService cylhService;
	
	private static List<Stock> allstocks = null;
	
	
	public Stock getStockByIsinCode(String isinCode) {
		return stockRepository.findByIsinCode(isinCode);
	}
	
	public Stock getStockByNseSymbol(String nseSymbol) {
		return stockRepository.findByNseSymbol(nseSymbol);
	}
	
	public Stock getStockById(long stockId) {
		return stockRepository.findByStockId(stockId);
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
		
		LOGGER.info("CURRENT PRICE UPDATED :" + stock.getNseSymbol() +" : " + currentPrice);
	}
	
	public void updateCylhPrice(Stock stock) {
		
		StockPrice stockPrice = cylhService.getChylPrice(stock);
		
		stockPriceRepository.save(stockPrice);
	}
	
	public List<Stock> activeStocks(){
		
		if(allstocks == null) {
			allstocks =  stockRepository.findByActive(true);
		}
		
		return allstocks;
	}
}
