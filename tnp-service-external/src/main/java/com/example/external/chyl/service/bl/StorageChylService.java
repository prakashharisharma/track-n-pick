package com.example.external.chyl.service.bl;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.external.common.ServiceProvider;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;

/**
 * 
 * http://www.mystocks.co.in/stocks/HDIL.html
 * 
 * @author phsharma
 *
 */

//@Service
public class StorageChylService implements CylhBaseService {

	private static final Logger LOGGER = LoggerFactory.getLogger(StorageChylService.class);
	
	/*@Autowired
	private StorageService storageService;
	*/
	@Override
	public ServiceProvider getServiceProvider() {
		// TODO Auto-generated method stub
		return ServiceProvider.STORAGE;
	}

	@Override
	public StockPrice getChylPrice(Stock stock){
		LOGGER.debug("Inside " + getServiceProvider().toString() +":"+stock.getNseSymbol());

		StockPrice stockPrice = stock.getStockPrice();

		if (stockPrice == null) {

			stockPrice = new StockPrice();
			
		} 
		
		stockPrice.setLastModified(LocalDate.now());
		/*stockPrice.setCurrentPrice(storageService.getCurrentPrice(stock.getNseSymbol()));
		stockPrice.setYearHigh(storageService.getyearHigh(stock.getNseSymbol()));

		stockPrice.setYearLow(storageService.getyearLow(stock.getNseSymbol()));*/
		
		return stockPrice;
	}

	@Override
	public String getServiceUrl(Stock stock) {
		// TODO Auto-generated method stub
		return "NO_URL";
	}

}
