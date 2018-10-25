package com.example.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chyl.service.CylhService;
import com.example.dylh.service.DylhService;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;

@Transactional
@Service
public class YearLowStocksService {

	private static final Logger LOGGER = LoggerFactory.getLogger(YearLowStocksService.class);
	
	@Autowired
	private DylhService dylhService;
	
	@Autowired
	private StockService stockService;
	
	@Autowired
	private CylhService cylhService;
	
	public List<Stock> yearLowStocks() {
		
		List<Stock> masterStocksList = stockService.getActiveStocks();
		
		List<Stock> todaysYearLowStocks = dylhService.yearLowStocks();
		
		todaysYearLowStocks.retainAll(masterStocksList);
		
		List<Stock> todaysYearLowStocksFinalList = new ArrayList<>();
		
		for(Stock stock : todaysYearLowStocks) {
			
			todaysYearLowStocksFinalList.add(stockService.getStockByNseSymbol(stock.getNseSymbol()));
		}
		
		return applyFilterRule(todaysYearLowStocksFinalList);
	}
	
	private List<Stock> applyFilterRule(List<Stock> inputStockList){
		
		List<Stock> tobeFilter = inputStockList.stream().filter( (s) -> s.getStockFactor().getMarketCap() > 2000.0 && s.getStockFactor().getDebtEquity() < 0.50 && s.getStockFactor().getDividend() > 0.00 && s.getStockFactor().getReturnOnEquity() > 10.0 && s.getStockFactor().getReturnOnCapital() > 15.0 ).collect(Collectors.toList());
		
		List<Stock> resultStocks = new ArrayList<>();
		
		for(Stock stock  : tobeFilter) {

				StockPrice stockPrice = cylhService.getChylPrice(stockService.getStockByNseSymbol(stock.getNseSymbol()));

				 if(stockPrice != null && stockPrice.getCurrentPrice() < 500.00) {
					 
					 LOGGER.info(stock +" : " + stockPrice);
					 
					 double currentPrice = stockPrice.getCurrentPrice();
					 
					 double bookValue = stock.getStockFactor().getBookValue();
					 
					 double eps = stock.getStockFactor().getEps();
					 
					 double pe = currentPrice / eps;
					 
					 double pb = currentPrice / bookValue;
					 
					 if(pb < 5.0 && pe < 20.0) {
						 
						 resultStocks.add(stock);
						 
						 stockPrice.setStock(stock);
						 
						 stock.setStockPrice(stockPrice);
						 
						 stock = stockService.save(stock);
						 
					 }
					 
				 }
		}
		
		return resultStocks;
	}
	
}
