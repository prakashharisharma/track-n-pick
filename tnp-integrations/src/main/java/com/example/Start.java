package com.example;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

import com.example.routes.CamelBindyCsvRoute;
import com.example.routes.QuartzRoute;
import com.example.routes.UnzipRoute;

public class Start {

	public static void main(String[] args) throws Exception {
		
		//ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
		
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		
		CamelContext context = new DefaultCamelContext();
		
		context.addComponent("jms",JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
		
		context.addRoutes(new QuartzRoute());
		
		//context.addRoutes(new UnzipRoute());
		
		//context.addRoutes(new CamelBindyCsvRoute());
		
		context.start();
		//Thread.sleep(10000);
        //context.stop();
	}
    
}
