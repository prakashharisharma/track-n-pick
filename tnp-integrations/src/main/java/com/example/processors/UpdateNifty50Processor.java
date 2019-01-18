package com.example.processors;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.StockMaster;
import com.example.model.master.Stock;
import com.example.service.SectorService;
import com.example.service.StockService;
import com.example.storage.service.StorageService;

@Service
public class UpdateNifty50Processor implements Processor {

private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNifty50Processor.class);
	
	@Autowired
	private StockService stockService;
	
	@Autowired
	private SectorService sectorService;
	
	@Autowired
	private StorageService storageService;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		LOGGER.info("FILTERING NIFTY50 RECORD NSE MASTER");
		
		@SuppressWarnings("unchecked")
		List<StockMaster> nseNifty50List = (List<StockMaster>) exchange.getIn().getBody();
		LOGGER.info("NSE NIFTY50");
		nseNifty50List.forEach(System.out::println);
		
		List<Stock> existingNifty50List = stockService.getNifty50ActiveStocks();
		LOGGER.info("EXISTING NIFTY50");
		existingNifty50List.forEach(System.out::println);
		
		Set<String> nseSymbols = existingNifty50List.stream().map(Stock::getNseSymbol).collect(Collectors.toSet());

		Set<String> existingSymbols = nseNifty50List.stream().map(StockMaster::getNseSymbol).collect(Collectors.toSet());
		
		List<Stock> discontinueList = existingNifty50List.stream()
	            .filter(s -> !existingSymbols.contains(s.getNseSymbol()))
	            .collect(Collectors.toList());
		LOGGER.info("DISCONTINUE NIFTY 50");
		
		discontinueList.forEach(System.out::println);
		
		this.discontinue(discontinueList);
		
		List<StockMaster> newAdditionList = nseNifty50List.stream()
	            .filter(sm -> !nseSymbols.contains(sm.getNseSymbol()))
	            .collect(Collectors.toList());
		
		LOGGER.info("NEW NIFTY 50");
		
		newAdditionList.forEach(System.out::println);
		
		this.addToMaster(newAdditionList);
	}

	private void discontinue(List<Stock> discontinueList) {
		stockService.setNifty50Inactive(discontinueList);
	}
	
	private void addToMaster(List<StockMaster> newAdditionList) {
		
		for (StockMaster stockMaster :newAdditionList ) {
			
			Stock stock = stockService.getStockByNseSymbol(stockMaster.getNseSymbol());
			
			stockService.setNifty50(stock);
			
			LOGGER.info("ADDED TO NIFTY50" + stock);
		}
	}
	
}
