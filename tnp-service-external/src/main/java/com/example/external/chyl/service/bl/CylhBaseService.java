package com.example.external.chyl.service.bl;

import java.io.IOException;
import java.net.MalformedURLException;

import com.example.external.common.ServiceProvider;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;

public interface CylhBaseService {

	ServiceProvider getServiceProvider();
	
	StockPrice getChylPrice(Stock stock) throws MalformedURLException, IOException;
	
	String getServiceUrl(Stock stock);
}
