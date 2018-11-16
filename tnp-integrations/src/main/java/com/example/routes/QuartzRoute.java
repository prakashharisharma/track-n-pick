package com.example.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.processors.CheckRearchListProcessor;
import com.example.processors.DownloadNSEBhavProcessor;
import com.example.processors.EmailPortfolioResearchProcessor;
import com.example.processors.EmailWatchListProcessor;
import com.example.processors.UpdateWatchListProcessor;
import com.example.processors.WatchListCleanProcessor;

@Component
public class QuartzRoute extends RouteBuilder{

	@Autowired
	private EmailWatchListProcessor emailWatchListProcessor;
	
	@Autowired
	private EmailPortfolioResearchProcessor emailPortfolioResearchProcessor;
	
	@Autowired
	private UpdateWatchListProcessor updateWatchListProcessor;
	
	@Autowired
	private WatchListCleanProcessor watchListCleanProcessor;
	
	@Autowired
	private DownloadNSEBhavProcessor downloadNSEBhavProcessor;
	
	@Autowired
	private CheckRearchListProcessor checkRearchListProcessor;
	
	@Override
	public void configure() throws Exception {

		//0 40 10 ? * 2#1 *
		from("quartz2://everyMonth_1ST_MONDAY_At_10_40?cron=0+40+10+?+*+2#1+*").process(watchListCleanProcessor)
		.to("log:everyMonth_1ST_MONDAY_At_10_40");
		
		from("quartz2://everyDayAt_10_30?cron=0+30+9+?+*+*+*").process(downloadNSEBhavProcessor)
		.to("log:everyDayAt_10_30");
		
		from("quartz2://everyDayAt_10_45?cron=0+40+9+?+*+*+*").process(emailPortfolioResearchProcessor)
		.to("log:everyDayAt_10_45");
		
		from("quartz2://everyDayAt_10_45?cron=0+55+11+?+*+*+*").process(checkRearchListProcessor)
		.to("log:everyDayAt_11_55");
		
		from("quartz2://everyDayAt_15_45?cron=0+45+0,15+?+*+*+*").process(updateWatchListProcessor)
		.to("log:everyDayAt_15_45");
		
		from("quartz2://everyDayAt_16_00?cron=0+0+0,16+?+*+*+*").process(emailWatchListProcessor)
		.to("log:everyDayAt_16_00");

	}

}
