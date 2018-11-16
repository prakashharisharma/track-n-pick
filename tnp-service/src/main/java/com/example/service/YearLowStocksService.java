package com.example.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
import com.example.model.um.User;
import com.example.util.Rules;

@Transactional
@Service
public class YearLowStocksService {

	private static final Logger LOGGER = LoggerFactory.getLogger(YearLowStocksService.class);

	@Autowired
	private Rules rules;

	@Autowired
	private DylhService dylhService;

	@Autowired
	private CylhService cylhService;

	@Autowired
	private StockService stockService;

	@Autowired
	private SectorService sectorService;

	@Autowired
	private UserService userService;

	@Autowired
	private RuleService ruleService;
	
	public List<Stock> yearLowStocks() {

		List<Stock> masterStocksList = stockService.getActiveStocks();

		List<Stock> todaysYearLowStocks = dylhService.yearLowStocks();

		todaysYearLowStocks.retainAll(masterStocksList);

		List<Stock> todaysYearLowStocksFinalList = new ArrayList<>();

		for (Stock stock : todaysYearLowStocks) {

			todaysYearLowStocksFinalList.add(stockService.getStockByNseSymbol(stock.getNseSymbol()));
		}

		return ruleService.applyFilterRuleYearLow(todaysYearLowStocksFinalList);
		//return applyFilterRuleYearLow(todaysYearLowStocksFinalList);
	}

	private List<Stock> applyFilterRuleYearLow(List<Stock> inputStockList) {

		User user = userService.getUserById(1);

		Set<Stock> watchList = user.getWatchList();

		inputStockList.removeAll(watchList);

		List<Stock> afterRuleList = inputStockList.stream()
				.filter((s) -> s.getStockFactor().getMarketCap() > rules.getMcap()
						&& s.getStockFactor().getDebtEquity() < rules.getDebtEquity()
						&& s.getStockFactor().getDividend() > rules.getDividend()
						&& s.getStockFactor().getReturnOnEquity() > rules.getRoe()
						&& s.getStockFactor().getReturnOnCapital() > rules.getRoce())
				.collect(Collectors.toList());

		List<Stock> resultStocks = new ArrayList<>();

		for (Stock stock : afterRuleList) {

			StockPrice stockPrice = cylhService.getChylPrice(stockService.getStockByNseSymbol(stock.getNseSymbol()));

			if (stockPrice != null && stockPrice.getCurrentPrice() < rules.getPrice()) {

				LOGGER.info(stock.getNseSymbol() + " : " + stockPrice);

				double currentPrice = stockPrice.getCurrentPrice();

				double bookValue = stock.getStockFactor().getBookValue();

				double eps = stock.getStockFactor().getEps();

				double pe = currentPrice / eps;
				LOGGER.info(stock.getNseSymbol() + " - PE : " + pe);
				double pb = currentPrice / bookValue;

				LOGGER.info(stock.getNseSymbol() + " - PB : " + pb);

				if (pb <= sectorService.getSectorByName(stock.getSector()).getSectorPb()
						&& pe <= sectorService.getSectorByName(stock.getSector()).getSectorPe()) {

					LOGGER.info("DEFAULT RULE ");
					
					resultStocks.add(stock);

					stockPrice.setStock(stock);

					stock.setStockPrice(stockPrice);

					stock = stockService.save(stock);

				} else if (pe <= sectorService.getSectorByName(stock.getSector()).getSectorPe() && pb <= rules.getPb()) {
					
					LOGGER.info(" RULE 1");
					
					resultStocks.add(stock);

					stockPrice.setStock(stock);

					stock.setStockPrice(stockPrice);

					stock = stockService.save(stock);
				}else if(pb <= sectorService.getSectorByName(stock.getSector()).getSectorPb() && pe <= (sectorService.getSectorByName(stock.getSector()).getSectorPe()) + rules.getPe()) {
					LOGGER.info(" RULE 2");
					resultStocks.add(stock);

					stockPrice.setStock(stock);

					stock.setStockPrice(stockPrice);

					stock = stockService.save(stock);
				}

			}
		}

		return resultStocks;
	}

	public List<Stock> yearHighStocks() {

		List<Stock> todaysYearHighStocks = dylhService.yearHighStocks();

		User user = userService.getUserById(1);

		Set<Stock> watchList = user.getWatchList();

		todaysYearHighStocks.retainAll(watchList);

		return todaysYearHighStocks;

	}

}
