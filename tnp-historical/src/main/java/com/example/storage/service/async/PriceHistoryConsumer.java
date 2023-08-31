package com.example.storage.service.async;

import java.time.LocalDate;

import javax.jms.Session;

import org.apache.activemq.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.storage.model.StockPrice;
import com.example.storage.model.result.HighLowResult;
import com.example.storage.repo.PriceTemplate;
import com.example.storage.service.StorageService;
import com.example.util.io.model.StockPriceIO;

@Component
public class PriceHistoryConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(PriceHistoryConsumer.class);

	//@Autowired
	//private StorageService storageService;

	@Autowired
	private PriceTemplate priceTemplate;

	@Autowired
	private QueueService queueService;
	
	@JmsListener(destination = QueueConstants.HistoricalQueue.UPDATE_PRICE_QUEUE)
	public void receiveMessage(@Payload StockPriceIO stockPriceIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {

		LOGGER.debug(QueueConstants.HistoricalQueue.UPDATE_PRICE_QUEUE.toUpperCase() +" : " + stockPriceIO.getNseSymbol() +" : START");

		// To Be Removed In Future
		//storageService.updatePrice(stockPriceIO.getNseSymbol(), stockPriceIO.getOpen(), stockPriceIO.getHigh(), stockPriceIO.getLow(), stockPriceIO.getClose(), stockPriceIO.getLast(), stockPriceIO.getPrevClose(), stockPriceIO.getTottrdqty(), stockPriceIO.getTottrdval(), stockPriceIO.getTotaltrades(), stockPriceIO.getBhavDate());
		//
		
		
		LOGGER.trace(QueueConstants.HistoricalQueue.UPDATE_PRICE_QUEUE.toUpperCase() +" : " + stockPriceIO.getNseSymbol() +" : updating Price History..");
		
		StockPrice stockPriceN = new StockPrice(stockPriceIO.getNseSymbol(), stockPriceIO.getOpen(), stockPriceIO.getHigh(),
				stockPriceIO.getLow(), stockPriceIO.getClose(),  stockPriceIO.getPrevClose(),
				 stockPriceIO.getBhavDate());


		HighLowResult highLowResult = priceTemplate.getHighLowByDate(stockPriceIO.getNseSymbol(), LocalDate.now().minusWeeks(52));
		
		double yearLow = 0.00;
		
		double yearHigh = 0.00;
		
		if(highLowResult != null && !highLowResult.get_id().equalsIgnoreCase("NO_DATA_FOUND")) {
		
		yearLow = highLowResult.getLow();
		
		yearHigh = highLowResult.getHigh();
		}else {
			yearLow = stockPriceIO.getLow();
			yearHigh = stockPriceIO.getHigh();
		}

		
		if (yearLow > stockPriceIO.getLow()) {
			yearLow = stockPriceIO.getLow();
			
		}
		
		stockPriceIO.setYearLow(yearLow);
		
		if (yearHigh < stockPriceIO.getHigh()) {
			yearHigh = stockPriceIO.getHigh();
		
		}
		stockPriceIO.setYearHigh(yearHigh);
		
		stockPriceN.setYearLow(yearLow);

		stockPriceN.setYearHigh(yearHigh);

		highLowResult = priceTemplate.getHighLowByDays(stockPriceIO.getNseSymbol(), 14);
		
		double low14 = 0.00;
		
		double high14 = 0.00;
		
		if(highLowResult != null && !highLowResult.get_id().equalsIgnoreCase("NO_DATA_FOUND")) {
		
		low14 = highLowResult.getLow();
		
		high14 = highLowResult.getHigh();
		
		}else {
			low14 = stockPriceIO.getLow();
			
		}
		
		
		if (low14 > stockPriceIO.getLow()) {
			
			low14 = stockPriceIO.getLow();
			high14 = stockPriceIO.getHigh();
		}
		
		stockPriceIO.setLow14(low14);
		
		stockPriceN.setLow14(low14);
		
		if (high14 < stockPriceIO.getHigh()) {
			
			high14 = stockPriceIO.getHigh();
		
		}
		
		stockPriceIO.setHigh14(high14);
		
		stockPriceN.setHigh14(high14);
		
		
		if(stockPriceIO.getTottrdqty() < 1000) {
			stockPriceIO.setTottrdqty(1);
			//stockPriceN.setTotalTradedQuantity(1);
		}else {
			long volumne = stockPriceIO.getTottrdqty() / 1000;
			
			stockPriceIO.setTottrdqty(volumne);
			//stockPriceN.setTotalTradedQuantity(volumne);
		}
		
		
		priceTemplate.create(stockPriceN);

		LOGGER.trace(QueueConstants.HistoricalQueue.UPDATE_PRICE_QUEUE.toUpperCase() +" : " + stockPriceIO.getNseSymbol() +" : Queuing to update Technicals..");
		
		///Thread.sleep(50);
		
		//queueService.send(stockPriceIO, QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE);
		
		LOGGER.trace(QueueConstants.HistoricalQueue.UPDATE_PRICE_QUEUE.toUpperCase() +" : " + stockPriceIO.getNseSymbol() +" : Queuing to update Transactional Price.. ");
		
		queueService.send(stockPriceIO, QueueConstants.MTQueue.UPDATE_PRICE_TXN_QUEUE);

		LOGGER.debug(QueueConstants.HistoricalQueue.UPDATE_PRICE_QUEUE.toUpperCase() +" : " + stockPriceIO.getNseSymbol() +" : END");
		
	}

}
