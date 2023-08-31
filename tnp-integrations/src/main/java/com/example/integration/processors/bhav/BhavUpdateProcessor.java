package com.example.integration.processors.bhav;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.integration.model.StockPriceIN;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.util.io.model.StockPriceIO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BhavUpdateProcessor implements Processor {

	//private static final Logger LOGGER = LoggerFactory.getLogger(BhavUpdateProcessor.class);

	@Autowired
	private QueueService queueService;
	
	@Override
	public void process(Exchange exchange) throws Exception {

		log.info("Starting Bhav Processor");
		
		@SuppressWarnings("unchecked")
		List<StockPriceIN> dailyStockPriceList = (List<StockPriceIN>) exchange.getIn().getBody();
		
		dailyStockPriceList = dailyStockPriceList.stream().filter(sp -> sp.getSeries().equalsIgnoreCase("EQ")).collect(Collectors.toList());
		
		Set<StockPriceIN> dailyStockPriceSet = new HashSet<>(dailyStockPriceList);
		

		dailyStockPriceSet.stream().filter(sp -> sp.getSeries().equalsIgnoreCase("EQ")).forEach( sp -> {
			
			StockPriceIO stockPriceIO = new StockPriceIO(sp.getNseSymbol(), sp.getSeries(), sp.getOpen(), sp.getHigh(), sp.getLow(), sp.getClose(), sp.getLast(), sp.getPrevClose(), sp.getTottrdqty(), sp.getTottrdval(), sp.getTimestamp(), sp.getTotaltrades(), sp.getIsin());
			
			log.debug("Queuing to update price Stock:{}" + stockPriceIO.getNseSymbol());

			/* Store Bhav for analytics purpose, Not needed for now
			queueService.send(stockPriceIO, QueueConstants.HistoricalQueue.UPDATE_BHAV_QUEUE);
			*/

			queueService.send(stockPriceIO, QueueConstants.HistoricalQueue.UPDATE_PRICE_QUEUE);
			
		});
		
		log.info("Completed Bhav Processor");
		
	}
}
