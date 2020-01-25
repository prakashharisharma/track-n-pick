package com.example.integration.routes.scheduler;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.integration.processors.update.UpdateMonthlyValueProcessor;

@Component
public class QuartzRouteMonthly extends RouteBuilder {

	@Autowired
	private UpdateMonthlyValueProcessor updateMonthlyValueProcessor;
	
	@Override
	public void configure() throws Exception {

		//0 47 10 1 * ? *
		from("quartz2://everyMonth_1ST_At_10_47?cron=0+45+12+2+*+?+*").process(updateMonthlyValueProcessor)
				.to("log:everyMonth_1ST_At_10_47");

	}

}
