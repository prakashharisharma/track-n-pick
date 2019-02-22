package com.example.ui.model;
import java.time.LocalDate;

import com.example.model.ledger.ResearchLedger;
import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.util.io.model.IndiceType;

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
	
	double pe;
	
	double pb;
	
	double sma50;
	
	double sma200;
	
	double rsi;

	double researchPrice;

	LocalDate researchDate;

	IndiceType indice;
	
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

	public UIRenderStock(ResearchLedger researchLedger, double profitPer, double pe, double pb) {

		this.symbol = researchLedger.getStock().getNseSymbol();
		this.currentPrice = researchLedger.getStock().getStockPrice().getCurrentPrice();
		this.yearLow = researchLedger.getStock().getStockPrice().getYearLow();
		this.yearHigh = researchLedger.getStock().getStockPrice().getYearHigh();
		this.researchPrice = researchLedger.getEntryPrice();
		this.researchDate = researchLedger.getEntryhDate();
		this.profitPer = profitPer;
		this.indice = researchLedger.getStock().getPrimaryIndice();
		this.pe = pe;
		this.pb = pb;
		this.debtEquity = researchLedger.getStock().getStockFactor().getDebtEquity();
		this.roe= researchLedger.getStock().getStockFactor().getReturnOnEquity();

		this.sma50 = researchLedger.getStock().getTechnicals().getSma50();
		this.sma200= researchLedger.getStock().getTechnicals().getSma200();
		this.rsi= researchLedger.getStock().getTechnicals().getRsi();
		
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


	public double getPe() {
		return pe;
	}


	public void setPe(double pe) {
		this.pe = pe;
	}

	public double getPb() {
		return pb;
	}


	public void setPb(double pb) {
		this.pb = pb;
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


	public IndiceType getIndice() {
		return indice;
	}


	public void setIndice(IndiceType indice) {
		this.indice = indice;
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
