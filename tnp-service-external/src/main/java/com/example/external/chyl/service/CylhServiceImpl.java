package com.example.external.chyl.service;

import java.io.IOException;
import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.external.chyl.service.bl.CylhBaseService;
import com.example.external.common.ServiceProvider;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;

/**
 * This service will return CYLH (Current Year Low High Price) data.
 * 
 * @author phsharma
 *
 */
@Service
class CylhServiceImpl implements CylhService {

	@Autowired
	private CylhServiceFactory chylServiceFactory;

	private static final Logger LOGGER = LoggerFactory.getLogger(CylhServiceImpl.class);

	public StockPrice getChylPrice(Stock stock) {

		//CylhBaseService baseChylService = chylServiceFactory.getService(ServiceProvider.randomServiceProvider());
		CylhBaseService baseChylService = chylServiceFactory.getService(ServiceProvider.STORAGE);

		LOGGER.debug(baseChylService.getServiceProvider().toString());

		StockPrice stockPrice = null;

		try {

			stockPrice = baseChylService.getChylPrice(stock);

		} catch (MalformedURLException me) {

			try {
				stockPrice = getNSEChylPrice(stock);
			} catch (IOException e) {
				stockPrice = getStorageChylPrice(stock);
				e.printStackTrace();
			}

		} catch (IOException e) {
			stockPrice = getStorageChylPrice(stock);
			e.printStackTrace();
		}

		return stockPrice;
	}

	private StockPrice getNSEChylPrice(Stock stock) throws IOException {

		CylhBaseService baseChylService = chylServiceFactory.getService(ServiceProvider.NSE);

		LOGGER.debug(baseChylService.getServiceProvider().toString());

		return baseChylService.getChylPrice(stock);
	}

	private StockPrice getStorageChylPrice(Stock stock){

		CylhBaseService baseChylService = chylServiceFactory.getService(ServiceProvider.STORAGE);

		LOGGER.debug(baseChylService.getServiceProvider().toString());

		StockPrice stockPrice = null;
		
		try {
			stockPrice = baseChylService.getChylPrice(stock);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return stockPrice;
	}

}
