package com.example.processors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.StockPrice;
import com.example.model.master.Stock;
import com.example.service.StockService;

@Service
public class RetainMasterRecordsProcessor implements Processor {

	@Autowired
	private StockService stockService;

	@Override
	public void process(Exchange exchange) throws Exception {

		@SuppressWarnings("unchecked")
		List<StockPrice> dailyStockPriceList = (List<StockPrice>) exchange.getIn().getBody();
		
		Set<StockPrice> dailyStockPriceSet = new HashSet<>(dailyStockPriceList);
		
		List<Stock> masterList = stockService.getActiveStocks();

		Set<String> stockMasterNseSymbols = masterList.stream().map(Stock::getNseSymbol).collect(Collectors.toSet());

		
		Set<StockPrice> listOutput = dailyStockPriceSet.stream()
			            .filter(sp -> stockMasterNseSymbols.contains(sp.getNseSymbol()))
			            .collect(Collectors.toSet());
		
		exchange.getOut().setBody(listOutput);

	}
}
