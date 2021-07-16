package com.example.external.service.async;

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
import com.example.util.EmailService;
import com.example.util.io.model.EmailIO;

@Component
public class EmailConsumer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EmailConsumer.class);
	
	@Autowired
	private EmailService emailService;
	
	@JmsListener(destination = QueueConstants.ExternalQueue.SEND_EMAIL_QUEUE)
	public void receiveMessage(@Payload EmailIO emailIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {
		
		
		try {
			emailService.sendEmail(emailIO.getEmailTo(), emailIO.getEmailSubject(), emailIO.getEmailBody());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
