package com.example;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

import com.example.integration.routes.scheduler.QuartzRouteDaily;


public class Start {

	public static void main(String[] args) throws Exception {
		
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
		
		//ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		
		CamelContext context = new DefaultCamelContext();
		
		context.addComponent("jms",JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
		
		context.addRoutes(new QuartzRouteDaily());
		
		//context.addRoutes(new UnzipRoute());
		
		//context.addRoutes(new BhavUpdateRoute());
		
		context.start();
		//Thread.sleep(10000);
        //context.stop();
	}
    
}
