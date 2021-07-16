package com.example.service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.model.master.Stock;
import com.example.model.um.UserProfile;

@Transactional
@Service
public class WatchListService {

	private static final Logger LOGGER = LoggerFactory.getLogger(WatchListService.class);

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
