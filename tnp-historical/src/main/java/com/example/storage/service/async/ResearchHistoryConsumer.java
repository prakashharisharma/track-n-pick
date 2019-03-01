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
import com.example.storage.model.StockResearch;
import com.example.storage.repo.ResearchTemplate;
import com.example.util.io.model.ResearchIO;

@Component
public class ResearchHistoryConsumer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResearchHistoryConsumer.class);

	@Autowired
	private ResearchTemplate researchTemplate;
	
	@JmsListener(destination = QueueConstants.HistoricalQueue.UPDATE_RESEARCH_QUEUE)
	public void receiveMessage(@Payload ResearchIO researchIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {
		
		LOGGER.debug(QueueConstants.HistoricalQueue.UPDATE_RESEARCH_QUEUE.toUpperCase() +" : " + researchIO.getNseSymbol() +" : START");
		
		StockResearch stockResearch = new StockResearch(researchIO.getNseSymbol(), researchIO.getResearchType(), researchIO.getResearchTrigger(), researchIO.getResearchPrice(), researchIO.getPe(), researchIO.getPb(), researchIO.getResearchDate());
		
		researchTemplate.create(stockResearch);
		
		LOGGER.debug(QueueConstants.HistoricalQueue.UPDATE_RESEARCH_QUEUE.toUpperCase() +" : " + researchIO.getNseSymbol() +" : END");
	}
}
