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
import com.example.storage.model.DailyBhav;
import com.example.storage.repo.BhavTemplate;
import com.example.util.io.model.StockPriceIO;

@Component
public class BhavHistoryConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(BhavHistoryConsumer.class);

	@Autowired
	private BhavTemplate bhavTemplate;

	@Autowired
	private QueueService queueService;
	
	@JmsListener(destination = QueueConstants.HistoricalQueue.UPDATE_BHAV_QUEUE)
	public void receiveMessage(@Payload StockPriceIO stockPriceIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {

		LOGGER.debug(QueueConstants.HistoricalQueue.UPDATE_BHAV_QUEUE.toUpperCase() +" : " + stockPriceIO.getNseSymbol() +" : START");

		
		LOGGER.trace(QueueConstants.HistoricalQueue.UPDATE_BHAV_QUEUE.toUpperCase() +" : " + stockPriceIO.getNseSymbol() +" : updating Bhav History..");
		
		DailyBhav dailyBhav = new DailyBhav(stockPriceIO.getNseSymbol(), stockPriceIO.getOpen(), stockPriceIO.getHigh(),
				stockPriceIO.getLow(), stockPriceIO.getClose(), stockPriceIO.getLast(), stockPriceIO.getPrevClose(),
				stockPriceIO.getTottrdqty(), stockPriceIO.getTottrdval(), stockPriceIO.getTotaltrades(), stockPriceIO.getBhavDate());

		bhavTemplate.create(dailyBhav);

		LOGGER.debug(QueueConstants.HistoricalQueue.UPDATE_BHAV_QUEUE.toUpperCase() +" : " + stockPriceIO.getNseSymbol() +" : END");
		
		queueService.send(stockPriceIO, QueueConstants.HistoricalQueue.UPDATE_CANDLESTICK_QUEUE);
	}

}
