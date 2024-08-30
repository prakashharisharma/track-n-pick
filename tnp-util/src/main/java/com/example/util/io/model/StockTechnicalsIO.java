package com.example.util.io.model;

import java.io.Serializable;

import com.example.util.io.model.type.DirectionIO;

public class StockTechnicalsIO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 113011437498353385L;

	String nseSymbol;

	double sma5;

	double sma10;

	double sma20;
	double sma50;
	double sma100;
	double sma200;

	double prevSma5;

	double prevSma10;

	double prevSma20;
	double prevSma50;
	double prevSma100;
	double prevSma200;

	double ema5;
	double ema10;
	double ema20;
	double ema50;
	double ema100;
	double ema200;

	double prevEma5;
	double prevEma10;
	double prevEma20;
	double prevEma50;
	double prevEma100;
	double prevEma200;

	double signalLine;
	double macd;

	double prevSignalLine;
	double prevMacd;

	double rsi;

	double prevRsi;

	double sok;
	
	double sod;
	
	long obv;
	
	double rocv;
	
	long volume;
	
	long avgVolume;
	
	public StockTechnicalsIO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public StockTechnicalsIO(String nseSymbol, double sma50, double prevSma50, double sma100, double sma200,
			double prevSma200, double rsi) {
		super();
		this.nseSymbol = nseSymbol;
		this.sma50 = sma50;
		this.prevSma50 = prevSma50;
		this.sma100 = sma100;
		this.sma200 = sma200;
		this.prevSma200 = prevSma200;
		this.rsi = rsi;

	}

	public StockTechnicalsIO(String nseSymbol, double sma50, double prevSma50, double sma100, double prevSma100, double sma200,
			double prevSma200, double rsi) {
		super();
		this.nseSymbol = nseSymbol;
		this.sma50 = sma50;
		this.prevSma50 = prevSma50;
		this.sma100 = sma100;
		this.prevSma100 = prevSma100;
		this.sma200 = sma200;
		this.prevSma200 = prevSma200;
		this.rsi = rsi;

	}

	
	public String getNseSymbol() {
		return nseSymbol;
	}

	public void setNseSymbol(String nseSymbol) {
		this.nseSymbol = nseSymbol;
	}

	public double getSma5() {
		return sma5;
	}

	public void setSma5(double sma5) {
		this.sma5 = sma5;
	}

	public double getSma10() {
		return sma10;
	}

	public void setSma10(double sma10) {
		this.sma10 = sma10;
	}

	public double getPrevSma5() {
		return prevSma5;
	}

	public void setPrevSma5(double prevSma5) {
		this.prevSma5 = prevSma5;
	}

	public double getPrevSma10() {
		return prevSma10;
	}

	public void setPrevSma10(double prevSma10) {
		this.prevSma10 = prevSma10;
	}

	public double getSma50() {
		return sma50;
	}

	public void setSma50(double sma50) {
		this.sma50 = sma50;
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

	public double getEma10() {
		return ema10;
	}

	public void setEma10(double ema10) {
		this.ema10 = ema10;
	}

	public double getEma20() {
		return ema20;
	}

	public void setEma20(double ema20) {
		this.ema20 = ema20;
	}

	public double getEma50() {
		return ema50;
	}

	public void setEma50(double ema50) {
		this.ema50 = ema50;
	}

	public double getEma100() {
		return ema100;
	}

	public void setEma100(double ema100) {
		this.ema100 = ema100;
	}

	public double getEma200() {
		return ema200;
	}

	public void setEma200(double ema200) {
		this.ema200 = ema200;
	}

	public double getPrevEma5() {
		return prevEma5;
	}

	public void setPrevEma5(double prevEma5) {
		this.prevEma5 = prevEma5;
	}

	public double getPrevEma10() {
		return prevEma10;
	}

	public void setPrevEma10(double prevEma10) {
		this.prevEma10 = prevEma10;
	}

	public double getPrevEma20() {
		return prevEma20;
	}

	public void setPrevEma20(double prevEma20) {
		this.prevEma20 = prevEma20;
	}

	public double getPrevEma50() {
		return prevEma50;
	}

	public void setPrevEma50(double prevEma50) {
		this.prevEma50 = prevEma50;
	}

	public double getPrevEma100() {
		return prevEma100;
	}

	public void setPrevEma100(double prevEma100) {
		this.prevEma100 = prevEma100;
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

	public double getPrevSma100() {
		return prevSma100;
	}

	public void setPrevSma100(double prevSma100) {
		this.prevSma100 = prevSma100;
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

	public long getVolume() {
		return volume;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	public long getAvgVolume() {
		return avgVolume;
	}

	public void setAvgVolume(long avgVolume) {
		this.avgVolume = avgVolume;
	}

	public double getSignalLine() {
		return signalLine;
	}

	public void setSignalLine(double signalLine) {
		this.signalLine = signalLine;
	}

	public double getMacd() {
		return macd;
	}

	public void setMacd(double macd) {
		this.macd = macd;
	}

	public double getPrevSignalLine() {
		return prevSignalLine;
	}

	public void setPrevSignalLine(double prevSignalLine) {
		this.prevSignalLine = prevSignalLine;
	}

	public double getPrevMacd() {
		return prevMacd;
	}

	public double getPrevRsi() {
		return prevRsi;
	}

	public void setPrevRsi(double prevRsi) {
		this.prevRsi = prevRsi;
	}

	public void setPrevMacd(double prevMacd) {
		this.prevMacd = prevMacd;
	}

	@Override
	public String toString() {
		return "StockTechnicalsIO [nseSymbol=" + nseSymbol + ", sma20=" + sma20 + ", sma50=" + sma50 + ", sma100="
				+ sma100 + ", sma200=" + sma200 + ", prevSma20=" + prevSma20 + ", prevSma50=" + prevSma50
				+ ", prevSma100=" + prevSma100 + ", prevSma200=" + prevSma200 + ", rsi=" + rsi + ", sok=" + sok
				+ ", sod=" + sod + ", obv=" + obv + ", rocv=" + rocv + "]";
	}


	
	
}
