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
	
	@Column(name = "SMA_200", columnDefinition="Decimal(10,2) default '0.00'")
	double sma200;
	
	@Column(name = "RSI", columnDefinition="Decimal(10,2) default '0.00'")
	double rsi;

	@Column(name = "LAST_MODIFIED")
	LocalDate lastModified = LocalDate.now();

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

	public double getSma200() {
		return sma200;
	}

	public void setSma200(double sma200) {
		this.sma200 = sma200;
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

	@Override
	public String toString() {
		return "StockTechnicals [stockTechnicalsId=" + stockTechnicalsId + ", stock=" + stock + ", sma50=" + sma50
				+ ", sma200=" + sma200 + ", rsi=" + rsi + ", lastModified=" + lastModified + "]";
	}
	
	
	
}
