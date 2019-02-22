package com.example.model.stocks;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.example.model.master.Stock;

@Entity
@Table(name = "STOCK_TECHNICALS")
public class StockTechnicals implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5870154333285682947L;


	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "STOCK_TECHNICALS_ID")
	long stockTechnicalsId;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STOCK_ID", referencedColumnName ="STOCK_ID",  nullable = false)
	Stock stock;

	@Column(name = "SMA_50", columnDefinition="Decimal(10,2) default '0.00'")
	double sma50;
	
	@Column(name = "PREV_SMA_50", columnDefinition="Decimal(10,2) default '0.00'")
	double prevSma50;
	
	
	@Column(name = "SMA_100", columnDefinition="Decimal(10,2) default '0.00'")
	double sma100;
	
	@Column(name = "SMA_200", columnDefinition="Decimal(10,2) default '0.00'")
	double sma200;
	
	@Column(name = "PREV_SMA_200", columnDefinition="Decimal(10,2) default '0.00'")
	double prevSma200;
	
	@Column(name = "RSI", columnDefinition="Decimal(10,2) default '0.00'")
	double rsi;

	@Column(name = "LONG_TERM_TREND")
	@Enumerated(EnumType.STRING)
	Direction longTermTrend;
	
	@Column(name = "MID_TERM_TREND")
	@Enumerated(EnumType.STRING)
	Direction midTermTrend;
	
	@Column(name = "CURRENT_TREND")
	@Enumerated(EnumType.STRING)
	Direction currentTrend;
	
	@Column(name = "LAST_MODIFIED")
	LocalDate lastModified = LocalDate.now();

	public long getStockTechnicalsId() {
		return stockTechnicalsId;
	}

	public void setStockTechnicalsId(long stockTechnicalsId) {
		this.stockTechnicalsId = stockTechnicalsId;
	}

	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	public double getSma50() {
		return sma50;
	}

	public void setSma50(double sma50) {
		this.sma50 = sma50;
	}

	public double getPrevSma50() {
		return prevSma50;
	}

	public void setPrevSma50(double prevSma50) {
		this.prevSma50 = prevSma50;
	}

	public double getSma200() {
		return sma200;
	}

	public void setSma200(double sma200) {
		this.sma200 = sma200;
	}

	public double getPrevSma200() {
		return prevSma200;
	}

	public void setPrevSma200(double prevSma200) {
		this.prevSma200 = prevSma200;
	}

	public double getRsi() {
		return rsi;
	}

	public void setRsi(double rsi) {
		this.rsi = rsi;
	}

	public LocalDate getLastModified() {
		return lastModified;
	}

	public void setLastModified(LocalDate lastModified) {
		this.lastModified = lastModified;
	}

	public double getSma100() {
		return sma100;
	}

	public void setSma100(double sma100) {
		this.sma100 = sma100;
	}

	public Direction getLongTermTrend() {
		return longTermTrend;
	}

	public void setLongTermTrend(Direction longTermTrend) {
		this.longTermTrend = longTermTrend;
	}

	public Direction getMidTermTrend() {
		return midTermTrend;
	}

	public void setMidTermTrend(Direction midTermTrend) {
		this.midTermTrend = midTermTrend;
	}

	public Direction getCurrentTrend() {
		return currentTrend;
	}

	public void setCurrentTrend(Direction currentTrend) {
		this.currentTrend = currentTrend;
	}

	@Override
	public String toString() {
		return "StockTechnicals [stockTechnicalsId=" + stockTechnicalsId + ", stock=" + stock.getNseSymbol() + ", sma50=" + sma50
				+ ", prevSma50=" + prevSma50 + ", sma100=" + sma100 + ", sma200=" + sma200 + ", prevSma200="
				+ prevSma200 + ", rsi=" + rsi + ", longTermTrend=" + longTermTrend + ", midTermTrend=" + midTermTrend
				+ ", currentTrend=" + currentTrend + ", lastModified=" + lastModified + "]";
	}


}
