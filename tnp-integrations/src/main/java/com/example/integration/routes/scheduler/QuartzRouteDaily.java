package com.example.integration.routes.scheduler;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.integration.processors.email.EmailPortfolioProcessor;
import com.example.integration.processors.email.EmailResearchProcessor;
import com.example.integration.processors.update.UpdateResearchProcessor;
import com.example.integration.processors.download.DownloadNSEBhavProcessor;

@Component
public class QuartzRouteDaily extends RouteBuilder{

	@Autowired
	private EmailResearchProcessor emailResearchProcessor;

	@Autowired
	private EmailPortfolioProcessor emailPortfolioProcessor;
	
	@Autowired
	private UpdateResearchProcessor researchUpdateProcessor;
	
	@Autowired
	private DownloadNSEBhavProcessor downloadNSEBhavProcessor;

	@Override
	public void configure() throws Exception {
		//0 10 10 ? * MON,TUE,WED,THU,FRI * // 0+10+10+?+*+MON,TUE,WED,THU,FRI+*
		from("quartz2://everyDayAt_9_30?cron=0+11+10+?+*+MON,TUE,WED,THU,FRI+*").process(downloadNSEBhavProcessor)
		.to("log:everyDayAt_10_30");

		from("quartz2://everyDayAt_16_45?cron=0+45+10+?+*+MON,TUE,WED,THU,FRI+*").process(researchUpdateProcessor)
		.to("log:everyDayAt_16_45");
		
		from("quartz2://everyDayAt_9_45?cron=0+50+10+?+*+MON,TUE,WED,THU,FRI+*").process(emailPortfolioProcessor)
		.to("log:everyDayAt_10_45");
		
		from("quartz2://everyDayAt_16_00?cron=0+55+10+?+*+MON,TUE,WED,THU,FRI+*").process(emailResearchProcessor)
		.to("log:everyDayAt_16_00");

	}

}
