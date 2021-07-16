package com.example.storage.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Document(collection = "price_history")
public class StockPrice {

	@Id
	private String id;

	private String nseSymbol;
	
	private Double open;
	private Double high;
	private Double low;
	private Double close;
	private Double prevClose;

	private Instant bhavDate = Instant.now();
	
	private Double change;
	
	private Double yearLow;
	private Double yearHigh;
	
	private Double low14;
	private Double high14;
	
	public StockPrice() {
		super();
	}

	public StockPrice(String nseSymbol, Double open, Double high, Double low, Double close,
			Double prevClose, Instant bhavDate) {
		super();
		this.nseSymbol = nseSymbol;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.prevClose = prevClose;
		this.bhavDate = bhavDate;
		this.change = (close - prevClose );
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

	public Double getOpen() {
		return open;
	}

	public void setOpen(Double open) {
		this.open = open;
	}

	public Double getHigh() {
		return high;
	}

	public void setHigh(Double high) {
		this.high = high;
	}

	public Double getLow() {
		return low;
	}

	public void setLow(Double low) {
		this.low = low;
	}

	public Double getClose() {
		return close;
	}

	public void setClose(Double close) {
		this.close = close;
	}

	public Double getPrevClose() {
		return prevClose;
	}

	public void setPrevClose(Double prevClose) {
		this.prevClose = prevClose;
	}

	public Instant getBhavDate() {
		return bhavDate;
	}

	public void setBhavDate(Instant bhavDate) {
		this.bhavDate = bhavDate;
	}

	public Double getChange() {
		return change;
	}

	public void setChange(Double change) {
		this.change = change;
	}

	public Double getYearLow() {
		return yearLow;
	}

	public void setYearLow(Double yearLow) {
		this.yearLow = yearLow;
	}

	public Double getYearHigh() {
		return yearHigh;
	}

	public void setYearHigh(Double yearHigh) {
		this.yearHigh = yearHigh;
	}

	public Double getLow14() {
		return low14;
	}

	public void setLow14(Double low14) {
		this.low14 = low14;
	}

	public Double getHigh14() {
		return high14;
	}

	public void setHigh14(Double high14) {
		this.high14 = high14;
	}

	@Override
	public String toString() {
		return "StockPrice [id=" + id + ", nseSymbol=" + nseSymbol + ", open=" + open + ", high=" + high + ", low="
				+ low + ", close=" + close + ", prevClose=" + prevClose + ", bhavDate=" + bhavDate + ", change="
				+ change + ", yearLow=" + yearLow + ", yearHigh=" + yearHigh + ", low14=" + low14 + ", high14=" + high14
				+ "]";
	}


}
