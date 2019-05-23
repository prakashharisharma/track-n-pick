package com.example.storage.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Document(collection = "price_history")
public class StockPrice {


	@Id
	private String id;

	String nseSymbol;
	
	double open;
	double high;
	double low;
	double close;
	double last;
	double prevClose;
	long totalTradedQuantity;
	double totalTradedValue;
	long totalTrades;

	Instant bhavDate = Instant.now();
	
	double change;
	double yearLow;
	double yearHigh;
	
	double low14;
	double high14;
	
	public StockPrice() {
		super();
	}

	public StockPrice(String nseSymbol, double open, double high, double low, double close, double last,
			double prevClose, long totalTradedQuantity, double totalTradedValue, long totalTrades, Instant bhavDate) {
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

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getClose() {
		return close;
	}

	public void setClose(double close) {
		this.close = close;
	}

	public double getLast() {
		return last;
	}

	public void setLast(double last) {
		this.last = last;
	}

	public double getPrevClose() {
		return prevClose;
	}

	public void setPrevClose(double prevClose) {
		this.prevClose = prevClose;
	}

	public long getTotalTradedQuantity() {
		return totalTradedQuantity;
	}

	public void setTotalTradedQuantity(long totalTradedQuantity) {
		this.totalTradedQuantity = totalTradedQuantity;
	}

	public double getTotalTradedValue() {
		return totalTradedValue;
	}

	public void setTotalTradedValue(double totalTradedValue) {
		this.totalTradedValue = totalTradedValue;
	}

	public long getTotalTrades() {
		return totalTrades;
	}

	public void setTotalTrades(long totalTrades) {
		this.totalTrades = totalTrades;
	}

	public Instant getBhavDate() {
		return bhavDate;
	}

	public void setBhavDate(Instant bhavDate) {
		this.bhavDate = bhavDate;
	}

	public double getChange() {
		return change;
	}

	public void setChange(double change) {
		this.change = change;
	}

	public double getYearLow() {
		return yearLow;
	}

	public void setYearLow(double yearLow) {
		this.yearLow = yearLow;
	}

	public double getYearHigh() {
		return yearHigh;
	}

	public void setYearHigh(double yearHigh) {
		this.yearHigh = yearHigh;
	}

	public double getLow14() {
		return low14;
	}

	public void setLow14(double low14) {
		this.low14 = low14;
	}

	public double getHigh14() {
		return high14;
	}

	public void setHigh14(double high14) {
		this.high14 = high14;
	}

	@Override
	public String toString() {
		return "StockPriceN [id=" + id + ", nseSymbol=" + nseSymbol + ", open=" + open + ", high=" + high + ", low="
				+ low + ", close=" + close + ", last=" + last + ", prevClose=" + prevClose + ", totalTradedQuantity="
				+ totalTradedQuantity + ", totalTradedValue=" + totalTradedValue + ", totalTrades=" + totalTrades
				+ ", bhavDate=" + bhavDate + ", change=" + change + ", yearLow=" + yearLow + ", yearHigh=" + yearHigh
				+ "]";
	}

	
}
