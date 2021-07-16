package com.example.storage.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "bhav_history")
public class DailyBhav {

	@Id
	private String id;

	private String nseSymbol;
	
	private Double open;
	private Double high;
	private Double low;
	private Double close;
	private Double last;
	private Double prevClose;
	private Long totalTradedQuantity;
	private Double totalTradedValue;
	private Long totalTrades;

	private Instant bhavDate = Instant.now();
	
	
	public DailyBhav(String nseSymbol, Double open, Double high, Double low, Double close, Double last,
			Double prevClose, Long totalTradedQuantity, Double totalTradedValue, Long totalTrades, Instant bhavDate) {
		super();
		this.nseSymbol = nseSymbol;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.last = last;
		this.prevClose = prevClose;
		this.totalTradedQuantity = totalTradedQuantity;
		this.totalTradedValue = totalTradedValue;
		this.totalTrades = totalTrades;
		this.bhavDate = bhavDate;
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


	public Double getLast() {
		return last;
	}


	public void setLast(Double last) {
		this.last = last;
	}


	public Double getPrevClose() {
		return prevClose;
	}


	public void setPrevClose(Double prevClose) {
		this.prevClose = prevClose;
	}


	public Long getTotalTradedQuantity() {
		return totalTradedQuantity;
	}


	public void setTotalTradedQuantity(Long totalTradedQuantity) {
		this.totalTradedQuantity = totalTradedQuantity;
	}


	public Double getTotalTradedValue() {
		return totalTradedValue;
	}


	public void setTotalTradedValue(Double totalTradedValue) {
		this.totalTradedValue = totalTradedValue;
	}


	public Long getTotalTrades() {
		return totalTrades;
	}


	public void setTotalTrades(Long totalTrades) {
		this.totalTrades = totalTrades;
	}


	public Instant getBhavDate() {
		return bhavDate;
	}


	public void setBhavDate(Instant bhavDate) {
		this.bhavDate = bhavDate;
	}


	@Override
	public String toString() {
		return "DailyBhav [id=" + id + ", nseSymbol=" + nseSymbol + ", open=" + open + ", high=" + high + ", low=" + low
				+ ", close=" + close + ", last=" + last + ", prevClose=" + prevClose + ", totalTradedQuantity="
				+ totalTradedQuantity + ", totalTradedValue=" + totalTradedValue + ", totalTrades=" + totalTrades
				+ ", bhavDate=" + bhavDate + "]";
	}
	
	
}
