package com.example.integration.processors.master;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.integration.model.StockPriceIN;
import com.example.integration.service.RestClientService;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.util.io.model.StockIO;
import com.example.util.io.model.StockPriceIO;
import com.example.util.io.model.StockIO.IndiceType;

@Service
public class UpdateNifty750Processor implements Processor{

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNifty750Processor.class);

	@Autowired
	private QueueService queueService;
	
	@Autowired
	private RestClientService restClientService;
	
	private static List<StockPriceIO> nift500List ;
	
	@Override
	public void process(Exchange exchange) throws Exception {

		LOGGER.info("UpdateNifty750Processor : START");
		
		nift500List = restClientService.getNift500();
		
		@SuppressWarnings("unchecked")
		List<StockPriceIN> dailyStockPriceList = (List<StockPriceIN>) exchange.getIn().getBody();
		
		List<StockPriceIN> dailyStockPriceList750  = dailyStockPriceList.stream().filter(sp -> sp.getSeries().equalsIgnoreCase("EQ")).filter( sp -> sp.getTottrdval() > 1000000).filter(sp -> sp.getClose() >= 50.0 ).collect(Collectors.toList());
		
		dailyStockPriceList750 = dailyStockPriceList750.stream().filter(filterNift500Stocks()).collect(Collectors.toList());
		
		dailyStockPriceList750  = dailyStockPriceList750.stream().sorted(Comparator.comparingDouble(StockPriceIN::getClose).reversed()).limit(250).collect(Collectors.toList());
		
		//dailyStockPriceList750.forEach(System.out::println);
		
		dailyStockPriceList750.stream().forEach( sp -> {
			
			StockIO stockIO = new StockIO(sp.getNseSymbol(), "NSE750", sp.getNseSymbol(), sp.getSeries(), sp.getIsin(), IndiceType.NIFTY750);
			
			LOGGER.debug("UpdateNifty750Processor : Queuing to update master " + stockIO);
			
			queueService.send(stockIO, QueueConstants.MTQueue.UPDATE_MASTER_STOCK_QUEUE);
			
		});
		
	}
	
	public static Predicate<StockPriceIN> filterNift500Stocks() 
	{
	    return p -> !nift500List.contains(new StockPriceIO(p.getNseSymbol(), p.getIsin()));
	}
}
