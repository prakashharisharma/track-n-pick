package com.example.factor;

import java.io.IOException;
import java.net.MalformedURLException;

import com.example.common.FactorProvider;
import com.example.model.master.Stock;
import com.example.model.stocks.StockFactor;

public interface FactorBaseService {

	FactorProvider getServiceProvider();
	
	StockFactor getFactor(Stock stock) throws MalformedURLException, IOException;
	
}
