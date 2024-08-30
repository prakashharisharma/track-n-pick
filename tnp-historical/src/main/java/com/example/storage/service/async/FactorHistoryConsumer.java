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
import com.example.storage.model.StockFactor;
import com.example.storage.repo.FactorTemplate;
import com.example.util.io.model.StockFactorIO;

@Component
@Deprecated
public class FactorHistoryConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(FactorHistoryConsumer.class);

	@Autowired
	private FactorTemplate factorTemplate;
	
	@JmsListener(destination = QueueConstants.HistoricalQueue.UPDATE_FACTORS_QUEUE)
	public void receiveMessage(@Payload StockFactorIO stockFactorIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {
		
		LOGGER.debug(QueueConstants.HistoricalQueue.UPDATE_FACTORS_QUEUE.toUpperCase() +" : " + stockFactorIO.getNseSymbol() +" : START" );
		
		StockFactor stockFactor = new StockFactor(stockFactorIO.getNseSymbol(), stockFactorIO.getMarketCap(), stockFactorIO.getDebtEquity(), stockFactorIO.getCurrentRatio(), stockFactorIO.getQuickRatio(), stockFactorIO.getDividend(), stockFactorIO.getBookValue(), stockFactorIO.getEps(), stockFactorIO.getReturnOnEquity(), stockFactorIO.getReturnOnCapital(), stockFactorIO.getFaceValue(), stockFactorIO.getQuarterEndedInstant());
		
		factorTemplate.create(stockFactor);
		
		LOGGER.debug(QueueConstants.HistoricalQueue.UPDATE_FACTORS_QUEUE.toUpperCase() +" : " + stockFactorIO.getNseSymbol() +" : END" );
	}
	
}
