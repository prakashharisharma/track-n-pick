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
	
	Volume volume;

	Trend trend;
	
	Momentum momentum;
	
	public StockTechnicals() {
		super();
		
	}

	public StockTechnicals(String nseSymbol, Instant bhavDate, Volume volume, Trend trend, Momentum momentum) {
		super();
		this.nseSymbol = nseSymbol;
		this.bhavDate = bhavDate;
		this.volume = volume;
		this.trend = trend;
		this.momentum = momentum;
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

	public Volume getVolume() {
		return volume;
	}

	public void setVolume(Volume volume) {
		this.volume = volume;
	}

	public Trend getTrend() {
		return trend;
	}

	public void setTrend(Trend trend) {
		this.trend = trend;
	}

	public Momentum getMomentum() {
		return momentum;
	}

	public void setMomentum(Momentum momentum) {
		this.momentum = momentum;
	}

	@Override
	public String toString() {
		return "StockTechnicals [id=" + id + ", nseSymbol=" + nseSymbol + ", bhavDate=" + bhavDate + ", volume="
				+ volume + ", trend=" + trend + ", momentum=" + momentum + "]";
	}

}
