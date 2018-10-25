package com.example.service;

import java.time.LocalDate;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chyl.service.CylhService;
import com.example.factor.FactorRediff;
import com.example.model.master.Stock;
import com.example.model.stocks.StockFactor;
import com.example.model.stocks.StockPrice;
import com.example.model.um.User;
import com.example.repo.StockFactorRepository;
import com.example.repo.StockPriceRepository;
import static java.time.temporal.ChronoUnit.DAYS;

import java.io.IOException;
import java.net.MalformedURLException;;

@Transactional
@Service
public class WatchListService {

	@Autowired
	private CylhService cylhService;
	
	@Autowired
	private FactorRediff factorRediff;
	
	@Autowired
	private StockPriceRepository stockPriceRepository;
	
	@Autowired
	private StockFactorRepository stockFactorRepository;
	
	public void updateDailyWatchListPrice(User user) {
		
		Set<Stock> watchList = user.getWatchList();
		
		for(Stock stock : watchList) {
			
			if(stock.getStockPrice().getLastModified().isBefore(LocalDate.now())) {
				
				StockPrice stockPrice = cylhService.getChylPrice(stock);
				
				stockPriceRepository.save(stockPrice);
			}
			if(DAYS.between(stock.getStockFactor().getLastModified(), LocalDate.now()) > 120) {
				
				try {
					
					StockFactor  stockFactor = factorRediff.getFactor(stock);
					
					stockFactorRepository.save(stockFactor);
					
				} catch (MalformedURLException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
				
					e.printStackTrace();
				}
				
			}
		}
		
	}
	
}
