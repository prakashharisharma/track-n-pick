package com.example.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "stock.filter")
@PropertySource("classpath:rules.properties")
public class Rules {

	private double price;
	private double mcap;
	private double debtEquity;
	private double dividend;
	private double roe;
	private double roce;
	
	private double pb;
	private double pe;
	
	private double profitPer;
	
	private double averagingPer;

	private int watchlistSize;
	
	private int averagingSize;
	
	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getMcap() {
		return mcap;
	}

	public void setMcap(double mcap) {
		this.mcap = mcap;
	}

	public double getDebtEquity() {
		return debtEquity;
	}

	public void setDebtEquity(double debtEquity) {
		this.debtEquity = debtEquity;
	}

	public double getDividend() {
		return dividend;
	}

	public void setDividend(double dividend) {
		this.dividend = dividend;
	}

	public double getRoe() {
		return roe;
	}

	public void setRoe(double roe) {
		this.roe = roe;
	}

	public double getRoce() {
		return roce;
	}

	public void setRoce(double roce) {
		this.roce = roce;
	}

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

}
