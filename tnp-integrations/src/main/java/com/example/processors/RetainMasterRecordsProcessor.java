package com.example.processors;

import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.integration.service.StorageQueueService;
import com.example.model.StockPrice;
import com.example.model.master.Stock;
import com.example.service.CalendarService;
import com.example.service.StockService;
import com.example.storage.model.TradingSession;
import com.example.storage.repo.StorageTemplate;

@Service
public class RetainMasterRecordsProcessor implements Processor {

	private static final Logger LOGGER = LoggerFactory.getLogger(RetainMasterRecordsProcessor.class);
	
	@Autowired
	private StockService stockService;

	@Autowired
	private StorageQueueService storageQueueService;
	@Autowired
	private StorageTemplate storageTemplate;
	@Autowired
	private CalendarService calendarService;
	
	@Override
	public void process(Exchange exchange) throws Exception {

		LOGGER.info("FILTERING MASTER RECORD FROM BHAV FILE START");
		
		@SuppressWarnings("unchecked")
		List<StockPrice> dailyStockPriceList = (List<StockPrice>) exchange.getIn().getBody();
		
		Set<StockPrice> dailyStockPriceSet = new HashSet<>(dailyStockPriceList);
		
		//
		dailyStockPriceSet.stream().filter(sp -> sp.getSeries().equalsIgnoreCase("EQ") && sp.getClose() > 10.0).forEach( sp -> {
			storageQueueService.processPrce(sp);
			
		});
		//
		
		storageTemplate.createTS(new TradingSession(calendarService.previousWorkingDay().atStartOfDay().toInstant(ZoneOffset.UTC)));
		
		List<Stock> masterList = stockService.getActiveStocks();

		Set<String> stockMasterNseSymbols = masterList.stream().map(Stock::getNseSymbol).collect(Collectors.toSet());

		Set<StockPrice> listOutput = dailyStockPriceSet.stream()
			            .filter(sp -> stockMasterNseSymbols.contains(sp.getNseSymbol()))
			            .collect(Collectors.toSet());
		
		exchange.getOut().setBody(listOutput);
		LOGGER.info("FILTERING MASTER RECORD FROM BHAV FILE END");
	}
}
