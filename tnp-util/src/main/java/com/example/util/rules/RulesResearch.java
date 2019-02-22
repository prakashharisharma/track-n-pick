package com.example.util.rules;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "research")
@PropertySource("classpath:config/rules/research_rules.properties")
public class RulesResearch {

	private double pb;
	private double pe;
	
	private double profitPer;
	private double averagingPer;
	private double targetPer;
	
	private double lowThreshold;
	private double highThreshold;
	
	private double pbOvervalued;
	private double peOvervalued;
	
	public double getPb() {
		return pb;
	}
	public void setPb(double pb) {
		this.pb = pb;
	}
	public double getPe() {
		return pe;
	}
	public void setPe(double pe) {
		this.pe = pe;
	}
	public double getProfitPer() {
		return profitPer;
	}
	public void setProfitPer(double profitPer) {
		this.profitPer = profitPer;
	}
	public double getAveragingPer() {
		return averagingPer;
	}
	public void setAveragingPer(double averagingPer) {
		this.averagingPer = averagingPer;
	}
	public double getTargetPer() {
		return targetPer;
	}
	public void setTargetPer(double targetPer) {
		this.targetPer = targetPer;
	}
	public double getLowThreshold() {
		return lowThreshold;
	}
	public void setLowThreshold(double lowThreshold) {
		this.lowThreshold = lowThreshold;
	}
	public double getHighThreshold() {
		return highThreshold;
	}
	public void setHighThreshold(double highThreshold) {
		this.highThreshold = highThreshold;
	}
	public double getPbOvervalued() {
		return pbOvervalued;
	}
	public void setPbOvervalued(double pbOvervalued) {
		this.pbOvervalued = pbOvervalued;
	}
	public double getPeOvervalued() {
		return peOvervalued;
	}
	public void setPeOvervalued(double peOvervalued) {
		this.peOvervalued = peOvervalued;
	}

}
