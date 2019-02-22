package com.example.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CleanseService {

	private static List<String> nifty500Stocks = new ArrayList<>();

	@Autowired
	private StockService stockService;
	
	public boolean isNifty500(String nseSymbol) {

		boolean isNnifty500 = false;

		if (nifty500Stocks.isEmpty()) {
			nifty500Stocks.addAll(stockService.getActiveStocks().stream().map(stock -> stock.getNseSymbol())
					.collect(Collectors.toList()));
		}

		if (nifty500Stocks.contains(nseSymbol)) {
			isNnifty500 = true;
		}

		return isNnifty500;
	}

}
