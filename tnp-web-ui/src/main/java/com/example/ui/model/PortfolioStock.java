package com.example.ui.model;

public class PortfolioStock {

	String symbol;
	
	long qunatity;
	
	double averagePrice;
	
	double currentPrice;
	
	double yearLow;
	
	double yearHigh;
	
	double profitPer;

	public PortfolioStock(String symbol, long qunatity, double averagePrice, double currentPrice, double yearLow,
			double yearHigh, double profitPer) {
		super();
		this.symbol = symbol;
		this.qunatity = qunatity;
		this.averagePrice = averagePrice;
		this.currentPrice = currentPrice;
		this.yearLow = yearLow;
		this.yearHigh = yearHigh;
		
		this.profitPer = profitPer;
		
	}

	
	
	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public long getQunatity() {
		return qunatity;
	}

	public void setQunatity(long qunatity) {
		this.qunatity = qunatity;
	}

	public double getAveragePrice() {
		return averagePrice;
	}

	public void setAveragePrice(double averagePrice) {
		this.averagePrice = averagePrice;
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

	public double getProfitPer() {
		return profitPer;
	}

	public void setProfitPer(double profitPer) {
		this.profitPer = profitPer;
	}

	@Override
	public String toString() {
		return "PortfolioStock [symbol=" + symbol + ", qunatity=" + qunatity + ", averagePrice=" + averagePrice
				+ ", currentPrice=" + currentPrice + ", yearLow=" + yearLow + ", yearHigh=" + yearHigh + ", profitPer="
				+ profitPer + "]";
	}
	
}
