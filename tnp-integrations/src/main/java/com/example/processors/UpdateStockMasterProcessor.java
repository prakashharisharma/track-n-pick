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
public class UpdateStockMasterProcessor implements Processor{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateStockMasterProcessor.class);
	
	@Autowired
	private StockService stockService;
	
	@Autowired
	private SectorService sectorService;
	
	@Autowired
	private StorageService storageService;
	
	
	@Override
	public void process(Exchange exchange) throws Exception {
		LOGGER.info("FILTERING MASTER RECORD NSE MASTER");
		
		@SuppressWarnings("unchecked")
		List<StockMaster> nseMasterList = (List<StockMaster>) exchange.getIn().getBody();
		LOGGER.info("NSE MASTER");
		nseMasterList.forEach(System.out::println);
		
		List<Stock> existingMasterList = stockService.getActiveStocks();
		LOGGER.info("EXISTING MASTER");
		existingMasterList.forEach(System.out::println);
		
		Set<String> nseSymbols = existingMasterList.stream().map(Stock::getNseSymbol).collect(Collectors.toSet());

		Set<String> existingSymbols = nseMasterList.stream().map(StockMaster::getNseSymbol).collect(Collectors.toSet());
		
		List<Stock> discontinueList = existingMasterList.stream()
	            .filter(s -> !existingSymbols.contains(s.getNseSymbol()))
	            .collect(Collectors.toList());
		LOGGER.info("DISCONTINUE");
		
		discontinueList.forEach(System.out::println);
		
		this.discontinue(discontinueList);
		
		List<StockMaster> newAdditionList = nseMasterList.stream()
	            .filter(sm -> !nseSymbols.contains(sm.getNseSymbol()))
	            .collect(Collectors.toList());
		
		LOGGER.info("NEW");
		
		newAdditionList.forEach(System.out::println);
		
		this.addToMaster(newAdditionList);
	}

	private void discontinue(List<Stock> discontinueList) {
		stockService.setInactive(discontinueList);
	}
	
	private void addToMaster(List<StockMaster> newAdditionList) {
		for (StockMaster stockMaster :newAdditionList ) {
			Stock stock = stockService.add(stockMaster.getIsin(), stockMaster.getCompanyName(), stockMaster.getNseSymbol(), sectorService.getOrAddSectorByName(stockMaster.getSector()));
			
			storageService.addStock(stockMaster.getIsin(), stockMaster.getCompanyName(), stockMaster.getNseSymbol(), null ,stockMaster.getSector());
			
			LOGGER.info("ADDED " + stock);
		}
	}
}
