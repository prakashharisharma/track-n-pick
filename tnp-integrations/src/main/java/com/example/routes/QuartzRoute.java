package com.example.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.routepolicy.quartz2.CronScheduledRoutePolicy;

import com.example.processors.MyTestProcessor;
import com.example.processors.PrintBodyProcessor;

public class QuartzRoute extends RouteBuilder{

	@Override
	public void configure() throws Exception {

		from("quartz2://firstDayOfTheMonth?cron=0+0/1+*+*+*+?").process(new MyTestProcessor())
		.to("log:executed every minute");
		
	}

}
