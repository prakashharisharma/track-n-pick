package com.example.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.processors.DownloadNSE500StocksMasterProcessor;
import com.example.processors.UpdateCYROProcessor;
import com.example.processors.UpdateFYROProcessor;

@Component
public class QuartzRouteYearly extends RouteBuilder {

	@Autowired
	private UpdateFYROProcessor updateFYROProcessor;

	@Autowired
	private UpdateCYROProcessor updateCYROProcessor;

	@Autowired
	private DownloadNSE500StocksMasterProcessor downloadNSE500StocksMasterProcessor;

	@Override
	public void configure() throws Exception {
		// 0 45 10 1 JAN ? *
		from("quartz2://every_01_Jan_At_10_45?cron=0+45+10+1+JAN+?+*").process(updateCYROProcessor)
				.to("log:every_01_Jan_At_10_45");

		// 0 45 10 1 APR ? *
		from("quartz2://every_01_APR_At_10_45?cron=0+45+10+1+APR+?+*").process(updateFYROProcessor)
				.to("log:every_01_APR_At_10_45");

		// 0 55 10 1 APR ? *
		from("quartz2://every_01_APR_At_10_55?cron=0+45+10+1+APR+?+*").process(downloadNSE500StocksMasterProcessor)
				.to("log:every_01_APR_At_10_55");
	}

}
