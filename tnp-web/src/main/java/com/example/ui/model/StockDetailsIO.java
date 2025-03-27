package com.example.ui.model;

import java.io.Serializable;

import com.example.util.io.model.StockIO.IndiceType;

public class StockDetailsIO implements Serializable {

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

	double sectorPb;

	double returnOnEquity;

	double returnOnCapital;

	String valuation;

	IndiceType indice;

	double rsi;

	double sok;

	double sod;

	long obv;

	double rocv;

	double ema20;

	double ema50;

	double ema100;

	double ema200;

	String crossOver;

	String breakOut;

	public StockDetailsIO(String nseSymbol, String sector, double currentPrice, double yearLow, double yearHigh,
			double marketCap, double debtEquity, double currentRatio, double quickRatio, double dividend, double pb,
			double pe, double sectorPe, double sectorPb, double returnOnEquity, double returnOnCapital, double rsi,
			String valuation, IndiceType indice,  double ema20,
			double ema50, double ema100, double ema200) {
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
		this.sectorPb = sectorPb;
		this.returnOnEquity = returnOnEquity;
		this.returnOnCapital = returnOnCapital;
		this.rsi = rsi;
		this.valuation = valuation;
		this.indice = indice;

		this.sok = sok;

		this.sod = sod;

		this.obv = obv;

		this.rocv = rocv;

		this.ema20 = ema20;

		this.ema50 = ema50;

		this.ema100 = ema100;

		this.ema200 = ema200;

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

	public double getSectorPb() {
		return sectorPb;
	}

	public void setSectorPb(double sectorPb) {
		this.sectorPb = sectorPb;
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

	public String getValuation() {
		return valuation;
	}

	public void setValuation(String valuation) {
		this.valuation = valuation;
	}

	public IndiceType getIndice() {
		return indice;
	}

	public void setIndice(IndiceType indice) {
		this.indice = indice;
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

	public String getCrossOver() {
		return crossOver;
	}

	public void setCrossOver(String crossOver) {
		this.crossOver = crossOver;
	}

	public String getBreakOut() {
		return breakOut;
	}

	public void setBreakOut(String breakOut) {
		this.breakOut = breakOut;
	}

}
