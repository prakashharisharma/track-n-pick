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

import com.example.chyl.service.CylhService;
import com.example.factor.FactorRediff;
import com.example.model.master.Stock;
import com.example.model.stocks.StockFactor;
import com.example.model.stocks.StockPrice;
import com.example.model.stocks.StockTechnicals;
import com.example.model.um.UserProfile;
import com.example.repo.stocks.StockFactorRepository;
import com.example.repo.stocks.StockPriceRepository;
import com.example.repo.stocks.StockTechnicalsRepository;
import com.example.ta.service.TechnicalRatioService;
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

	@Autowired
	private TechnicalRatioService technicalRatioService;
	
	@Autowired
	private RulesNotification notificationRules;
	
	@Autowired
	private MiscUtil miscUtil;
	
	public void updateWatchListPriceAndFactor(UserProfile user) {

		Set<Stock> watchList = user.getWatchList();
		LOGGER.info("updateDailyWatchListPrice START");
		for (Stock stock : watchList) {

			if (stock.getStockPrice().getLastModified().isBefore(LocalDate.now())) {
				LOGGER.info("Updating Price for : " + stock.getNseSymbol());
				StockPrice stockPrice = cylhService.getChylPrice(stock);

				stockPriceRepository.save(stockPrice);
			}

			if (DAYS.between(stock.getStockFactor().getLastModified(), LocalDate.now()) > notificationRules
					.getFactorIntervalDays()) {

				LOGGER.info("Updating Factor for : " + stock.getNseSymbol());

				StockFactor stockFactor = factorRediff.getFactor(stock);

				stockFactorRepository.save(stockFactor);

			}
		}
		LOGGER.info("updateDailyWatchListPrice END");
	}

	public void updateWatchListStockTechnicals(UserProfile user) {
		
		Set<Stock> watchList = user.getWatchList().stream().limit(10).collect(Collectors.toSet());
		
		for (Stock stock : watchList) {
			
			StockTechnicals stockTechnicals = technicalRatioService.retrieveTechnicals(stock);
			
			System.out.println(stockTechnicals);
			
			stockTechnicalsRepository.save(stockTechnicals);
			
			try {
				miscUtil.delay();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public void updateWatchListAddStocks(UserProfile user) {

		List<Stock> stockLiost = yearLowStocksService.yearLowStocks();

		if (!stockLiost.isEmpty()) {
			user = userService.addtoWatchList(user, stockLiost);
		} else {
			LOGGER.info("NO QUALITY STOCK TO ADD ..");
		}

	}

	public void updateMonthlyWatchListRemoveStocks(UserProfile user) {

		Set<Stock> stockList = user.getWatchList();

		List<Stock> tobeRemovedfromWatchList = new ArrayList<>();

		for (Stock stock : stockList) {

			StockPrice stockPrice = stock.getStockPrice();

			double currentPrice = stockPrice.getCurrentPrice();

			double yearHigh = stockPrice.getYearHigh();

			double per_10 = stockPrice.getYearHigh() * 0.10;

			if (isBetween((yearHigh - per_10), (yearHigh + per_10), currentPrice)) {
				tobeRemovedfromWatchList.add(stock);
			}

		}

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
