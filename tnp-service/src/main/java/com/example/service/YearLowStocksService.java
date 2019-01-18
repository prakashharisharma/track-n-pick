package com.example.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.dylh.service.DylhService;
import com.example.model.master.Stock;
import com.example.model.um.UserProfile;

@Transactional
@Service
public class YearLowStocksService {

	private static final Logger LOGGER = LoggerFactory.getLogger(YearLowStocksService.class);

	@Autowired
	private DylhService dylhService;

	@Autowired
	private StockService stockService;

	@Autowired
	private UserService userService;

	@Autowired
	private RuleService ruleService;
	
	public List<Stock> yearLowStocks() {

		LOGGER.info("RETREIVING YEAR LOW STOCKS ..");
		
		List<Stock> masterStocksList = stockService.getActiveStocks();

		List<Stock> todaysYearLowStocks = dylhService.yearLowStocks();

		todaysYearLowStocks.retainAll(masterStocksList);

		List<Stock> todaysYearLowStocksFinalList = new ArrayList<>();

		for (Stock stock : todaysYearLowStocks) {

			todaysYearLowStocksFinalList.add(stockService.getStockByNseSymbol(stock.getNseSymbol()));
		}

		List<Stock> filteredStocks = ruleService.applyFilterRuleYearLow(todaysYearLowStocksFinalList);
		
		filteredStocks.forEach((stock) -> {
			LOGGER.info("YEAR LOW STOCK .." + stock.getNseSymbol());
		});
		
		return filteredStocks;
		
	}

	public List<Stock> yearHighStocks() {

		List<Stock> todaysYearHighStocks = dylhService.yearHighStocks();

		UserProfile user = userService.getUserById(1);

		Set<Stock> watchList = user.getWatchList();

		todaysYearHighStocks.retainAll(watchList);

		return todaysYearHighStocks;

	}

}
