package com.example.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.StockPrice;
import com.example.storage.service.StorageService;

@Service
public class UpdateStoragePriceProcessor implements Processor{

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateStoragePriceProcessor.class);

	@Autowired
	private StorageService storageService;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		StockPrice stockPrice = (StockPrice) exchange.getIn().getBody();

		storageService.updatePrice(stockPrice.getNseSymbol(), stockPrice.getOpen(), stockPrice.getHigh(), stockPrice.getLow(), stockPrice.getClose(), stockPrice.getLast(), stockPrice.getPrevClose(), stockPrice.getTottrdqty(), stockPrice.getTottrdval(), stockPrice.getTotaltrades(), stockPrice.getTimestamp());
		
		LOGGER.info("ADDED PRICE DATA :" + stockPrice.getNseSymbol());
		
		Thread.sleep(50);
	}

}
