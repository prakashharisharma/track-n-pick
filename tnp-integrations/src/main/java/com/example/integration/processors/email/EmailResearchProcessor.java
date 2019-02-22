package com.example.integration.processors.email;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.util.io.model.NotificationTriggerIO;

@Service
public class EmailResearchProcessor implements Processor {
	
	@Autowired
	private QueueService queueService;
	
	@Override
	public void process(Exchange arg0) throws Exception {

		System.out.println("EmailResearchListProcessor START");

		NotificationTriggerIO notificationTriggerIO = new NotificationTriggerIO(NotificationTriggerIO.TriggerType.RESEARCH);
		
		queueService.send(notificationTriggerIO, QueueConstants.MTQueue.NOTIFICATION_SEND_MAIL_TRIGGER);
		
		System.out.println("EmailResearchListProcessor END");
		
	}

	

}
