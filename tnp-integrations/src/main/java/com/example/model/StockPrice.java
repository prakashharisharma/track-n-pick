package com.example.model;

import java.io.Serializable;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

import com.fasterxml.jackson.annotation.JsonProperty;

@CsvRecord(separator = ",", skipFirstLine = true)
public class StockPrice implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7916644484544527519L;

	@JsonProperty("symbol")
	@DataField(pos = 1)
    private String nseSymbol;
	
	@DataField(pos = 2)
    private String series;
	
	@DataField(pos = 3)
    private double open;
	
	@DataField(pos = 4)
    private double high;
	
	
	@DataField(pos = 5)
    private double low;
	
    @DataField(pos = 6)
    private double close;
    
    @DataField(pos = 7)
    private double last;
    
    @DataField(pos = 8)
    private double prevClose;
    
    @DataField(pos = 9)
    private long tottrdqty;
    
    @DataField(pos = 10)
    private double tottrdval;
    
    @DataField(pos = 11)
    private String timestamp;
    
    @DataField(pos = 12)
    private long totaltrades;

    @DataField(pos = 13)
    private String isin;

    @DataField(pos = 14)
    private String dummy;

    
	public String getNseSymbol() {
		return nseSymbol;
	}

	public void setNseSymbol(String nseSymbol) {
		this.nseSymbol = nseSymbol;
	}

	public String getSeries() {
		return series;
	}

	public void setSeries(String series) {
		this.series = series;
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

	public long getTottrdqty() {
		return tottrdqty;
	}

	public void setTottrdqty(long tottrdqty) {
		this.tottrdqty = tottrdqty;
	}

	public double getTottrdval() {
		return tottrdval;
	}

	public void setTottrdval(double tottrdval) {
		this.tottrdval = tottrdval;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public long getTotaltrades() {
		return totaltrades;
	}

	public void setTotaltrades(long totaltrades) {
		this.totaltrades = totaltrades;
	}

	public String getIsin() {
		return isin;
	}

	public void setIsin(String isin) {
		this.isin = isin;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	

	public String getDummy() {
		return dummy;
	}

	public void setDummy(String dummy) {
		this.dummy = dummy;
	}

	@Override
	public String toString() {
		return "StockPrice [symbol=" + nseSymbol + ", close=" + close + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nseSymbol == null) ? 0 : nseSymbol.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StockPrice other = (StockPrice) obj;
		if (nseSymbol == null) {
			if (other.nseSymbol != null)
				return false;
		} else if (!nseSymbol.equals(other.nseSymbol))
			return false;
		return true;
	}
 
}
