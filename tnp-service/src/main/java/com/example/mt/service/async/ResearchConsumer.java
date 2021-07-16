package com.example.mt.service.async;

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
import com.example.util.io.model.ResearchIO;
import com.example.util.io.model.ResearchIO.ResearchType;;

@Component
public class ResearchConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResearchConsumer.class);
	
	@Autowired
	private QueueService queueService;

	@JmsListener(destination = QueueConstants.MTQueue.RESEARCH_QUEUE)
	public void receiveMessage(@Payload ResearchIO researchIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {

		LOGGER.info("RESEARCH_CONSUMER START " + researchIO);

		if (researchIO.getResearchType() == ResearchType.FUNDAMENTAL) {
			
			queueService.send(researchIO, QueueConstants.MTQueue.RESEARCH_FUNDAMENTAL_QUEUE);
			
		} else if (researchIO.getResearchType() == ResearchType.TECHNICAL) {
			
			queueService.send(researchIO, QueueConstants.MTQueue.RESEARCH_TECHNICAL_QUEUE);
			
		} else {
			LOGGER.info("INVALID RESEARCH TYPE");
		}

	}


}
