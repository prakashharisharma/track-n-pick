package com.example.external.dylh.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.external.dylh.model.NseResponseData;
import com.example.model.master.Stock;

@Service
class DylhServiceImpl implements DylhService{

	private static final String NSE_DAILY_YEAR_LOW_URL = "https://www.nseindia.com/products/dynaContent/equities/equities/json/online52NewLow.json";
	
	private static final String NSE_DAILY_YEAR_HIGH_URL = "https://www.nseindia.com/products/dynaContent/equities/equities/json/online52NewHigh.json";
	
	@Override
	public List<Stock> yearLowStocks() {
		
		RestTemplate restTemplate = new RestTemplate();

		// Send request with GET method and default Headers.
		NseResponseData data = restTemplate.getForObject(NSE_DAILY_YEAR_LOW_URL, NseResponseData.class);

		if (data != null) {

			List<Stock> stockList = data.getStockList();
			
			if(stockList != null) {
				return stockList;
			}
		}
		
		return null;
	}

	@Override
	public List<Stock> yearHighStocks() {
		
		RestTemplate restTemplate = new RestTemplate();

		// Send request with GET method and default Headers.
		NseResponseData data = restTemplate.getForObject(NSE_DAILY_YEAR_HIGH_URL, NseResponseData.class);

		if (data != null) {

			List<Stock> stockList = data.getStockList();
			
			if(stockList != null) {
				return stockList;
			}
		}
		
		return null;
	}

}
