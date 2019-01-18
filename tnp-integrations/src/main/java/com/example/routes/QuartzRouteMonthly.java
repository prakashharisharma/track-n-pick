package com.example.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.processors.EmailResearchHistoryProcessor;
import com.example.processors.UpdateMonthlyValueProcessor;
import com.example.processors.WatchListCleanProcessor;

@Component
public class QuartzRouteMonthly extends RouteBuilder {

	@Autowired
	private EmailResearchHistoryProcessor emailResearchHistoryProcessor;

	@Autowired
	private WatchListCleanProcessor watchListCleanProcessor;

	@Autowired
	private UpdateMonthlyValueProcessor updateMonthlyValueProcessor;
	
	@Override
	public void configure() throws Exception {

		// 0 40 10 ? * 2#1 *
		from("quartz2://everyMonth_1ST_MONDAY_At_10_30?cron=0+30+10+?+*+2#1+*").process(emailResearchHistoryProcessor)
				.to("log:everyMonth_1ST_MONDAY_At_10_30");
		// 0 40 10 ? * 2#1 *
		from("quartz2://everyMonth_1ST_MONDAY_At_10_40?cron=0+40+10+?+*+2#1+*").process(watchListCleanProcessor)
				.to("log:everyMonth_1ST_MONDAY_At_10_40");
		//0 47 10 1 * ? *
		from("quartz2://everyMonth_1ST_At_10_47?cron=0+29+10+1+*+?+*").process(updateMonthlyValueProcessor)
				.to("log:everyMonth_1ST_At_10_47");

	}

}
