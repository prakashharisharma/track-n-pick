package com.example.model.stocks;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.example.model.master.Stock;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true, 
value = {"Volume", "PercentageDiff", "ChangePercent"})
@Entity
@Table(name = "STOCK_PRICE")
public class StockPrice implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1495123844495916776L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "STOCK_PRICE_ID")
	long stockPriceId;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STOCK_ID", referencedColumnName ="STOCK_ID",  nullable = false)
	Stock stock;
	
	@JsonProperty("LastTradedPrice")
	@Column(name = "CURRENT_PRICE", columnDefinition="Decimal(10,2) default '0.00'")
	double currentPrice;
	
	@Column(name = "PREV_CLOSE", columnDefinition="Decimal(10,2) default '0.00'")
	double prevClose;
	
	@Column(name = "OPEN", columnDefinition="Decimal(10,2) default '0.00'")
	double openPrice;
	
	@JsonProperty("FiftyTwoWeekLow")
	@Column(name = "YEAR_LOW", columnDefinition="Decimal(10,2) default '0.00'")
	double yearLow;
	
	@JsonProperty("FiftyTwoWeekHigh")
	@Column(name = "YEAR_HIGH", columnDefinition="Decimal(10,2) default '0.00'")
	double yearHigh;

	@Column(name = "LAST_MODIFIED")
	LocalDate lastModified = LocalDate.now();

	@Column(name = "BHAV_DATE")
	LocalDate bhavDate = LocalDate.now();
	
	public long getStockPriceId() {
		return stockPriceId;
	}

	public void setStockPriceId(long stockPriceId) {
		this.stockPriceId = stockPriceId;
	}

	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	public double getCurrentPrice() {
		return currentPrice;
	}

	public void setCurrentPrice(double currentPrice) {
		this.currentPrice = currentPrice;
	}

	public double getPrevClose() {
		return prevClose;
	}

	public void setPrevClose(double prevClose) {
		this.prevClose = prevClose;
	}

	public double getOpenPrice() {
		return openPrice;
	}

	public void setOpenPrice(double openPrice) {
		this.openPrice = openPrice;
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

	public LocalDate getLastModified() {
		return lastModified;
	}

	public void setLastModified(LocalDate lastModified) {
		this.lastModified = lastModified;
	}
	
	public LocalDate getBhavDate() {
		return bhavDate;
	}

	public void setBhavDate(LocalDate bhavDate) {
		this.bhavDate = bhavDate;
	}

	@Override
	public String toString() {
		return "StockPrice [stockPriceId=" + stockPriceId 
				+ ", currentPrice=" + currentPrice + ", yearLow=" + yearLow + ", yearHigh=" + yearHigh
				+ ", lastModified=" + lastModified + "]";
	}

	

}
