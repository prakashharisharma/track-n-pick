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
import com.example.util.io.model.type.DirectionIO;

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
	
	
	@Column(name = "PREV_SMA_100", columnDefinition="Decimal(10,2) default '0.00'")
	double prevSma100;
	
	@Column(name = "SMA_200", columnDefinition="Decimal(10,2) default '0.00'")
	double sma200;
	
	@Column(name = "PREV_SMA_200", columnDefinition="Decimal(10,2) default '0.00'")
	double prevSma200;
	
	@Column(name = "SMA_21", columnDefinition="Decimal(10,2) default '0.00'")
	double sma21;
	
	@Column(name = "PREV_SMA_21", columnDefinition="Decimal(10,2) default '0.00'")
	double prevSma21;
	
	@Column(name = "RSI", columnDefinition="Decimal(10,2) default '0.00'")
	double rsi;
	
/*	@Column(name = "LONG_TERM_TREND")
	@Enumerated(EnumType.STRING)
	DirectionIO longTermTrend;
	
	@Column(name = "MID_TERM_TREND")
	@Enumerated(EnumType.STRING)
	DirectionIO midTermTrend;
	
	@Column(name = "CURRENT_TREND")
	@Enumerated(EnumType.STRING)
	DirectionIO currentTrend;*/
	
	@Column(name = "LAST_MODIFIED")
	LocalDate lastModified = LocalDate.now();

	@Column(name = "SOK", columnDefinition="Decimal(10,2) default '0.00'")
	double sok;
	
	@Column(name = "SOD", columnDefinition="Decimal(10,2) default '0.00'")
	double sod;
	
	@Column(name = "OBV")
	long obv;
	
	@Column(name = "ROCV", columnDefinition="Decimal(10,2) default '0.00'")
	double rocv;
	
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

	public double getPrevSma100() {
		return prevSma100;
	}

	public void setPrevSma100(double prevSma100) {
		this.prevSma100 = prevSma100;
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

	public double getSma21() {
		return sma21;
	}

	public void setSma21(double sma21) {
		this.sma21 = sma21;
	}

	public double getPrevSma21() {
		return prevSma21;
	}

	public void setPrevSma21(double prevSma21) {
		this.prevSma21 = prevSma21;
	}

/*	public DirectionIO getLongTermTrend() {
		return longTermTrend;
	}

	public void setLongTermTrend(DirectionIO longTermTrend) {
		this.longTermTrend = longTermTrend;
	}

	public DirectionIO getMidTermTrend() {
		return midTermTrend;
	}

	public void setMidTermTrend(DirectionIO midTermTrend) {
		this.midTermTrend = midTermTrend;
	}

	public DirectionIO getCurrentTrend() {
		return currentTrend;
	}

	public void setCurrentTrend(DirectionIO currentTrend) {
		this.currentTrend = currentTrend;
	}*/

	public double getSok() {
		return sok;
	}

	public void setSok(double sok) {
		this.sok = sok;
	}

	public double getSod() {
		return sod;
	}

	public void setSod(double sod) {
		this.sod = sod;
	}

	public long getObv() {
		return obv;
	}

	public void setObv(long obv) {
		this.obv = obv;
	}

	public double getRocv() {
		return rocv;
	}

	public void setRocv(double rocv) {
		this.rocv = rocv;
	}

	@Override
	public String toString() {
		return "StockTechnicals [stockTechnicalsId=" + stockTechnicalsId + ", stock=" + stock + ", sma50=" + sma50
				+ ", prevSma50=" + prevSma50 + ", sma100=" + sma100 + ", prevSma100=" + prevSma100 + ", sma200="
				+ sma200 + ", prevSma200=" + prevSma200 + ", sma21=" + sma21 + ", prevSma21=" + prevSma21 + ", rsi="
				+ rsi + ", lastModified=" + lastModified + ", sok=" + sok + ", sod=" + sod + ", obv=" + obv + ", rocv="
				+ rocv + "]";
	}

	
}
