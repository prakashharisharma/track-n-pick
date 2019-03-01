package com.example.integration.routes.scheduler;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.integration.processors.download.DownloadNSE500Processor;
import com.example.integration.processors.download.DownloadNifty200Processor;
import com.example.integration.processors.download.DownloadNifty50Processor;
import com.example.integration.processors.update.UpdateCYROProcessor;
import com.example.integration.processors.update.UpdateFYROProcessor;

@Component
public class QuartzRouteYearly extends RouteBuilder {

	@Autowired
	private UpdateFYROProcessor updateFYROProcessor;

	@Autowired
	private UpdateCYROProcessor updateCYROProcessor;

	@Autowired
	private DownloadNSE500Processor downloadNSE500StocksMasterProcessor;
	
	@Autowired
	private DownloadNifty50Processor downloadNifty50Processor;

	@Autowired
	private DownloadNifty200Processor downloadNifty200Processor;
	
	@Override
	public void configure() throws Exception {
		// 0 45 10 1 JAN ? *
		from("quartz2://every_01_Jan_At_10_45?cron=0+21+10+1+JAN+?+*").process(updateCYROProcessor)
				.to("log:every_01_Jan_At_10_45");

		// 0 45 10 1 APR ? *
		from("quartz2://every_01_APR_At_10_45?cron=0+45+10+1+APR+?+*").process(updateFYROProcessor)
				.to("log:every_01_APR_At_10_45");

		// 0 55 10 1 APR ? *
		from("quartz2://every_01_APR_At_10_55?cron=0+55+10+1+APR+?+*").process(downloadNSE500StocksMasterProcessor)
				.to("log:every_01_APR_At_10_55");
		
		// 0 55 10 1 APR ? *
		from("quartz2://every_01_APR_At_11_05?cron=0+22+0,18+13+FEB+?+*").process(downloadNifty50Processor)
				.to("log:every_01_APR_At_11_05");
		
		
		// 0 55 10 1 APR ? *
		from("quartz2://every_01_APR_At_11_10?cron=0+36+0,13+12+FEB+?+*").process(downloadNifty200Processor)
			 	.to("log:every_01_APR_At_11_10");
	}

}
