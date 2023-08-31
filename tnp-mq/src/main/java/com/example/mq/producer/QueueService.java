package com.example.mq.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class QueueService {
	
	private static Logger log = LoggerFactory.getLogger(QueueService.class);

    @Autowired
    private JmsTemplate jmsTemplate;

    public void send(Object object, String queueName) {

        System.out.println("Sending ...." + queueName);
        jmsTemplate.convertAndSend(queueName, object);
    }
    
}
