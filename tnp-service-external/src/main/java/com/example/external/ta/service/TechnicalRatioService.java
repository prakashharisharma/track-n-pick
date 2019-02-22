package com.example.external.ta.service;

import com.example.model.master.Stock;
import com.example.model.stocks.StockTechnicals;

public interface TechnicalRatioService {
	
	public StockTechnicals retrieveTechnicals(Stock stock);

	double get50SMA(Stock stock);

	double get200SMA(Stock stock);

	double getRSI(Stock stock);

}
