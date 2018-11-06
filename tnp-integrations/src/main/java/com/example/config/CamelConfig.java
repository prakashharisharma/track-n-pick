package com.example.config;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;

//import javax.jms.ConnectionFactory;

//import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jms.JmsComponent;
//import org.apache.camel.component.jms.JmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelConfig {

	@Autowired
	CamelContext camelContext;

	@Bean
	ConnectionFactory connectionFactory() {
		//return new ActiveMQConnectionFactory("tcp://localhost:61616");
		return new ActiveMQConnectionFactory("vm://localhost");
	}
	
	@PostConstruct
	public void afterPropertiesSet() {
		camelContext.addComponent("jms",JmsComponent.jmsComponentAutoAcknowledge(connectionFactory()));
	}
	
}
