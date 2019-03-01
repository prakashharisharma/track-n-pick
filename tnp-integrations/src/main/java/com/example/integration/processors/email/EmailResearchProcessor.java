package com.example.integration.processors.email;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.util.io.model.NotificationTriggerIO;

@Service
public class EmailResearchProcessor implements Processor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EmailResearchProcessor.class);

	
	@Autowired
	private QueueService queueService;
	
	@Override
	public void process(Exchange arg0) throws Exception {

		LOGGER.info("EmailResearchProcessor START");

		NotificationTriggerIO notificationTriggerIO = new NotificationTriggerIO(NotificationTriggerIO.TriggerType.RESEARCH);
		
		LOGGER.debug("EmailResearchProcessor : Queuinh to Notification ... " + notificationTriggerIO);
		
		queueService.send(notificationTriggerIO, QueueConstants.MTQueue.NOTIFICATION_SEND_MAIL_TRIGGER);
		
		LOGGER.info("EmailResearchProcessor END");
		
	}

	

}
