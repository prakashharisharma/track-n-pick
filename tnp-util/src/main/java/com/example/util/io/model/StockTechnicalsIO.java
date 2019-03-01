package com.example.util.io.model;

import java.io.Serializable;

import com.example.util.io.model.type.DirectionIO;

public class StockTechnicalsIO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 113011437498353385L;

	String nseSymbol;

	double sma50;

	double prevSma50;

	double sma100;

	double sma200;

	double prevSma200;

	double rsi;

	DirectionIO longTermTrend;

	DirectionIO midTermTrend;

	DirectionIO currentTrend;

	public StockTechnicalsIO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public StockTechnicalsIO(String nseSymbol, double sma50, double prevSma50, double sma100, double sma200,
			double prevSma200, double rsi, DirectionIO longTermTrend, DirectionIO midTermTrend,
			DirectionIO currentTrend) {
		super();
		this.nseSymbol = nseSymbol;
		this.sma50 = sma50;
		this.prevSma50 = prevSma50;
		this.sma100 = sma100;
		this.sma200 = sma200;
		this.prevSma200 = prevSma200;
		this.rsi = rsi;
		this.longTermTrend = longTermTrend;
		this.midTermTrend = midTermTrend;
		this.currentTrend = currentTrend;
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

	@Override
	public String toString() {
		return "StockTechnicalsIO [nseSymbol=" + nseSymbol + ", sma50=" + sma50 + ", prevSma50=" + prevSma50
				+ ", sma100=" + sma100 + ", sma200=" + sma200 + ", prevSma200=" + prevSma200 + ", rsi=" + rsi
				+ ", longTermTrend=" + longTermTrend + ", midTermTrend=" + midTermTrend + ", currentTrend="
				+ currentTrend + "]";
	}
	
	
	
}
