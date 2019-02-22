package com.example.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.external.dylh.service.DylhService;
import com.example.model.ledger.ResearchLedger;
import com.example.model.master.Stock;
import com.example.model.type.ResearchStatus;
import com.example.repo.ledger.ResearchLedgerRepository;
import com.example.util.MiscUtil;
import com.example.util.io.model.ResearchType;
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
	private TechnicalsResearchService technicalsResearchService;

	@Autowired
	private UndervalueLedgerService undervalueLedgerService;

	@Autowired
	private UserService userService;

	@Autowired
	private RuleService ruleService;
	@Autowired
	private DylhService dylhService;
	@Autowired
	private MiscUtil miscUtil;

	public void addResearch(Stock stock, ResearchType researchType) {

		ResearchLedger researchLedger = researchLedgerRepository.findByStockAndResearchType(stock, researchType);

		if (researchLedger == null) {
			researchLedger = new ResearchLedger();
			researchLedger.setStock(stock);
			researchLedger.setEntryhDate(miscUtil.currentDate());
			researchLedger.setEntryPrice(stock.getStockPrice().getCurrentPrice());
			researchLedger.setResearchType(researchType);
			researchLedger.setResearchStatus(ResearchStatus.BUY);
			researchLedgerRepository.save(researchLedger);
		} else {
			LOGGER.debug(stock.getNseSymbol() + " is already in Ledger for " + researchType);
		}

	}

	public void updateResearch(Stock stock, ResearchType researchType) {

		ResearchLedger researchLedger = researchLedgerRepository.findByStockAndResearchType(stock, researchType);
		if (researchLedger != null) {

			researchLedger.setExitDate(miscUtil.currentDate());
			researchLedger.setExitPrice(stock.getStockPrice().getCurrentPrice());
			researchLedger.setResearchStatus(ResearchStatus.SELL);

		} else {
			researchLedger = new ResearchLedger();
			researchLedger.setStock(stock);
			researchLedger.setExitDate(miscUtil.currentDate());
			researchLedger.setExitPrice(stock.getStockPrice().getCurrentPrice());
			researchLedger.setResearchType(researchType);
			researchLedger.setResearchStatus(ResearchStatus.SELL);

		}
		researchLedgerRepository.save(researchLedger);
	}

	public void updateResearchNotifiedBuy(Stock stock, ResearchType researchType) {
		ResearchLedger researchLedger = researchLedgerRepository.findByStockAndResearchType(stock, researchType);
		if (researchLedger != null) {

			researchLedger.setNotifiedBuy(true);

		}
		researchLedgerRepository.save(researchLedger);
	}

	public void updateResearchNotifiedSell(Stock stock, ResearchType researchType) {

		ResearchLedger researchLedger = researchLedgerRepository.findByStockAndResearchType(stock, researchType);
		if (researchLedger != null) {

			researchLedger.setNotifiedSell(true);

		}
		researchLedgerRepository.save(researchLedger);

	}

	public List<ResearchLedger> allActiveResearch() {
		return researchLedgerRepository.findByResearchStatus(ResearchStatus.BUY);
	}

	public List<ResearchLedger> allNotificationPending() {
		return researchLedgerRepository.findByNotifiedBuyOrNotifiedSell(false, false);
	}

	public List<ResearchLedger> buyNotificationPending() {
		return researchLedgerRepository.findByResearchStatusAndNotifiedBuy(ResearchStatus.BUY, false);
	}

	public List<ResearchLedger> sellNotificationPending() {
		return researchLedgerRepository.findByResearchStatusAndNotifiedSell(ResearchStatus.SELL, false);
	}

	public List<ResearchLedger> researchStocksFundamentals() {

		return researchLedgerRepository.findByResearchTypeAndResearchStatus(ResearchType.FUNDAMENTAL,
				ResearchStatus.BUY);
	}

	public List<ResearchLedger> researchStocksTechnicalss() {

		return researchLedgerRepository.findByResearchTypeAndResearchStatus(ResearchType.TECHNICAL, ResearchStatus.BUY);
	}

	/*
	 * public ResearchLedger ledgerDetails(Stock stock) { //return
	 * researchLedgerRepository.findByStockAndActive(stock, true); }
	 */
	@Deprecated
	public boolean isActive(Stock stock) {

		boolean result = false;

		// ResearchLedger ledgerStock =
		// researchLedgerRepository.findByStockAndActive(stock, true);

		/*
		 * if (ledgerStock != null) { result = true; }
		 */

		return result;
	}

	@Deprecated
	public List<ResearchLedger> activeResearchStocks() {

		List<ResearchLedger> activeResearchList = researchLedgerRepository.findAll();

		return activeResearchList;
	}

	@Deprecated
	public List<ResearchLedger> updateDailyResearchListTargetAchived() {

		List<ResearchLedger> researchPickTargetAchived = new ArrayList<>();

		List<ResearchLedger> researchList = activeResearchStocks();

		Set<Stock> sellSignalsResearchSet = new HashSet<>();

		List<Stock> bearishCrossoverList = technicalsResearchService
				.bearishCrossover(undervalueLedgerService.getCurrentUndervalueStocks());

		sellSignalsResearchSet.addAll(bearishCrossoverList);

		List<Stock> uptrendReversalList = technicalsResearchService
				.uptrendReversal(undervalueLedgerService.getCurrentUndervalueStocks());

		sellSignalsResearchSet.addAll(uptrendReversalList);

		List<Stock> yearHighStocks = dylhService.yearHighStocks();

		sellSignalsResearchSet.addAll(yearHighStocks);

		LOGGER.info("START..");

		for (ResearchLedger researchLedger : researchList) {

			if (sellSignalsResearchSet.contains(researchLedger.getStock())) {
				LOGGER.info("TARGET ACHIVED.." + researchLedger.getStock().getNseSymbol() + " Current Price : "
						+ researchLedger.getStock().getStockPrice().getCurrentPrice());
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

	@Deprecated
	public void targetAchived(ResearchLedger researchLedger) {

		if (researchLedger != null) {

			// researchLedger.setActive(false);
			// researchLedger.setTargetDate(LocalDate.now());
		}

		researchLedgerHistoryService.addStock(researchLedger);

		researchLedgerRepository.delete(researchLedger);

	}

	@Deprecated
	public List<Stock> researchNnotificationStocks() {

		List<ResearchLedger> researchLedgerList = researchLedgerRepository.findByNotifiedBuy(true);

		return researchLedgerList.stream().map((rl) -> rl.getStock()).collect(Collectors.toList());
	}

	@Deprecated
	public void researchValueStocks() {
		/*
		 * 
		 * UserProfile user = userService.getUserById(1);
		 * 
		 * Set<Stock> userWatchList = user.getWatchList();
		 * 
		 * //List<Stock> filtereduserWatchList =
		 * ruleService.filterFundamentalStocksWatchList(userWatchList);
		 * 
		 * Set<Stock> buySignalsResearchSet = new HashSet<>();
		 * 
		 * List<Stock> bullishCrossOverList =
		 * technicalsResearchService.bullishCrossOver(undervalueLedgerService.
		 * getCurrentUndervalueStocks());
		 * 
		 * buySignalsResearchSet.addAll(bullishCrossOverList);
		 * 
		 * List<Stock> downtrendReversalList =
		 * technicalsResearchService.downtrendReversal(undervalueLedgerService.
		 * getCurrentUndervalueStocks());
		 * 
		 * buySignalsResearchSet.addAll(downtrendReversalList);
		 * 
		 * // List<Stock> filteredWatchList = new ArrayList<>();
		 * 
		 * buySignalsResearchSet.stream().forEach(s -> { LOGGER.info(" Research on  " +
		 * s.getNseSymbol() + " .. ");
		 * 
		 * if (!isActive(s)) { addStock(s); // filteredWatchList.add(s);
		 * LOGGER.info(" Added to Research " + s.getNseSymbol() + " .. ");
		 * 
		 * } else { LOGGER.info(" Already in Research List  " + s.getNseSymbol() +
		 * " .. ");
		 * 
		 * } }
		 * 
		 * );
		 * 
		 * 
		 * return filteredWatchList.stream()
		 * .sorted(byRoeComparator().thenComparing(byDebtEquityComparator())).limit(
		 * rules.getWatchlistSize()) .collect(Collectors.toList());
		 * 
		 * 
		 */}

	@Deprecated
	private Comparator<Stock> byRoeComparator() {
		return Comparator.comparing(stock -> stock.getStockFactor().getReturnOnEquity(), Comparator.reverseOrder());
	}

	@Deprecated
	private Comparator<Stock> byDebtEquityComparator() {
		return Comparator.comparing(stock -> stock.getStockFactor().getDebtEquity());
	}
}
