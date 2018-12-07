package com.example.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.processors.EmailRearchWeeklyPerformanceProcessor;
@Component
public class QuartzRouteWeekly extends RouteBuilder {

	@Autowired
	private EmailRearchWeeklyPerformanceProcessor emailRearchWeeklyPerformanceProcessor;
	
	@Override
	public void configure() throws Exception {
		
		//0 50 10 ? * MON *
		from("quartz2://every_MONDAY_At_10_50?cron=0+50+10+?+*+MON+*").process(emailRearchWeeklyPerformanceProcessor)
		.to("log:every_MONDAY_At_10_50");

	}

}
