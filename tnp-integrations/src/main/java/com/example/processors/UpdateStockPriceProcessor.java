package com.example.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.StockPrice;
import com.example.model.master.Stock;
import com.example.service.StockService;

@Service
public class UpdateStockPriceProcessor implements Processor {

	@Autowired
	private StockService stockService;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		
		Thread.sleep(100);
		
		StockPrice stockPrice = (StockPrice) exchange.getIn().getBody();
		
		Stock stock = stockService.getStockByNseSymbol(stockPrice.getNseSymbol());
		
		stockService.updateCurrentPrice(stock, Double.parseDouble(stockPrice.getClose()));
		
		System.out.println("PRICE UPDATED " + stockPrice);
	}

}
