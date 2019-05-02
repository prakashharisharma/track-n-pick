package com.example.ui.model;

import java.io.Serializable;

import com.example.util.io.model.type.DirectionIO;

public class StockDetailsIO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 738919998494172793L;

	String nseSymbol;
	
	String sector;

	double currentPrice;
	
	double yearLow;
	
	double yearHigh;
	
	double marketCap;

	double debtEquity;

	double currentRatio;

	double quickRatio;	

	double dividend;

	double pb;

	double pe;
	
	double sectorPe;

	double returnOnEquity;

	double returnOnCapital;
	
	double rsi;

	DirectionIO longTermTrend;

	DirectionIO midTermTrend;

	DirectionIO currentTrend;

	public StockDetailsIO(String nseSymbol, String sector, double currentPrice, double yearLow,double yearHigh, double marketCap, double debtEquity, double currentRatio,
			double quickRatio, double dividend, double pb, double pe, double sectorPe, double returnOnEquity,
			double returnOnCapital, double rsi, DirectionIO longTermTrend, DirectionIO midTermTrend,
			DirectionIO currentTrend) {
		super();
		this.nseSymbol = nseSymbol;
		this.sector = sector;
		this.currentPrice = currentPrice;
		this.yearLow = yearLow;
		this.yearHigh = yearHigh;
		this.marketCap = marketCap;
		this.debtEquity = debtEquity;
		this.currentRatio = currentRatio;
		this.quickRatio = quickRatio;
		this.dividend = dividend;
		this.pb = pb;
		this.pe = pe;
		this.sectorPe = sectorPe;
		this.returnOnEquity = returnOnEquity;
		this.returnOnCapital = returnOnCapital;
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

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	public double getCurrentPrice() {
		return currentPrice;
	}

	public void setCurrentPrice(double currentPrice) {
		this.currentPrice = currentPrice;
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

	public double getMarketCap() {
		return marketCap;
	}

	public void setMarketCap(double marketCap) {
		this.marketCap = marketCap;
	}

	public double getDebtEquity() {
		return debtEquity;
	}

	public void setDebtEquity(double debtEquity) {
		this.debtEquity = debtEquity;
	}

	public double getCurrentRatio() {
		return currentRatio;
	}

	public void setCurrentRatio(double currentRatio) {
		this.currentRatio = currentRatio;
	}

	public double getQuickRatio() {
		return quickRatio;
	}

	public void setQuickRatio(double quickRatio) {
		this.quickRatio = quickRatio;
	}

	public double getDividend() {
		return dividend;
	}

	public void setDividend(double dividend) {
		this.dividend = dividend;
	}

	public double getPb() {
		return pb;
	}

	public void setPb(double pb) {
		this.pb = pb;
	}

	public double getPe() {
		return pe;
	}

	public void setPe(double pe) {
		this.pe = pe;
	}

	public double getSectorPe() {
		return sectorPe;
	}

	public void setSectorPe(double sectorPe) {
		this.sectorPe = sectorPe;
	}

	public double getReturnOnEquity() {
		return returnOnEquity;
	}

	public void setReturnOnEquity(double returnOnEquity) {
		this.returnOnEquity = returnOnEquity;
	}

	public double getReturnOnCapital() {
		return returnOnCapital;
	}

	public void setReturnOnCapital(double returnOnCapital) {
		this.returnOnCapital = returnOnCapital;
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
	
	
}
