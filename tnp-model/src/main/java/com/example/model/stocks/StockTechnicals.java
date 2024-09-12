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
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "STOCK_TECHNICALS_ID")
	long stockTechnicalsId;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STOCK_ID", referencedColumnName ="STOCK_ID",  nullable = false)
	Stock stock;

	@Column(name = "BHAV_DATE")
	LocalDate bhavDate = LocalDate.now();
	@Column(name = "SMA_5", columnDefinition="decimal(10,2) default '0.00'")
	double sma5;

	@Column(name = "PREV_SMA_5", columnDefinition="decimal(10,2) default '0.00'")
	double prevSma5;

	@Column(name = "SMA_10", columnDefinition="decimal(10,2) default '0.00'")
	double sma10;

	@Column(name = "PREV_SMA_10", columnDefinition="decimal(10,2) default '0.00'")
	double prevSma10;

	@Column(name = "SMA_20", columnDefinition="decimal(10,2) default '0.00'")
	double sma20;

	@Column(name = "PREV_SMA_20", columnDefinition="decimal(10,2) default '0.00'")
	double prevSma20;

	@Column(name = "SMA_50", columnDefinition="decimal(10,2) default '0.00'")
	double sma50;
	
	@Column(name = "PREV_SMA_50", columnDefinition="decimal(10,2) default '0.00'")
	double prevSma50;
	
	@Column(name = "SMA_100", columnDefinition="decimal(10,2) default '0.00'")
	double sma100;
	
	@Column(name = "PREV_SMA_100", columnDefinition="decimal(10,2) default '0.00'")
	double prevSma100;
	
	@Column(name = "SMA_200", columnDefinition="decimal(10,2) default '0.00'")
	double sma200;
	
	@Column(name = "PREV_SMA_200", columnDefinition="decimal(10,2) default '0.00'")
	double prevSma200;


	@Column(name = "EMA_5", columnDefinition="decimal(10,2) default '0.00'")
	double ema5;

	@Column(name = "PREV_EMA_5", columnDefinition="decimal(10,2) default '0.00'")
	double prevEma5;

	@Column(name = "EMA_10", columnDefinition="decimal(10,2) default '0.00'")
	double ema10;

	@Column(name = "PREV_EMA_10", columnDefinition="decimal(10,2) default '0.00'")
	double prevEma10;

	@Column(name = "EMA_20", columnDefinition="decimal(10,2) default '0.00'")
	double ema20;

	@Column(name = "PREV_EMA_20", columnDefinition="decimal(10,2) default '0.00'")
	double prevEma20;

	@Column(name = "EMA_50", columnDefinition="decimal(10,2) default '0.00'")
	double ema50;

	@Column(name = "PREV_EMA_50", columnDefinition="decimal(10,2) default '0.00'")
	double prevEma50;

	@Column(name = "EMA_100", columnDefinition="decimal(10,2) default '0.00'")
	double ema100;

	@Column(name = "PREV_EMA_100", columnDefinition="decimal(10,2) default '0.00'")
	double prevEma100;

	@Column(name = "EMA_200", columnDefinition="decimal(10,2) default '0.00'")
	double ema200;

	@Column(name = "PREV_EMA_200", columnDefinition="decimal(10,2) default '0.00'")
	double prevEma200;

	
	@Column(name = "RSI", columnDefinition="decimal(10,2) default '0.00'")
	double rsi;

	@Column(name = "PREV_RSI", columnDefinition="decimal(10,2) default '0.00'")
	double prevRsi;

	@Column(name = "AVG_RSI", columnDefinition="decimal(10,2) default '0.00'")
	double avgRsi;

	@Column(name = "MACD", columnDefinition="decimal(10,2) default '0.00'")
	double macd;

	@Column(name = "PREV_MACD", columnDefinition="decimal(10,2) default '0.00'")
	double prevMacd;

	@Column(name = "AVG_MACD", columnDefinition="decimal(10,2) default '0.00'")
	double avgMacd;

	@Column(name = "SIGNAL_LINE", columnDefinition="decimal(10,2) default '0.00'")
	double signal;

	@Column(name = "PREV_SIGNAL_LINE", columnDefinition="decimal(10,2) default '0.00'")
	double prevSignal;
	
	@Column(name = "LAST_MODIFIED")
	LocalDate lastModified = LocalDate.now();

	@Column(name = "SOK", columnDefinition="decimal(10,2) default '0.00'")
	double sok;
	
	@Column(name = "SOD", columnDefinition="decimal(10,2) default '0.00'")
	double sod;
	
	@Column(name = "OBV")
	long obv;
	
	@Column(name = "ROCV", columnDefinition="decimal(10,2) default '0.00'")
	double rocv;
	
	@Column(name = "VOLUME")
	Long volume;
	
	@Column(name = "AVG_VOLUME")
	Long avgVolume;

	@Column(name = "ADX", columnDefinition="decimal(10,2) default '0.00'")
	double adx;

	@Column(name = "AVG_ADX", columnDefinition="decimal(10,2) default '0.00'")
	double avgAdx;

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

	public LocalDate getBhavDate() {
		return bhavDate;
	}

	public void setBhavDate(LocalDate bhavDate) {
		this.bhavDate = bhavDate;
	}

	public double getSma5() {
		return sma5;
	}

	public void setSma5(double sma5) {
		this.sma5 = sma5;
	}

	public double getPrevSma5() {
		return prevSma5;
	}

	public void setPrevSma5(double prevSma5) {
		this.prevSma5 = prevSma5;
	}

	public double getSma10() {
		return sma10;
	}

	public void setSma10(double sma10) {
		this.sma10 = sma10;
	}

	public double getPrevSma10() {
		return prevSma10;
	}

	public void setPrevSma10(double prevSma10) {
		this.prevSma10 = prevSma10;
	}

	public double getSma20() {
		return sma20;
	}

	public void setSma20(double sma20) {
		this.sma20 = sma20;
	}

	public double getPrevSma20() {
		return prevSma20;
	}

	public void setPrevSma20(double prevSma20) {
		this.prevSma20 = prevSma20;
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

	public double getSma100() {
		return sma100;
	}

	public void setSma100(double sma100) {
		this.sma100 = sma100;
	}

	public double getPrevSma100() {
		return prevSma100;
	}

	public void setPrevSma100(double prevSma100) {
		this.prevSma100 = prevSma100;
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

	public double getEma5() {
		return ema5;
	}

	public void setEma5(double ema5) {
		this.ema5 = ema5;
	}

	public double getPrevEma5() {
		return prevEma5;
	}

	public void setPrevEma5(double prevEma5) {
		this.prevEma5 = prevEma5;
	}

	public double getEma10() {
		return ema10;
	}

	public void setEma10(double ema10) {
		this.ema10 = ema10;
	}

	public double getPrevEma10() {
		return prevEma10;
	}

	public void setPrevEma10(double prevEma10) {
		this.prevEma10 = prevEma10;
	}

	public double getEma20() {
		return ema20;
	}

	public void setEma20(double ema20) {
		this.ema20 = ema20;
	}

	public double getPrevEma20() {
		return prevEma20;
	}

	public void setPrevEma20(double prevEma20) {
		this.prevEma20 = prevEma20;
	}

	public double getEma50() {
		return ema50;
	}

	public void setEma50(double ema50) {
		this.ema50 = ema50;
	}

	public double getPrevEma50() {
		return prevEma50;
	}

	public void setPrevEma50(double prevEma50) {
		this.prevEma50 = prevEma50;
	}

	public double getEma100() {
		return ema100;
	}

	public void setEma100(double ema100) {
		this.ema100 = ema100;
	}

	public double getPrevEma100() {
		return prevEma100;
	}

	public void setPrevEma100(double prevEma100) {
		this.prevEma100 = prevEma100;
	}

	public double getEma200() {
		return ema200;
	}

	public void setEma200(double ema200) {
		this.ema200 = ema200;
	}

	public double getPrevEma200() {
		return prevEma200;
	}

	public void setPrevEma200(double prevEma200) {
		this.prevEma200 = prevEma200;
	}

	public double getRsi() {
		return rsi;
	}

	public void setRsi(double rsi) {
		this.rsi = rsi;
	}

	public double getPrevRsi() {
		return prevRsi;
	}

	public void setPrevRsi(double prevRsi) {
		this.prevRsi = prevRsi;
	}

	public double getMacd() {
		return macd;
	}

	public void setMacd(double macd) {
		this.macd = macd;
	}

	public double getPrevMacd() {
		return prevMacd;
	}

	public void setPrevMacd(double prevMacd) {
		this.prevMacd = prevMacd;
	}

	public double getSignal() {
		return signal;
	}

	public void setSignal(double signal) {
		this.signal = signal;
	}

	public double getPrevSignal() {
		return prevSignal;
	}

	public void setPrevSignal(double prevSignal) {
		this.prevSignal = prevSignal;
	}

	public LocalDate getLastModified() {
		return lastModified;
	}

	public void setLastModified(LocalDate lastModified) {
		this.lastModified = lastModified;
	}

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

	public Long getVolume() {
		return volume!= null ? volume : 1l;
	}

	public void setVolume(Long volume) {
		this.volume = volume;
	}

	public Long getAvgVolume() {
		return avgVolume;
	}

	public void setAvgVolume(Long avgVolume) {
		this.avgVolume = avgVolume;
	}

	public double getAdx() {
		return adx;
	}

	public void setAdx(double adx) {
		this.adx = adx;
	}

	public double getAvgAdx() {
		return avgAdx;
	}

	public void setAvgAdx(double avgAdx) {
		this.avgAdx = avgAdx;
	}

	public double getAvgRsi() {
		return avgRsi;
	}

	public void setAvgRsi(double avgRsi) {
		this.avgRsi = avgRsi;
	}

	public double getAvgMacd() {
		return avgMacd;
	}

	public void setAvgMacd(double avgMacd) {
		this.avgMacd = avgMacd;
	}

	@Override
	public String toString() {
		return "StockTechnicals [stockTechnicalsId=" + stockTechnicalsId + ", stock=" + stock + ", sma50=" + sma50
				+ ", prevSma50=" + prevSma50 + ", sma100=" + sma100 + ", prevSma100=" + prevSma100 + ", sma200="
				+ sma200 + ", prevSma200=" + prevSma200 + ", sma20=" + sma20 + ", prevSma20=" + prevSma20 + ", rsi="
				+ rsi + ", lastModified=" + lastModified + ", sok=" + sok + ", sod=" + sod + ", obv=" + obv + ", rocv="
				+ rocv + "]";
	}

	
}
