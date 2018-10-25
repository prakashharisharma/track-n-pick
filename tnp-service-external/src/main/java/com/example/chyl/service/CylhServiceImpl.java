package com.example.chyl.service;

import java.io.IOException;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.chyl.service.bl.CylhBaseService;
import com.example.common.ServiceProvider;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
/**
 * This service will return CYLH (Current Year Low High Price) data.
 * 
 * @author phsharma
 *
 */
@Service
class CylhServiceImpl implements CylhService{

	@Autowired
	private CylhServiceFactory chylServiceFactory;

	private static final Logger LOGGER = LoggerFactory.getLogger(CylhServiceImpl.class);

	public StockPrice getChylPrice(Stock stock) {
		
		CylhBaseService baseChylService = chylServiceFactory.getService(ServiceProvider.randomServiceProvider());
	
		LOGGER.debug(baseChylService.getServiceProvider().toString());
		
		StockPrice stockPrice = null;
		
		try {
			
			stockPrice = baseChylService.getChylPrice(stock);
			
		}catch(MalformedURLException me) {
			
			try {
				stockPrice = getNSEChylPrice(stock);
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
		return stockPrice;
	}

	private StockPrice getNSEChylPrice(Stock stock) throws IOException {
		
		CylhBaseService baseChylService = chylServiceFactory.getService(ServiceProvider.NSE);
	
		LOGGER.debug(baseChylService.getServiceProvider().toString());
		
		return baseChylService.getChylPrice(stock);
	}
	
	
}
