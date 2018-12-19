package com.example.ui.model;

import java.time.LocalDate;

import com.example.model.ledger.ResearchLedger;
import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;

public class UIRenderStock {

	long stockid;
	
	String symbol;

	long qunatity;

	double averagePrice;

	double currentPrice;
	
	double buySellPrice;

	double yearLow;

	double yearHigh;

	double profitPer;

	double debtEquity;

	double roe;

	double roc;

	double researchPrice;

	LocalDate researchDate;

	public UIRenderStock() {
		this.researchDate = LocalDate.now();
	}
	
	
	public UIRenderStock(UserPortfolio userPortfolioStock, double profitPer) {
		super();
		this.symbol = userPortfolioStock.getStock().getNseSymbol();
		this.qunatity = userPortfolioStock.getQuantity();
		this.averagePrice = userPortfolioStock.getAveragePrice();
		this.currentPrice = userPortfolioStock.getStock().getStockPrice().getCurrentPrice();
		this.yearLow = userPortfolioStock.getStock().getStockPrice().getYearLow();
		this.yearHigh = userPortfolioStock.getStock().getStockPrice().getYearHigh();

		this.profitPer = profitPer;
		this.researchDate = LocalDate.now();

	}

	public UIRenderStock(Stock stock) {

		this.symbol = stock.getNseSymbol();
		this.currentPrice = stock.getStockPrice().getCurrentPrice();
		this.yearLow = stock.getStockPrice().getYearLow();
		this.yearHigh = stock.getStockPrice().getYearHigh();
		this.debtEquity = stock.getStockFactor().getDebtEquity();
		this.roe = stock.getStockFactor().getReturnOnEquity();
		this.roc = stock.getStockFactor().getReturnOnCapital();
		this.researchDate = LocalDate.now();

	}

	public UIRenderStock(ResearchLedger researchLedger, double profitPer) {

		this.symbol = researchLedger.getStock().getNseSymbol();
		this.currentPrice = researchLedger.getStock().getStockPrice().getCurrentPrice();
		this.yearLow = researchLedger.getStock().getStockPrice().getYearLow();
		this.yearHigh = researchLedger.getStock().getStockPrice().getYearHigh();
		this.researchPrice = researchLedger.getResearchPrice();
		this.researchDate = researchLedger.getResearchDate();
		this.profitPer = profitPer;
	}

	public long getStockid() {
		return stockid;
	}

	public void setStockid(long stockid) {
		this.stockid = stockid;
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

	public double getDebtEquity() {
		return debtEquity;
	}

	public void setDebtEquity(double debtEquity) {
		this.debtEquity = debtEquity;
	}

	public double getRoe() {
		return roe;
	}

	public void setRoe(double roe) {
		this.roe = roe;
	}

	public double getRoc() {
		return roc;
	}

	public void setRoc(double roc) {
		this.roc = roc;
	}

	public double getResearchPrice() {
		return researchPrice;
	}

	public void setResearchPrice(double researchPrice) {
		this.researchPrice = researchPrice;
	}

	public String getResearchDate() {
		return researchDate.toString();
	}

	public void setResearchDate(LocalDate researchDate) {
		this.researchDate = researchDate;
	}

	
	
	public double getBuySellPrice() {
		return buySellPrice;
	}


	public void setBuySellPrice(double buySellPrice) {
		this.buySellPrice = buySellPrice;
	}


	@Override
	public String toString() {
		return "UIRenderStock [stockid=" + stockid + ", symbol=" + symbol + ", qunatity=" + qunatity + ", averagePrice="
				+ averagePrice + ", currentPrice=" + currentPrice + ", buySellPrice=" + buySellPrice + ", yearLow="
				+ yearLow + ", yearHigh=" + yearHigh + ", profitPer=" + profitPer + ", debtEquity=" + debtEquity
				+ ", roe=" + roe + ", roc=" + roc + ", researchPrice=" + researchPrice + ", researchDate="
				+ researchDate + "]";
	}


}
