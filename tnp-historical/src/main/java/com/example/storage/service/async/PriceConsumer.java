package com.example.storage.service.async;

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
import com.example.storage.repo.PriceTemplate;
import com.example.storage.service.StorageService;
import com.example.util.io.model.StockPriceIO;

@Component
public class PriceConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(PriceConsumer.class);

	@Autowired
	private StorageService storageService;

	// Temporary
	@Autowired
	private PriceTemplate priceTemplate;

	@Autowired
	private QueueService queueService;
	
	@JmsListener(destination = QueueConstants.HistoricalQueue.UPDATE_PRICE_QUEUE)
	public void receiveMessage(@Payload StockPriceIO stockPriceIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {

		System.out.println("PS_CONSUMER START " + stockPriceIO);

		// To Be Removed In Future
		storageService.updatePrice(stockPriceIO.getNseSymbol(), stockPriceIO.getOpen(), stockPriceIO.getHigh(), stockPriceIO.getLow(), stockPriceIO.getClose(), stockPriceIO.getLast(), stockPriceIO.getPrevClose(), stockPriceIO.getTottrdqty(), stockPriceIO.getTottrdval(), stockPriceIO.getTotaltrades(), stockPriceIO.getBhavDate());
		//
		
		
		StockPrice stockPriceN = new StockPrice(stockPriceIO.getNseSymbol(), stockPriceIO.getOpen(), stockPriceIO.getHigh(),
				stockPriceIO.getLow(), stockPriceIO.getClose(), stockPriceIO.getLast(), stockPriceIO.getPrevClose(),
				stockPriceIO.getTottrdqty(), stockPriceIO.getTottrdval(), stockPriceIO.getTotaltrades(), stockPriceIO.getBhavDate());

		double yearLow = storageService.getyearLow(stockPriceIO.getNseSymbol());

		double yearHigh = storageService.getyearHigh(stockPriceIO.getNseSymbol());

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

		priceTemplate.create(stockPriceN);

		LOGGER.info("ADDED PRICE DATA :" + stockPriceIO.getNseSymbol());

		Thread.sleep(50);
		
		queueService.send(stockPriceIO, QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE);
		
		queueService.send(stockPriceIO, QueueConstants.MTQueue.UPDATE_PRICE_TXN_QUEUE);

	}

}
