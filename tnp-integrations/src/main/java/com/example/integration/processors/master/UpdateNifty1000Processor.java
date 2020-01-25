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
import com.example.util.io.model.UpdateTriggerIO;
import com.example.util.io.model.StockIO.IndiceType;
import com.example.util.io.model.StockPriceIO;
import com.example.util.io.model.UpdateTriggerIO.TriggerType;

@Service
public class UpdateNifty1000Processor implements Processor{

	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNifty1000Processor.class);

	@Autowired
	private QueueService queueService;
	
	@Autowired
	private RestClientService restClientService;
	
	private static List<StockPriceIO> nift500List ;
	
	@Override
	public void process(Exchange exchange) throws Exception {

		LOGGER.info("UpdateNifty1000Processor : START");
		
		nift500List = restClientService.getNift500();
		
		@SuppressWarnings("unchecked")
		List<StockPriceIN> dailyStockPriceList = (List<StockPriceIN>) exchange.getIn().getBody();
		
		List<StockPriceIN> dailyStockPriceList1000  = dailyStockPriceList.stream().filter(sp -> sp.getSeries().equalsIgnoreCase("EQ")).filter( sp -> sp.getTottrdval() > 1000000).filter(sp -> sp.getClose() >= 50.0 ).collect(Collectors.toList());
		
		dailyStockPriceList1000 = dailyStockPriceList1000.stream().filter(filterNift500Stocks()).collect(Collectors.toList());
		
		dailyStockPriceList1000  = dailyStockPriceList1000.stream().sorted(Comparator.comparingDouble(StockPriceIN::getClose).reversed()).limit(500).collect(Collectors.toList());
		
		//dailyStockPriceList1000.forEach(System.out::println);
		
		dailyStockPriceList1000.stream().forEach( sp -> {
			
			StockIO stockIO = new StockIO(sp.getNseSymbol(), "NSE1000", sp.getNseSymbol(), sp.getSeries(), sp.getIsin(), IndiceType.NIFTY1000);
			
			LOGGER.debug("UpdateNifty1000Processor : Queuing to update master " + stockIO);
			
			queueService.send(stockIO, QueueConstants.MTQueue.UPDATE_MASTER_STOCK_QUEUE);
			
		});
		
	}
	
	public static Predicate<StockPriceIN> filterNift500Stocks() 
	{
	    return p -> !nift500List.contains(new StockPriceIO(p.getNseSymbol(), p.getIsin()));
	}

}
