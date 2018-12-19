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
import com.example.model.um.User;
import com.example.repo.stocks.StockFactorRepository;
import com.example.repo.stocks.StockPriceRepository;
import com.example.util.Rules;

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
	private YearLowStocksService yearLowStocksService;

	@Autowired
	private UserService userService;

	@Autowired
	private Rules rules;

	private void updateWatchListPrice(User user) {

		Set<Stock> watchList = user.getWatchList();
		LOGGER.info("updateDailyWatchListPrice START");
		for (Stock stock : watchList) {

			if (stock.getStockPrice().getLastModified().isBefore(LocalDate.now())) {
				LOGGER.info("Updating Price for : " + stock.getNseSymbol());
				StockPrice stockPrice = cylhService.getChylPrice(stock);

				stockPriceRepository.save(stockPrice);
			}

			if (DAYS.between(stock.getStockFactor().getLastModified(), LocalDate.now()) > rules
					.getFactorIntervalDays()) {

				LOGGER.info("Updating Factor for : " + stock.getNseSymbol());

				StockFactor stockFactor = factorRediff.getFactor(stock);

				stockFactorRepository.save(stockFactor);

			}
		}
		LOGGER.info("updateDailyWatchListPrice END");
	}

	public void updateWatchList(User user) {

		List<Stock> stockLiost = yearLowStocksService.yearLowStocks();

		if (!stockLiost.isEmpty()) {
			user = userService.addtoWatchList(user, stockLiost);
		} else {
			LOGGER.info("NO QUALITY STOCK TO ADD ..");
		}

		updateWatchListPrice(user);
	}

	public void updateMonthlyWatchListRemoveStocks(User user) {

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


	public List<Stock> userWatchList(User user) {

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
