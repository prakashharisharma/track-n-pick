package com.example.integration.config;

import java.util.Arrays;

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
	/*

	@Autowired
	private CamelContext camelContext;

	@Bean
	public ConnectionFactory connectionFactory() {
		//return new ActiveMQConnectionFactory("tcp://localhost:61616");
		//return new ActiveMQConnectionFactory("tcp://activemq-server:61616");
		//return new ActiveMQConnectionFactory("vm://localhost");


		//ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("vm://localhost");
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
	    factory.setTrustedPackages(Arrays.asList("com.example.util.io.model"));
	    factory.setTrustAllPackages(true);
	    System.out.println("Connecting.......");
	    return factory;

	}
	
	@PostConstruct
	public void afterPropertiesSet() {
		camelContext.addComponent("jms",JmsComponent.jmsComponentAutoAcknowledge(connectionFactory()));
	}
	*/
}
