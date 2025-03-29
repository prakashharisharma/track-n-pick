package com.example.external.chyl.service.bl;

import java.io.IOException;
import java.net.MalformedURLException;

import com.example.external.common.ServiceProvider;
import com.example.transactional.model.master.Stock;
import com.example.transactional.model.stocks.StockPriceOld;

public interface CylhBaseService {

	ServiceProvider getServiceProvider();
	
	StockPriceOld getChylPrice(Stock stock) throws MalformedURLException, IOException;
	
	String getServiceUrl(Stock stock);
}
