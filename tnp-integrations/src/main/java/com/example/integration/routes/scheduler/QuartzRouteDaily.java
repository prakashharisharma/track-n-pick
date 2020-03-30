package com.example.integration.routes.scheduler;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.integration.processors.email.EmailCurrentUnderValueStocksProcessor;
import com.example.integration.processors.email.EmailPortfolioProcessor;
import com.example.integration.processors.email.EmailResearchProcessor;
import com.example.integration.processors.update.UpdateFactorsProcessor;
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

	@Autowired
	private UpdateFactorsProcessor updateFactorsProcessor;
	
	
	@Override
	public void configure() throws Exception {
		
	/*	from("quartz2://everyDayAt_11_00?cron=0+01+10+?+*+MON,TUE,WED,THU,FRI+*").process(updateFactorsProcessor)
		.to("log:everyDayAt_11_00");*/
		
		//0 10 10 ? * MON,TUE,WED,THU,FRI * // 0+10+10+?+*+MON,TUE,WED,THU,FRI+*
		from("quartz2://everyDayAt_9_30?cron=0+08+07+?+*+MON,TUE,WED,THU,FRI+*").process(downloadNSEBhavProcessor)
		.to("log:everyDayAt_10_30");

		from("quartz2://everyDayAt_16_45?cron=0+18+07+?+*+MON,TUE,WED,THU,FRI+*").process(researchUpdateProcessor)
		.to("log:everyDayAt_16_45");
		
		from("quartz2://everyDayAt_9_45?cron=0+22+07+?+*+MON,TUE,WED,THU,FRI+*").process(emailPortfolioProcessor)
		.to("log:everyDayAt_10_45");
		
		from("quartz2://everyDayAt_11_00?cron=0+25+07+?+*+MON,TUE,WED,THU,FRI+*").process(emailResearchProcessor)
		.to("log:everyDayAt_11_00");
		
		
	}

}
