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

import com.example.external.chyl.service.CylhService;
import com.example.external.factor.FactorRediff;
import com.example.model.master.Stock;
import com.example.model.stocks.StockFactor;
import com.example.model.stocks.StockPrice;
import com.example.model.stocks.UserPortfolio;
import com.example.model.um.UserProfile;
import com.example.repo.stocks.StockFactorRepository;
import com.example.util.FormulaService;
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

	@Autowired
	private FormulaService formulaService;
	
	public List<Stock> applyFilterRuleYearLow(List<Stock> inputStockList) {

		UserProfile user = userService.getUserById(1);

		Set<Stock> watchList = user.getWatchList();

		inputStockList.removeAll(watchList);

		return this.filterFundamentalStocks(inputStockList);

	}

	public List<Stock> filterFundamentalStocks(Collection<Stock> inputStockList) {

		List<Stock> afterRuleList = filterFundamentalCommon(inputStockList);

		List<Stock> resultStocks = new ArrayList<>();

		for (Stock stock : afterRuleList) {
			
			if(this.isUndervalued(stock)) {
				resultStocks.add(stock);
			}
}

		return resultStocks;
	}

	public List<Stock> filterFundamentalStocksWatchList(Collection<Stock> inputStockList) {

		return this.filterFundamentalStocks(inputStockList);
}

	public List<UserPortfolio> applyAveragingFilterRule(Collection<UserPortfolio> inputStockList) {

		List<UserPortfolio> afterRuleList = filterPortfolioFundamentalStocks(inputStockList);

		List<UserPortfolio> resultStocks = new ArrayList<>();

		for (UserPortfolio stock : afterRuleList) {
			
			if(this.isUndervalued(stock.getStock())) {
				resultStocks.add(stock);
			}
}

		return resultStocks;
	}

	public boolean isPriceInRange(Stock stock) {
		boolean isPriceInRange = false;
		
		if(stock.getStockPrice().getCurrentPrice() > rules.getPricegt() && stock.getStockPrice().getCurrentPrice() < rules.getPricelt()) {
			isPriceInRange = true;
		}
		
		
		return isPriceInRange;
	}
	
	public boolean isPriceOutRange(Stock stock) {
		boolean isPriceOutRange = false;
		
		if(stock.getStockPrice().getCurrentPrice() <= rules.getPricegt() && stock.getStockPrice().getCurrentPrice() >= rules.getPricelt()) {
			isPriceOutRange = true;
		}
		
		
		return isPriceOutRange;
	}
	
	
	public boolean isUndervalued(Stock stock) {
		boolean isUndervalued = false;

		if (this.isUndervaluedPre(stock)) {

			StockPrice stockPrice = stock.getStockPrice();

			if (isPriceInRange(stock)) {

				double currentPrice = stockPrice.getCurrentPrice();

				double bookValue = stock.getStockFactor().getBookValue();

				double eps = stock.getStockFactor().getEps();

				double pe = formulaService.calculatePe(currentPrice, eps);

				double pb = formulaService.calculatePb(currentPrice, bookValue);

				LOGGER.debug(stock.getNseSymbol() + " : " + stockPrice + " PE : " + pe + " PB : " + pb);

				if (pe <= researchRules.getPe() && pb <= researchRules.getPb()) {
					LOGGER.debug(" RULE 1 ");

					isUndervalued = true;
				} else if (pe <= stock.getSector().getSectorPe() && pb <= researchRules.getPb()) {

					LOGGER.debug(" RULE 2");

					isUndervalued = true;

				} else if (pb <= stock.getSector().getSectorPb() && pe <= researchRules.getPe()) {
					LOGGER.debug(" RULE 3");
					isUndervalued = true;

				}

				else if (pb <= stock.getSector().getSectorPb() && pe <= stock.getSector().getSectorPe()) {

					LOGGER.debug(" RULE 4 ");

					isUndervalued = true;

				}
				
			}

		}

		return isUndervalued;
	}

	private boolean isUndervaluedPre(Stock stock) {

		boolean isUndervalued = false;

		if (stock.getSector().getSectorName().equalsIgnoreCase("FINANCIAL SERVICES")) {

			if (stock.getStockFactor().getMarketCap() >= rules.getMcap()
					&& stock.getStockFactor().getDividend() > rules.getDividend()
					&& stock.getStockFactor().getReturnOnEquity() >= rules.getRoe()
					&& stock.getStockFactor().getReturnOnCapital() >= rules.getRoce()
					&& stock.getStockFactor().getCurrentRatio() >= rules.getCurrentRatio()
					&& stock.getStockFactor().getQuickRatio() >= rules.getQuickRatioBanks()
					) {
				isUndervalued = true;
			}

		} else {
			if (stock.getStockFactor().getMarketCap() >= rules.getMcap()
					&& stock.getStockFactor().getDebtEquity() < rules.getDebtEquity()
					&& stock.getStockFactor().getDividend() >= rules.getDividend()
					&& stock.getStockFactor().getReturnOnEquity() >= rules.getRoe()
					&& stock.getStockFactor().getReturnOnCapital() >= rules.getRoce()
					&& stock.getStockFactor().getCurrentRatio() >= rules.getCurrentRatio()
					
					) {

				isUndervalued = true;
			}
		}

		return isUndervalued;
	}

	private boolean isOvervaluedPre(Stock stock) {
		boolean isOvervalued = false;
		

		if (stock.getSector().getSectorName().equalsIgnoreCase("FINANCIAL SERVICES")) {

			if (
					stock.getStockFactor().getDividend() < rules.getDividend()
					
					&& stock.getStockFactor().getReturnOnEquity() < rules.getRoe()
					&& stock.getStockFactor().getReturnOnCapital() < rules.getRoce()
					&& stock.getStockFactor().getCurrentRatio() < rules.getCurrentRatio()
					&& stock.getStockFactor().getQuickRatio() < rules.getQuickRatioBanks()
					) {
				isOvervalued = true;
			}

		} else {
			if (
					stock.getStockFactor().getDebtEquity() > rules.getDebtEquity()
					&& stock.getStockFactor().getDividend() < rules.getDividend()
					&& stock.getStockFactor().getReturnOnEquity() < rules.getRoe()
					&& stock.getStockFactor().getReturnOnCapital() < rules.getRoce()
					&& stock.getStockFactor().getCurrentRatio() < rules.getCurrentRatio()
					
					) {

				isOvervalued = true;
			}
		}
		
		return isOvervalued;
	}
	
	public boolean isOvervalued(Stock stock) {
		boolean isOvervalued = false;

		StockPrice stockPrice = stock.getStockPrice();

		double currentPrice = stockPrice.getCurrentPrice();

		double bookValue = stock.getStockFactor().getBookValue();

		double eps = stock.getStockFactor().getEps();

		double pe = currentPrice / eps;

		double pb = currentPrice / bookValue;
		
		
		if (this.isOvervaluedPre(stock)) {
			isOvervalued = true;
		}else if(isPriceOutRange(stock)) {
			isOvervalued = true;
		}else if (pe > stock.getSector().getSectorPe() + 5){
			isOvervalued = true;
		}else if(pb > stock.getSector().getSectorPb() + 3) {
			isOvervalued = true;
		}
		
		return isOvervalued;
	}

	
	private List<Stock> filterFundamentalCommon(Collection<Stock> inputStockList) {

		List<Stock> resultList = new ArrayList<>();

		List<Stock> bankingList = inputStockList.stream()
				.filter((s) -> s.getSector().getSectorName().equalsIgnoreCase("FINANCIAL SERVICES")
						&& s.getStockFactor().getMarketCap() > rules.getMcap()

						&& s.getStockFactor().getDividend() > rules.getDividend()

				).collect(Collectors.toList());

		List<Stock> nonBankingList = inputStockList.stream()
				.filter((s) -> !s.getSector().getSectorName().equalsIgnoreCase("FINANCIAL SERVICES")
						&& s.getStockFactor().getMarketCap() > rules.getMcap()
						&& s.getStockFactor().getDebtEquity() < rules.getDebtEquity()
						&& s.getStockFactor().getDividend() >= rules.getDividend()
						&& s.getStockFactor().getReturnOnEquity() > rules.getRoe()
						&& s.getStockFactor().getReturnOnCapital() > rules.getRoce()
						&& s.getStockFactor().getCurrentRatio() > rules.getCurrentRatio())
				.collect(Collectors.toList());

		resultList.addAll(bankingList);
		resultList.addAll(nonBankingList);

		return resultList;
	}

	public List<UserPortfolio> filterPortfolioFundamentalStocks(Collection<UserPortfolio> inputStockList) {

		List<UserPortfolio> resultList = new ArrayList<>();

		List<UserPortfolio> bankingList = inputStockList.stream()
				.filter((s) -> s.getStock().getSector().getSectorName().equalsIgnoreCase("FINANCIAL SERVICES")
						&& s.getStock().getStockFactor().getMarketCap() > rules.getMcap()

						&& s.getStock().getStockFactor().getDividend() > rules.getDividend()

				).collect(Collectors.toList());

		List<UserPortfolio> nonBankingList = inputStockList.stream()
				.filter((s) -> !s.getStock().getSector().getSectorName().equalsIgnoreCase("FINANCIAL SERVICES")
						&& s.getStock().getStockFactor().getMarketCap() > rules.getMcap()
						&& s.getStock().getStockFactor().getDebtEquity() < rules.getDebtEquity()
						&& s.getStock().getStockFactor().getDividend() >= rules.getDividend()
						&& s.getStock().getStockFactor().getReturnOnEquity() > rules.getRoe()
						&& s.getStock().getStockFactor().getReturnOnCapital() > rules.getRoce()
						&& s.getStock().getStockFactor().getCurrentRatio() > rules.getCurrentRatio())
				.collect(Collectors.toList());

		resultList.addAll(bankingList);
		resultList.addAll(nonBankingList);

		return resultList;

	}

}
