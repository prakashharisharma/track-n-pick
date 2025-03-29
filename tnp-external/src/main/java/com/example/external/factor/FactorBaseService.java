package com.example.external.factor;

import java.io.IOException;
import java.net.MalformedURLException;

import com.example.external.common.FactorProvider;
import com.example.transactional.model.master.Stock;
import com.example.transactional.model.stocks.StockFactor;

public interface FactorBaseService {

	FactorProvider getServiceProvider();
	
	StockFactor getFactor(Stock stock) throws MalformedURLException, IOException;
	
}
