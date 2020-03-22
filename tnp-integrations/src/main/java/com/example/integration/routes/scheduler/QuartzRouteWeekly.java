package com.example.integration.routes.scheduler;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.integration.processors.email.EmailCurrentUnderValueStocksProcessor;
@Component
public class QuartzRouteWeekly extends RouteBuilder {

	@Autowired
	private EmailCurrentUnderValueStocksProcessor emailCurrentUnderValueStocksProcessor;
	
	@Override
	public void configure() throws Exception {
		
		//0 10 10 ? * MON,WED,FRI * // 0+10+10+?+*+MON,WED,FRI+*
		from("quartz2://everyWednesDayAt_11_15?cron=0+55+07+?+*+MON+*").process(emailCurrentUnderValueStocksProcessor)
		.to("log:everyWednesDayAt_11_15");

	}

}
