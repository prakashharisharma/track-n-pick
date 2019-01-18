package com.example.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.ResearchLedger;
import com.example.model.master.Stock;
import com.example.model.um.UserProfile;
import com.example.repo.ledger.ResearchLedgerRepository;
import com.example.util.MiscUtil;
import com.example.util.rules.RulesFundamental;
import com.example.util.rules.RulesResearch;

@Transactional
@Service
public class ResearchLedgerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResearchLedgerService.class);

	@Autowired
	private ResearchLedgerRepository researchLedgerRepository;

	@Autowired
	private ResearchLedgerHistoryService researchLedgerHistoryService;

	@Autowired
	private RulesFundamental rules;

	@Autowired
	private RulesResearch researchRules;
	
	
	@Autowired
	private UserService userService;

	@Autowired
	private RuleService ruleService;

	@Autowired
	private MiscUtil miscUtil;
	
	public void addStock(Stock stock) {

		ResearchLedger ledgerStock = researchLedgerRepository.findByStockAndActive(stock, true);

		if (ledgerStock != null) {

			researchLedgerRepository.save(ledgerStock);

		} else {
			ledgerStock = new ResearchLedger();
			ledgerStock.setActive(true);
			ledgerStock.setNotified(false);
			ledgerStock.setResearchDate(miscUtil.currentDate());
			
			System.out.println("PRICe" +miscUtil.currentDate() );
			
			System.out.println("PRICe" +stock.getStockPrice().getCurrentPrice() );
			
			ledgerStock.setResearchPrice(stock.getStockPrice().getCurrentPrice());
			ledgerStock.setStock(stock);

			double currentPrice = stock.getStockPrice().getCurrentPrice();

			double targetPrice = currentPrice + (currentPrice * researchRules.getTargetPer());

			ledgerStock.setTargetPrice(targetPrice);

			researchLedgerRepository.save(ledgerStock);
		}

	}

	public void markNotified(List<Stock> stockList) {

		ResearchLedger ledgerStock = null;
		
		for (Stock stock : stockList) {
			
			ledgerStock = researchLedgerRepository.findByStockAndActive(stock, true);

			if (ledgerStock != null) {

				ledgerStock.setNotified(true);

				researchLedgerRepository.save(ledgerStock);
			}
		}

	}

	public List<ResearchLedger> researchStocks(){
		
		return researchLedgerRepository.findAll();
	}
	
	public ResearchLedger ledgerDetails(Stock stock) {
		return researchLedgerRepository.findByStockAndActive(stock, true);
	}

	public boolean isActive(Stock stock) {

		boolean result = false;

		ResearchLedger ledgerStock = researchLedgerRepository.findByStockAndActive(stock, true);

		if (ledgerStock != null) {
			result = true;
		}

		return result;
	}

	public List<ResearchLedger> activeResearchStocks() {

		List<ResearchLedger> activeResearchList = researchLedgerRepository.findAll();

		return activeResearchList;
	}

	public List<ResearchLedger> updateDailyResearchListTargetAchived() {

		List<ResearchLedger> researchPickTargetAchived = new ArrayList<>();

		List<ResearchLedger> researchList = activeResearchStocks();

		LOGGER.info("START..");

		for (ResearchLedger researchLedger : researchList) {

			LOGGER.info("Checking.." + researchLedger.getStock().getNseSymbol() + " Research Price :  "
					+ researchLedger.getResearchPrice() + " Target Price :" + researchLedger.getTargetPrice());

			if (researchLedger.getTargetPrice() < researchLedger.getStock().getStockPrice().getCurrentPrice()) {
				LOGGER.info("TARGET ACHIVED.." + researchLedger.getStock().getNseSymbol());
				researchPickTargetAchived.add(researchLedger);
				targetAchived(researchLedger);
			} else {
				LOGGER.info("NO TARGET ACHIVED.." + researchLedger.getStock().getNseSymbol() + " Current Price : "
						+ researchLedger.getStock().getStockPrice().getCurrentPrice());
			}

		}

		LOGGER.info("END..");

		return researchPickTargetAchived;
	}

	public void targetAchived(ResearchLedger researchLedger) {

		if (researchLedger != null) {

			researchLedger.setActive(false);
			researchLedger.setTargetDate(LocalDate.now());
		}

		researchLedgerHistoryService.addStock(researchLedger);

		researchLedgerRepository.delete(researchLedger);

	}

	public List<Stock> researchNnotificationStocks() {

		List<ResearchLedger> researchLedgerList = researchLedgerRepository.findByNotified(false);

		return researchLedgerList.stream().map((rl) -> rl.getStock()).collect(Collectors.toList());
	}

	public void researchValueStocks() {

		UserProfile user = userService.getUserById(1);

		Set<Stock> userWatchList = user.getWatchList();

		List<Stock> filtereduserWatchList = ruleService.applyWatchListFilterRule(userWatchList);

		// List<Stock> filteredWatchList = new ArrayList<>();

		filtereduserWatchList.stream().forEach(s -> {
			LOGGER.info(" Research on  " + s.getNseSymbol() + " .. ");

			if (!isActive(s)) {
				addStock(s);
				// filteredWatchList.add(s);
				LOGGER.info(" Added to Research " + s.getNseSymbol() + " .. ");

			} else {
				LOGGER.info(" Already in Research List  " + s.getNseSymbol() + " .. ");

			}
		}

		);

		/*
		 * return filteredWatchList.stream()
		 * .sorted(byRoeComparator().thenComparing(byDebtEquityComparator())).limit(
		 * rules.getWatchlistSize()) .collect(Collectors.toList());
		 */

	}

	private Comparator<Stock> byRoeComparator() {
		return Comparator.comparing(stock -> stock.getStockFactor().getReturnOnEquity(), Comparator.reverseOrder());
	}

	private Comparator<Stock> byDebtEquityComparator() {
		return Comparator.comparing(stock -> stock.getStockFactor().getDebtEquity());
	}
}
