package com.example.processors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.integration.service.StorageQueueService;
import com.example.model.StockPrice;

@Service
public class TempStoragePriceUpdateProcessor implements Processor {

	private static final Logger LOGGER = LoggerFactory.getLogger(TempStoragePriceUpdateProcessor.class);

	@Autowired
	private StorageQueueService storageQueueService;

	@Override
	public void process(Exchange exchange) throws Exception {
		LOGGER.info("TEMPSTORAGE MASTER RECORD FROM BHAV FILE START");

		@SuppressWarnings("unchecked")
		List<StockPrice> dailyStockPriceList = (List<StockPrice>) exchange.getIn().getBody();

		Set<StockPrice> dailyStockPriceSet = new HashSet<>(dailyStockPriceList);

		//
		dailyStockPriceSet.stream().filter(sp -> sp.getSeries().equalsIgnoreCase("EQ") && sp.getClose() > 10.0)
				.forEach(sp -> {
					storageQueueService.processPrce(sp);

				});
		//
		exchange.getOut().setBody("TEMPSTORAGE BODY");
		LOGGER.info("TEMPSTORAGE MASTER RECORD FROM BHAV FILE END");
	}

}
