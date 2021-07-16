package com.example.util.io.model;

import java.io.Serializable;

import com.example.util.io.model.type.DirectionIO;

public class StockTechnicalsIO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 113011437498353385L;

	String nseSymbol;

	double sma21;
	
	double sma50;

	double sma100;

	double sma200;

	double prevSma21;
	
	double prevSma50;
	
	double prevSma100;
	
	double prevSma200;

	double rsi;

/*	DirectionIO longTermTrend;

	DirectionIO midTermTrend;

	DirectionIO currentTrend;*/

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
/*		this.longTermTrend = longTermTrend;
		this.midTermTrend = midTermTrend;
		this.currentTrend = currentTrend;*/
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
		/*this.longTermTrend = longTermTrend;
		this.midTermTrend = midTermTrend;
		this.currentTrend = currentTrend;*/
	}

	
	public String getNseSymbol() {
		return nseSymbol;
	}

	public void setNseSymbol(String nseSymbol) {
		this.nseSymbol = nseSymbol;
	}

	public double getSma50() {
		return sma50;
	}

	public void setSma50(double sma50) {
		this.sma50 = sma50;
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

	public double getRsi() {
		return rsi;
	}

	public void setRsi(double rsi) {
		this.rsi = rsi;
	}
/*
	public DirectionIO getLongTermTrend() {
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
	}

	*/
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

	@Override
	public String toString() {
		return "StockTechnicalsIO [nseSymbol=" + nseSymbol + ", sma21=" + sma21 + ", sma50=" + sma50 + ", sma100="
				+ sma100 + ", sma200=" + sma200 + ", prevSma21=" + prevSma21 + ", prevSma50=" + prevSma50
				+ ", prevSma100=" + prevSma100 + ", prevSma200=" + prevSma200 + ", rsi=" + rsi + ", sok=" + sok
				+ ", sod=" + sod + ", obv=" + obv + ", rocv=" + rocv + "]";
	}


	
	
}
