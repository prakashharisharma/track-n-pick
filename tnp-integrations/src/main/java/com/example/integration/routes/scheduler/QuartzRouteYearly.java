package com.example.integration.routes.scheduler;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.integration.processors.update.UpdateCYROProcessor;
import com.example.integration.processors.update.UpdateFYROProcessor;

@Component
public class QuartzRouteYearly extends RouteBuilder {

	@Autowired
	private UpdateFYROProcessor updateFYROProcessor;

	@Autowired
	private UpdateCYROProcessor updateCYROProcessor;
	
	@Override
	public void configure() throws Exception {
		// 0 45 10 1 JAN ? *
		from("quartz2://every_01_Jan_At_10_45?cron=0+50+12+2+JAN+?+*").process(updateCYROProcessor)
				.to("log:every_01_Jan_At_10_45");

		// 0 45 10 1 APR ? *
		from("quartz2://every_01_APR_At_10_45?cron=0+52+10+1+APR+?+*").process(updateFYROProcessor)
				.to("log:every_01_APR_At_10_45");

	}

}
