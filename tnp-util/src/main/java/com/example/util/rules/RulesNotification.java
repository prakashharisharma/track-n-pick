package com.example.util.rules;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "notification")
@PropertySource("classpath:config/rules/notification_rules.properties")
public class RulesNotification {

	private int watchlistSize;
	
	private int averagingSize;
	
	private int factorIntervalDays;

	private int apiCallCounter;

	public int getWatchlistSize() {
		return watchlistSize;
	}

	public void setWatchlistSize(int watchlistSize) {
		this.watchlistSize = watchlistSize;
	}

	public int getAveragingSize() {
		return averagingSize;
	}

	public void setAveragingSize(int averagingSize) {
		this.averagingSize = averagingSize;
	}

	public int getFactorIntervalDays() {
		return factorIntervalDays;
	}

	public void setFactorIntervalDays(int factorIntervalDays) {
		this.factorIntervalDays = factorIntervalDays;
	}

	public int getApiCallCounter() {
		return apiCallCounter;
	}

	public void setApiCallCounter(int apiCallCounter) {
		this.apiCallCounter = apiCallCounter;
	}
}
