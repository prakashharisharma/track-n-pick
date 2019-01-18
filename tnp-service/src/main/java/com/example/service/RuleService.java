package com.example.service;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
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
import com.example.factor.FactorRediff;
import com.example.model.master.Stock;
import com.example.model.stocks.StockFactor;
import com.example.model.stocks.StockPrice;
import com.example.model.stocks.UserPortfolio;
import com.example.model.um.UserProfile;
import com.example.repo.stocks.StockFactorRepository;
import com.example.util.MiscUtil;
import com.example.util.rules.RulesFundamental;
import com.example.util.rules.RulesNotification;
import com.example.util.rules.RulesResearch;

@Transactional
@Service
public class RuleService {

	private static final Logger LOGGER = LoggerFactory.getLogger(RuleService.class);
	
	@Autowired
	private RulesFundamental rules;

	@Autowired
	private RulesResearch researchRules;
	
	@Autowired
	private RulesNotification notificationRules;
	
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
	
	@Autowired
	private FactorRediff factorRediff;


	@Autowired
	private StockFactorRepository stockFactorRepository;
	
	public List<Stock> applyFilterRuleYearLow(List<Stock> inputStockList) {

		UserProfile user = userService.getUserById(1);

		Set<Stock> watchList = user.getWatchList();

		inputStockList.removeAll(watchList);

		List<Stock> afterRuleList = filterFundamentalStocks(inputStockList);

		List<Stock> resultStocks = new ArrayList<>();

		for (Stock stock : afterRuleList) {

			StockPrice stockPrice = cylhService.getChylPrice(stockService.getStockByNseSymbol(stock.getNseSymbol()));

			if (stockPrice != null && stockPrice.getCurrentPrice() < rules.getPrice()) {

				double currentPrice = stockPrice.getCurrentPrice();

				StockFactor stockFactor = null;
				
				if (DAYS.between(stock.getStockFactor().getLastModified(), LocalDate.now()) > notificationRules.getFactorIntervalDays()) {
					
					stockFactor = factorRediff.getFactor(stock);
					
				}else {
					stockFactor = stock.getStockFactor();
				}
				
				double bookValue = stockFactor.getBookValue();

				double eps = stockFactor.getEps();

				double pe = currentPrice / eps;
				
				double pb = currentPrice / bookValue;

				LOGGER.info(stock.getNseSymbol() + " : " + stockPrice + " PE : " + pe + " PB : " + pb);

				if (pb <= stock.getSector().getSectorPb() && pe <= stock.getSector().getSectorPe()) {

					LOGGER.info("DEFAULT RULE ");
					
					resultStocks.add(stock);

					stockPrice.setStock(stock);

					stock.setStockPrice(stockPrice);

					stock = stockService.save(stock);

				} else if (pe <= stock.getSector().getSectorPe() && pb <= researchRules.getPb()) {
					
					LOGGER.info(" RULE 1");
					
					resultStocks.add(stock);

					stockPrice.setStock(stock);

					stock.setStockPrice(stockPrice);

					stock = stockService.save(stock);
				}else if(pb <= stock.getSector().getSectorPb() && pe <= (stock.getSector().getSectorPe()) + researchRules.getPe()) {
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

		List<Stock> afterRuleList = filterFundamentalStocks(inputStockList);

		List<Stock> resultStocks = new ArrayList<>();

		for (Stock stock : afterRuleList) {

			StockPrice stockPrice = stock.getStockPrice();

			if (stockPrice != null && stockPrice.getCurrentPrice() < rules.getPrice()) {

				double currentPrice = stockPrice.getCurrentPrice();
				
				double yearLow = stockPrice.getYearLow();
				
				double yearHigh= stockPrice.getYearHigh();
				
				double yearLow_per = yearLow * researchRules.getLowThreshold();
				
				double yearHigh_per = yearLow * researchRules.getHighThreshold();

				double yearLowThreasHold = yearLow + yearLow_per;
				
				double yearHighThreasHold = yearHigh - yearHigh_per;
				
				double bookValue = stock.getStockFactor().getBookValue();

				double eps = stock.getStockFactor().getEps();

				double pe = currentPrice / eps;
				
				double pb = currentPrice / bookValue;

				LOGGER.info(stock.getNseSymbol() + " : " + stockPrice + " PE : " + pe + " PB : " + pb);
				
				LOGGER.info(stock.getNseSymbol() + " yearLowThreasHold : " + yearLowThreasHold+ " yearHighThreasHold : " + yearHighThreasHold+ " currentPrice : " + currentPrice);

				if(miscUtil.isBetween(yearLowThreasHold,yearHighThreasHold,currentPrice)) {
				
					if (pb <= stock.getSector().getSectorPb()
							&& pe <= stock.getSector().getSectorPe()) {
	
						LOGGER.info("DEFAULT RULE ");
						
						resultStocks.add(stock);
	
					} else if (pe <= stock.getSector().getSectorPe() && pb <= researchRules.getPb()) {
						
						LOGGER.info(" RULE 1");
						
						resultStocks.add(stock);
	
					}else if(pb <= stock.getSector().getSectorPb() && pe <= (stock.getSector().getSectorPe()) + researchRules.getPe()) {
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

		List<UserPortfolio> afterRuleList = filterPortfolioFundamentalStocks(inputStockList);

		List<UserPortfolio> resultStocks = new ArrayList<>();

		for (UserPortfolio stock : afterRuleList) {

			StockPrice stockPrice = stock.getStock().getStockPrice();

			if (stockPrice != null && stockPrice.getCurrentPrice() < rules.getPrice()) {

				double currentPrice = stockPrice.getCurrentPrice();
				
				double yearLow = stockPrice.getYearLow();
				
				double yearHigh= stockPrice.getYearHigh();
				
				double yearLow_per = yearLow * researchRules.getLowThreshold();
				
				double yearHigh_per = yearLow * researchRules.getHighThreshold();

				double yearLowThreasHold = yearLow + yearLow_per;
				
				double yearHighThreasHold = yearHigh - yearHigh_per;
				
				double bookValue = stock.getStock().getStockFactor().getBookValue();

				double eps = stock.getStock().getStockFactor().getEps();

				double pe = currentPrice / eps;
				
				double pb = currentPrice / bookValue;

				LOGGER.info(stock.getStock().getNseSymbol() + " : Current :" + stockPrice.getCurrentPrice() + " PE : " + pe + " PB : " + pb);

				//if(miscUtil.isBetween(yearLowThreasHold,yearHighThreasHold,currentPrice)) {
				
					if (pb <= stock.getStock().getSector().getSectorPb()
							&& pe <= stock.getStock().getSector().getSectorPe()) {
	
						LOGGER.info("DEFAULT RULE ");
						
						resultStocks.add(stock);
	
					} else if (pe <= stock.getStock().getSector().getSectorPe() && pb <= researchRules.getPb()) {
						
						LOGGER.info(" RULE 1");
						
						resultStocks.add(stock);
	
					}else if(pb <= stock.getStock().getSector().getSectorPb() && pe <= (stock.getStock().getSector().getSectorPe()) + researchRules.getPe()) {
						LOGGER.info(" RULE 2");
						resultStocks.add(stock);
	
					}else {
						LOGGER.info(" RULE CRITERIA NOT MATCH");
					}
				//}
			}
		}

		return resultStocks;
	}
	
	public List<Stock> filterFundamentalStocks(Collection<Stock> inputStockList){
		
		List<Stock> resultList = new ArrayList<>();
		
		List<Stock> bankingList = inputStockList.stream()
				.filter((s) -> s.getSector().getSectorName().equalsIgnoreCase("FINANCIAL SERVICES") && s.getStockFactor().getMarketCap() > rules.getMcap()
						
						&& s.getStockFactor().getDividend() >= rules.getDividend()
						
						
						)
				.collect(Collectors.toList());
		
		List<Stock> nonBankingList = inputStockList.stream()
				.filter((s) -> !s.getSector().getSectorName().equalsIgnoreCase("FINANCIAL SERVICES") && s.getStockFactor().getMarketCap() > rules.getMcap()
						&& s.getStockFactor().getDebtEquity() < rules.getDebtEquity()
						&& s.getStockFactor().getDividend() >= rules.getDividend()
						&& s.getStockFactor().getReturnOnEquity() > rules.getRoe()
						&& s.getStockFactor().getReturnOnCapital() > rules.getRoce())
				.collect(Collectors.toList());
		
		resultList.addAll(bankingList);
		resultList.addAll(nonBankingList);
		
		 return resultList;
	}
	
public List<UserPortfolio> filterPortfolioFundamentalStocks(Collection<UserPortfolio> inputStockList){
	
	List<UserPortfolio> resultList = new ArrayList<>();
	
	List<UserPortfolio> bankingList = inputStockList.stream()
			.filter((s) -> s.getStock().getSector().getSectorName().equalsIgnoreCase("FINANCIAL SERVICES") 
					&& s.getStock().getStockFactor().getMarketCap() > rules.getMcap()
					
					&& s.getStock().getStockFactor().getDividend() >= rules.getDividend()
					
					)
			.collect(Collectors.toList());
	
	List<UserPortfolio> nonBankingList = inputStockList.stream()
				.filter((s) -> !s.getStock().getSector().getSectorName().equalsIgnoreCase("FINANCIAL SERVICES") 
						&& s.getStock().getStockFactor().getMarketCap() > rules.getMcap()
						&& s.getStock().getStockFactor().getDebtEquity() < rules.getDebtEquity()
						&& s.getStock().getStockFactor().getDividend() >= rules.getDividend()
						&& s.getStock().getStockFactor().getReturnOnEquity() > rules.getRoe()
						&& s.getStock().getStockFactor().getReturnOnCapital() > rules.getRoce())
				.collect(Collectors.toList());
	
	resultList.addAll(bankingList);
	resultList.addAll(nonBankingList);
	
	return resultList;
		
		
	}
	
}
