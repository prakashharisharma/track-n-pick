package com.example.external.factor;

import java.io.IOException;
import java.net.MalformedURLException;

import com.example.external.common.FactorProvider;
import com.example.data.transactional.entities.Stock;
import com.example.data.transactional.entities.StockFactor;


public interface FactorBaseService {

	FactorProvider getServiceProvider();
	
	StockFactor getFactor(Stock stock) throws MalformedURLException, IOException;
	
}
