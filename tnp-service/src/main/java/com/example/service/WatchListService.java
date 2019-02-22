package com.example.service;

import static java.time.temporal.ChronoUnit.DAYS;

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

import com.example.external.chyl.service.CylhService;
import com.example.external.dylh.service.DylhService;
import com.example.external.factor.FactorRediff;
import com.example.model.master.Stock;
import com.example.model.stocks.StockFactor;
import com.example.model.stocks.StockPrice;
import com.example.model.stocks.StockTechnicals;
import com.example.model.um.UserProfile;
import com.example.repo.stocks.StockFactorRepository;
import com.example.repo.stocks.StockPriceRepository;
import com.example.repo.stocks.StockTechnicalsRepository;
import com.example.external.ta.service.TechnicalRatioService;
import com.example.util.MiscUtil;
import com.example.util.rules.RulesFundamental;
import com.example.util.rules.RulesNotification;

@Transactional
@Service
public class WatchListService {

	private static final Logger LOGGER = LoggerFactory.getLogger(WatchListService.class);

	@Autowired
	private CylhService cylhService;

	@Autowired
	private FactorRediff factorRediff;

	@Autowired
	private StockPriceRepository stockPriceRepository;

	@Autowired
	private StockFactorRepository stockFactorRepository;

	@Autowired
	private StockTechnicalsRepository stockTechnicalsRepository;
	
	@Autowired
	private YearLowStocksService yearLowStocksService;

	@Autowired
	private UserService userService;

	@Autowired
	private RulesFundamental rules;

/*	@Autowired
	private TechnicalRatioService technicalRatioService;*/
	
	@Autowired
	private RulesNotification notificationRules;
	
	@Autowired
	private MiscUtil miscUtil;
	@Autowired
	private DylhService dylhService;

	@Autowired
	private RuleService ruleService;
	
	public void updateWatchListPrice(UserProfile user) {

		Set<Stock> watchList = user.getWatchList();
		LOGGER.info("updateWatchListPrice START");
		for (Stock stock : watchList) {

			if (stock.getStockPrice().getLastModified().isBefore(LocalDate.now())) {
				LOGGER.info("Updating Price for : " + stock.getNseSymbol());
				StockPrice stockPrice = cylhService.getChylPrice(stock);

				stockPriceRepository.save(stockPrice);
			}

		}
		LOGGER.info("updateWatchListPrice END");
	}

	public void updateWatchListFactor(UserProfile user) {

		Set<Stock> watchList = user.getWatchList();
		LOGGER.info("updateWatchListFactor START");
		for (Stock stock : watchList) {

			if (DAYS.between(stock.getStockFactor().getLastModified(), LocalDate.now()) > notificationRules
					.getFactorIntervalDays()) {

				try {
					miscUtil.delay();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				LOGGER.info("Updating Factor for : " + stock.getNseSymbol());

				StockFactor stockFactor = factorRediff.getFactor(stock);

				stockFactorRepository.save(stockFactor);

				
				
			}
		}
		LOGGER.info("updateWatchListFactor END");
	}
	
	
	
	public void updateWatchListAddStocks(UserProfile user) {

		List<Stock> stockLiost = yearLowStocksService.yearLowStocks();

		if (!stockLiost.isEmpty()) {
			user = userService.addtoWatchList(user, stockLiost);
		} else {
			LOGGER.info("NO QUALITY STOCK TO ADD ..");
		}

	}

	public void updateWatchListRemoveStocks(UserProfile user) {

		Set<Stock> watchListAll = user.getWatchList();

		List<Stock> fundamentalList = ruleService.filterFundamentalStocks(watchListAll);
		
		List<Stock> tobeRemovedfromWatchList = new ArrayList<>(watchListAll);
		
		tobeRemovedfromWatchList.removeAll(fundamentalList);

		//
		List<Stock> yearHighStocks = dylhService.yearHighStocks();
		tobeRemovedfromWatchList.removeAll(yearHighStocks);
		//
		LOGGER.info("TOBE REMOVED FROM WATCHLIST ..");
		tobeRemovedfromWatchList.forEach(System.out::println);
		LOGGER.info("REMOVED FROM WATCHLIST ..");
		userService.removeFromtoWatchList(user, tobeRemovedfromWatchList);
	}

	// Return true if c is between a and b.
	public static boolean isBetween(double a, double b, double c) {
		return b > a ? c > a && c < b : c > b && c < a;
	}


	public List<Stock> userWatchList(UserProfile user) {

		Set<Stock> watchList = user.getWatchList();

		
		return  watchList.stream()
				.sorted(byRoeComparator().thenComparing(byDebtEquityComparator())).collect(Collectors.toList());

	}

	private Comparator<Stock> byRoeComparator() {
		return Comparator.comparing(stock -> stock.getStockFactor().getReturnOnEquity(), Comparator.reverseOrder());
	}

	private Comparator<Stock> byDebtEquityComparator() {
		return Comparator.comparing(stock -> stock.getStockFactor().getDebtEquity());
	}

}
