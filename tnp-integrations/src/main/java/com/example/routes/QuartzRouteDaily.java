package com.example.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.processors.DownloadNSEBhavProcessor;
import com.example.processors.EmailPortfolioProcessor;
import com.example.processors.EmailRearchTargetAchievedProcessor;
import com.example.processors.EmailRearchWeeklyPerformanceProcessor;
import com.example.processors.EmailResearchProcessor;
import com.example.processors.ResearchUpdateProcessor;
import com.example.processors.WatchListUpdateProcessor;

@Component
public class QuartzRouteDaily extends RouteBuilder{

	@Autowired
	private EmailResearchProcessor emailResearchProcessor;
	
	@Autowired
	private EmailRearchTargetAchievedProcessor emailRearchTargetAchievedProcessor;

	@Autowired
	private EmailPortfolioProcessor emailPortfolioProcessor;
	
	@Autowired
	private WatchListUpdateProcessor watchListUpdateProcessor;

	@Autowired
	private ResearchUpdateProcessor researchUpdateProcessor;
	
	@Autowired
	private DownloadNSEBhavProcessor downloadNSEBhavProcessor;

	@Autowired
	private EmailRearchWeeklyPerformanceProcessor emailRearchWeeklyPerformanceProcessor;
	
	@Override
	public void configure() throws Exception {

		from("quartz2://everyDayAt_9_30?cron=0+21+10+?+*+*+*").process(downloadNSEBhavProcessor)
		.to("log:everyDayAt_10_30");

		from("quartz2://everyDayAt_9_45?cron=0+23+10+?+*+*+*").process(emailPortfolioProcessor)
		.to("log:everyDayAt_10_45");
		
		from("quartz2://everyDayAt_11_45?cron=0+25+10+?+*+*+*").process(emailRearchTargetAchievedProcessor)
		.to("log:everyDayAt_11_55");
		
		from("quartz2://everyDayAt_15_45?cron=0+15+0,16+?+*+*+*").process(watchListUpdateProcessor)
		.to("log:everyDayAt_15_45");

		from("quartz2://everyDayAt_16_45?cron=0+15+0,16+?+*+*+*").process(researchUpdateProcessor)
		.to("log:everyDayAt_16_45");
		
		from("quartz2://everyDayAt_16_00?cron=0+17+0,16+?+*+*+*").process(emailResearchProcessor)
		.to("log:everyDayAt_16_00");

		//Test
		from("quartz2://everyDayAt_16_30?cron=0+18+0,16+?+*+*+*").process(emailRearchWeeklyPerformanceProcessor)
		.to("log:everyDayAt_16_30");
		
				
	}

}