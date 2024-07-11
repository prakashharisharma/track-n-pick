package com.example.util.io.model;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;


public class StockPriceIO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7704441081622739216L;

	private String nseSymbol;

	private String series;

	private double open;

	private double high;

	private double low;

	private double close;

	private double last;

	private double prevClose;

	private long tottrdqty;

	private double tottrdval;

	private LocalDate timestamp;
	
	private Instant bhavDate;

	private long totaltrades;

	private String isin;

	private double change;
	private double yearLow;
	private double yearHigh;

	private double low14;
	private double high14;
	
	
	
	
	public StockPriceIO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public StockPriceIO(String nseSymbol, String isin) {
		super();
		this.nseSymbol = nseSymbol;
		this.isin = isin;
	}

	public StockPriceIO(String nseSymbol, String series, double open, double high, double low, double close,
			double last, double prevClose, long tottrdqty, double tottrdval, String timestamp, long totaltrades,
			String isin) {
		super();
		this.nseSymbol = nseSymbol;
		this.series = series;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.last = last;
		this.prevClose = prevClose;
		this.tottrdqty = tottrdqty;
		this.tottrdval = tottrdval;
		
		DateTimeFormatter formatter_1 = new DateTimeFormatterBuilder().parseCaseInsensitive()
				.appendPattern("yyyy-MM-dd").toFormatter(Locale.ENGLISH);

		LocalDate localdateBhavDate = LocalDate.parse(timestamp, formatter_1);
		
		Instant bhavInstant = localdateBhavDate.atStartOfDay().toInstant(ZoneOffset.UTC);
		
		
		this.timestamp = localdateBhavDate;
		this.bhavDate = bhavInstant;
		
		this.totaltrades = totaltrades;
		this.isin = isin;
		this.change = prevClose - close;

	}
	
	public StockPriceIO(String nseSymbol, String series, double open, double high, double low, double close,
			double last, double prevClose, long tottrdqty, double tottrdval, LocalDate timestamp, long totaltrades,
			String isin, double yearLow, double yearHigh) {
		super();
		this.nseSymbol = nseSymbol;
		this.series = series;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.last = last;
		this.prevClose = prevClose;
		this.tottrdqty = tottrdqty;
		this.tottrdval = tottrdval;
		Instant bhavInstant = timestamp.atStartOfDay().toInstant(ZoneOffset.UTC);
		
		this.timestamp = timestamp;
		this.bhavDate = bhavInstant;
		this.totaltrades = totaltrades;
		this.isin = isin;
		this.change = prevClose - close;
		this.yearLow = yearLow;
		this.yearHigh = yearHigh;
	}

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

	public LocalDate getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDate timestamp) {
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

	public Instant getBhavDate() {
		return bhavDate;
	}

	public void setBhavDate(Instant bhavDate) {
		this.bhavDate = bhavDate;
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
		return "StockPriceIO [nseSymbol=" + nseSymbol + ", series=" + series + ", open=" + open + ", high=" + high
				+ ", low=" + low + ", close=" + close + ", last=" + last + ", prevClose=" + prevClose + ", tottrdqty="
				+ tottrdqty + ", tottrdval=" + tottrdval + ", timestamp=" + timestamp + ", totaltrades=" + totaltrades
				+ ", isin=" + isin + ", change=" + change + ", yearLow=" + yearLow + ", yearHigh=" + yearHigh + "]";
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
		StockPriceIO other = (StockPriceIO) obj;
		if (nseSymbol == null) {
			if (other.nseSymbol != null)
				return false;
		} else if (!nseSymbol.equals(other.nseSymbol))
			return false;
		return true;
	}
}
