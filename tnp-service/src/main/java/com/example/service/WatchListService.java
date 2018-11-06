package com.example.service;

import static java.time.temporal.ChronoUnit.DAYS;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
	
	@Autowired
	private YearLowStocksService yearLowStocksService;
	
	@Autowired
	private UserService userService;
	
	private void updateDailyWatchListPrice(User user) {
		
		Set<Stock> watchList = user.getWatchList();
		
		for(Stock stock : watchList) {
			
			if(stock.getStockPrice().getLastModified().isBefore(LocalDate.now())) {
				
				StockPrice stockPrice = cylhService.getChylPrice(stock);
				
				stockPriceRepository.save(stockPrice);
			}
			if(DAYS.between(stock.getStockFactor().getLastModified(), LocalDate.now()) > 60) {
				
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
	
	public void updateDailyWatchListAddStocks(User user) {
		
		List<Stock> stockLiost = yearLowStocksService.yearLowStocks();
		
		user = userService.addtoWatchList(user, stockLiost);
		
		updateDailyWatchListPrice(user);
	}
	
	public void updateMonthlyWatchListRemoveStocks(User user) {
		
		Set<Stock> stockList = user.getWatchList();
		
		List<Stock> tobeRemovedfromWatchList = new ArrayList<>();
		
		for(Stock stock : stockList) {
			
			StockPrice stockPrice = stock.getStockPrice();
			
			double currentPrice = stockPrice.getCurrentPrice();
			
			double yearHigh = stockPrice.getYearHigh();
			
			double per_10 = stockPrice.getYearHigh() * 0.10;
			
			if(isBetween((yearHigh - per_10), (yearHigh + per_10), currentPrice)) {
				tobeRemovedfromWatchList.add(stock);
			}
			
		}
		userService.removeFromtoWatchList(user, tobeRemovedfromWatchList);
	}

	// Return true if c is between a and b.
	public static boolean isBetween(double a, double b, double c) {
	    return b > a ? c > a && c < b : c > b && c < a;
	}
	
	public List<Stock> userWatchList(User user){
		
		Set<Stock> watchList = user.getWatchList();
		
		List<Stock> sortedWatchList = watchList.stream().sorted(byRoeComparator().thenComparing(byDebtEquityComparator())).collect(Collectors.toList());
		
		return sortedWatchList;
		
	}

	private Comparator<Stock> byRoeComparator(){
		return Comparator.comparing(
			    stock -> stock.getStockFactor().getReturnOnEquity(), Comparator.reverseOrder()
			    );
	}
	
	private Comparator<Stock> byDebtEquityComparator(){
		return Comparator.comparing(
				stock ->  stock.getStockFactor().getDebtEquity()
		);
	}
	
}
