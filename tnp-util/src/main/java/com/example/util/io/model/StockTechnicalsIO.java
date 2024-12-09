package com.example.util.io.model;

import java.io.Serializable;

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

	double ema5;
	double ema10;
	double ema20;
	double ema50;
	double ema100;
	double ema200;

	double signal;
	double macd;

	double prevSignal;
	double prevMacd;

	double rsi;
	double prevRsi;

	long volume;
	long weeklyVolume;

	long monthlyVolume;

	double adx;
	double prevAdx;

	double plusDi;

	double MinusDi;
	
	public StockTechnicalsIO() {
		super();
		// TODO Auto-generated constructor stub
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

	public double getSma20() {
		return sma20;
	}

	public void setSma20(double sma20) {
		this.sma20 = sma20;
	}

	public double getSma50() {
		return sma50;
	}

	public void setSma50(double sma50) {
		this.sma50 = sma50;
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


	public double getSignal() {
		return signal;
	}

	public void setSignal(double signal) {
		this.signal = signal;
	}

	public double getMacd() {
		return macd;
	}

	public void setMacd(double macd) {
		this.macd = macd;
	}

	public double getPrevSignal() {
		return prevSignal;
	}

	public void setPrevSignal(double prevSignal) {
		this.prevSignal = prevSignal;
	}

	public double getPrevMacd() {
		return prevMacd;
	}

	public void setPrevMacd(double prevMacd) {
		this.prevMacd = prevMacd;
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

	public long getVolume() {
		return volume;
	}

	public void setVolume(long volume) {
		this.volume = volume;
	}

	public long getWeeklyVolume() {
		return weeklyVolume;
	}

	public void setWeeklyVolume(long weeklyVolume) {
		this.weeklyVolume = weeklyVolume;
	}

	public long getMonthlyyVolume() {
		return monthlyVolume;
	}

	public void setMonthlyyVolume(long monthlyyVolume) {
		this.monthlyVolume = monthlyyVolume;
	}

	public double getAdx() {
		return adx;
	}

	public void setAdx(double adx) {
		this.adx = adx;
	}

	public double getPrevAdx() {
		return prevAdx;
	}

	public void setPrevAdx(double prevAdx) {
		this.prevAdx = prevAdx;
	}

	public double getPlusDi() {
		return plusDi;
	}

	public void setPlusDi(double plusDi) {
		this.plusDi = plusDi;
	}

	public double getMinusDi() {
		return MinusDi;
	}

	public void setMinusDi(double minusDi) {
		MinusDi = minusDi;
	}

	@Override
	public String toString() {
		return "StockTechnicalsIO{" +
				"nseSymbol='" + nseSymbol + '\'' +
				", sma5=" + sma5 +
				", sma10=" + sma10 +
				", sma20=" + sma20 +
				", sma50=" + sma50 +
				", sma100=" + sma100 +
				", sma200=" + sma200 +
				", ema5=" + ema5 +
				", ema10=" + ema10 +
				", ema20=" + ema20 +
				", ema50=" + ema50 +
				", ema100=" + ema100 +
				", ema200=" + ema200 +
				", signal=" + signal +
				", macd=" + macd +
				", prevSignal=" + prevSignal +
				", prevMacd=" + prevMacd +
				", rsi=" + rsi +
				", prevRsi=" + prevRsi +
				", volume=" + volume +
				", avgVolume=" + weeklyVolume +
				", adx=" + adx +
				", prevAdx=" + prevAdx +
				'}';
	}
}
