package com.example.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chyl.service.CylhService;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.model.stocks.UserPortfolio;
import com.example.model.um.User;
import com.example.util.MiscUtil;
import com.example.util.Rules;

@Transactional
@Service
public class RuleService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RuleService.class);
	
	@Autowired
	private Rules rules;

	@Autowired
	private CylhService cylhService;

	@Autowired
	private StockService stockService;

	@Autowired
	private SectorService sectorService;

	@Autowired
	private UserService userService;
	
	@Autowired
	private MiscUtil miscUtil;
	
	public List<Stock> applyFilterRuleYearLow(List<Stock> inputStockList) {

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
	
	public List<Stock> applyWatchListFilterRule(Collection<Stock> inputStockList) {

		List<Stock> afterRuleList = inputStockList.stream()
				.filter((s) -> s.getStockFactor().getMarketCap() > rules.getMcap()
						&& s.getStockFactor().getDebtEquity() < rules.getDebtEquity()
						&& s.getStockFactor().getDividend() > rules.getDividend()
						&& s.getStockFactor().getReturnOnEquity() > rules.getRoe()
						&& s.getStockFactor().getReturnOnCapital() > rules.getRoce())
				.collect(Collectors.toList());

		List<Stock> resultStocks = new ArrayList<>();

		for (Stock stock : afterRuleList) {

			StockPrice stockPrice = stock.getStockPrice();

			if (stockPrice != null && stockPrice.getCurrentPrice() < rules.getPrice()) {

				LOGGER.info(stock.getNseSymbol() + " : " + stockPrice);

				double currentPrice = stockPrice.getCurrentPrice();
				
				double yearLow = stockPrice.getYearLow();
				
				double yearHigh= stockPrice.getYearHigh();
				
				double yearLow_per = yearLow * rules.getLowThreshold();
				
				double yearHigh_per = yearLow * rules.getHighThreshold();

				double yearLowThreasHold = yearLow + yearLow_per;
				
				double yearHighThreasHold = yearHigh - yearHigh_per;
				
				double bookValue = stock.getStockFactor().getBookValue();

				double eps = stock.getStockFactor().getEps();

				double pe = currentPrice / eps;
				LOGGER.info(stock.getNseSymbol() + " - PE : " + pe);
				double pb = currentPrice / bookValue;

				LOGGER.info(stock.getNseSymbol() + " - PB : " + pb);
				
				LOGGER.info(stock.getNseSymbol() + " - yearLowThreasHold : " + yearLowThreasHold);
				LOGGER.info(stock.getNseSymbol() + " - yearHighThreasHold : " + yearHighThreasHold);
				LOGGER.info(stock.getNseSymbol() + " - currentPrice : " + currentPrice);

				if(miscUtil.isBetween(yearLowThreasHold,yearHighThreasHold,currentPrice)) {
				
					if (pb <= sectorService.getSectorByName(stock.getSector()).getSectorPb()
							&& pe <= sectorService.getSectorByName(stock.getSector()).getSectorPe()) {
	
						LOGGER.info("DEFAULT RULE ");
						
						resultStocks.add(stock);
	
					} else if (pe <= sectorService.getSectorByName(stock.getSector()).getSectorPe() && pb <= rules.getPb()) {
						
						LOGGER.info(" RULE 1");
						
						resultStocks.add(stock);
	
					}else if(pb <= sectorService.getSectorByName(stock.getSector()).getSectorPb() && pe <= (sectorService.getSectorByName(stock.getSector()).getSectorPe()) + rules.getPe()) {
						LOGGER.info(" RULE 2");
						resultStocks.add(stock);
	
					}
				}else {
					LOGGER.info(stock.getNseSymbol() +" : CURRENT PRICE IS NOT IN BETWEEN");
				}
			}
		}

		return resultStocks;
	}
	
	public List<UserPortfolio> applyAveragingFilterRule(Collection<UserPortfolio> inputStockList){

		List<UserPortfolio> afterRuleList = inputStockList.stream()
				.filter((s) -> s.getStock().getStockFactor().getMarketCap() > rules.getMcap()
						&& s.getStock().getStockFactor().getDebtEquity() < rules.getDebtEquity()
						&& s.getStock().getStockFactor().getDividend() > rules.getDividend()
						&& s.getStock().getStockFactor().getReturnOnEquity() > rules.getRoe()
						&& s.getStock().getStockFactor().getReturnOnCapital() > rules.getRoce())
				.collect(Collectors.toList());

		List<UserPortfolio> resultStocks = new ArrayList<>();

		for (UserPortfolio stock : afterRuleList) {

			StockPrice stockPrice = stock.getStock().getStockPrice();

			if (stockPrice != null && stockPrice.getCurrentPrice() < rules.getPrice()) {

				LOGGER.info(stock.getStock().getNseSymbol() + " : " + stockPrice);

				double currentPrice = stockPrice.getCurrentPrice();
				
				double yearLow = stockPrice.getYearLow();
				
				double yearHigh= stockPrice.getYearHigh();
				
				double yearLow_per = yearLow * rules.getLowThreshold();
				
				double yearHigh_per = yearLow * rules.getHighThreshold();

				double yearLowThreasHold = yearLow + yearLow_per;
				
				double yearHighThreasHold = yearHigh - yearHigh_per;
				
				double bookValue = stock.getStock().getStockFactor().getBookValue();

				double eps = stock.getStock().getStockFactor().getEps();

				double pe = currentPrice / eps;
				LOGGER.info(stock.getStock().getNseSymbol() + " - PE : " + pe);
				double pb = currentPrice / bookValue;

				LOGGER.info(stock.getStock().getNseSymbol() + " - PB : " + pb);

				if(miscUtil.isBetween(yearLowThreasHold,yearHighThreasHold,currentPrice)) {
				
					if (pb <= sectorService.getSectorByName(stock.getStock().getSector()).getSectorPb()
							&& pe <= sectorService.getSectorByName(stock.getStock().getSector()).getSectorPe()) {
	
						LOGGER.info("DEFAULT RULE ");
						
						resultStocks.add(stock);
	
					} else if (pe <= sectorService.getSectorByName(stock.getStock().getSector()).getSectorPe() && pb <= rules.getPb()) {
						
						LOGGER.info(" RULE 1");
						
						resultStocks.add(stock);
	
					}else if(pb <= sectorService.getSectorByName(stock.getStock().getSector()).getSectorPb() && pe <= (sectorService.getSectorByName(stock.getStock().getSector()).getSectorPe()) + rules.getPe()) {
						LOGGER.info(" RULE 2");
						resultStocks.add(stock);
	
					}
				}
			}
		}

		return resultStocks;
	}
}
