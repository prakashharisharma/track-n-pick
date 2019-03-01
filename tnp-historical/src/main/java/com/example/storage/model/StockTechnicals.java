package com.example.storage.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "technicals_history")
public class StockTechnicals {

	@Id
	private String id;

	String nseSymbol;
	
	Instant bhavDate = Instant.now();
	
	double avgGain;
	
	double avgLoss;
	
	private MovingAverage movingAverage;
	
	private Indicator indicator;
	
	public StockTechnicals() {
		super();
		
	}

	public StockTechnicals(String nseSymbol, Instant bhavDate, double avgGain, double avgLoss,
			MovingAverage movingAverage, Indicator indicator) {
		super();
		this.nseSymbol = nseSymbol;
		this.bhavDate = bhavDate;
		this.avgGain = avgGain;
		this.avgLoss = avgLoss;
		this.movingAverage = movingAverage;
		this.indicator = indicator;
		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNseSymbol() {
		return nseSymbol;
	}

	public void setNseSymbol(String nseSymbol) {
		this.nseSymbol = nseSymbol;
	}

	public Instant getBhavDate() {
		return bhavDate;
	}

	public void setBhavDate(Instant bhavDate) {
		this.bhavDate = bhavDate;
	}

	public double getAvgGain() {
		return avgGain;
	}

	public void setAvgGain(double avgGain) {
		this.avgGain = avgGain;
	}

	public double getAvgLoss() {
		return avgLoss;
	}

	public void setAvgLoss(double avgLoss) {
		this.avgLoss = avgLoss;
	}

	public MovingAverage getMovingAverage() {
		return movingAverage;
	}

	public void setMovingAverage(MovingAverage movingAverage) {
		this.movingAverage = movingAverage;
	}

	public Indicator getIndicator() {
		return indicator;
	}

	public void setIndicator(Indicator indicator) {
		this.indicator = indicator;
	}

	@Override
	public String toString() {
		return "StockTechnicals [id=" + id + ", nseSymbol=" + nseSymbol + ", bhavDate=" + bhavDate + ", avgGain="
				+ avgGain + ", avgLoss=" + avgLoss + ", movingAverage=" + movingAverage + ", indicator=" + indicator
				+ "]";
	}

}
