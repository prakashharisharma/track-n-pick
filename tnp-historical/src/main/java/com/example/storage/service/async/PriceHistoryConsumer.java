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
@Deprecated
public class PriceHistoryConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(PriceHistoryConsumer.class);

	@Autowired
	private PriceTemplate priceTemplate;

	@Autowired
	private QueueService queueService;
	
	@JmsListener(destination = QueueConstants.HistoricalQueue.UPDATE_PRICE_QUEUE)
	public void receiveMessage(@Payload StockPriceIO stockPriceIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {

		LOGGER.debug(QueueConstants.HistoricalQueue.UPDATE_PRICE_QUEUE.toUpperCase() +" : " + stockPriceIO.getNseSymbol() +" : START");

		//this.updatePriceHistory(stockPriceIO);

		queueService.send(stockPriceIO, QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE);

		LOGGER.debug(QueueConstants.HistoricalQueue.UPDATE_PRICE_QUEUE.toUpperCase() +" : " + stockPriceIO.getNseSymbol() +" : END");
		
	}



}
