package com.example.ui.model;

import java.time.LocalDate;

import com.example.model.ledger.ResearchLedgerFundamental;
import com.example.model.ledger.ResearchLedgerTechnical;
import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.service.TechnicalsResearchService;
import com.example.util.io.model.StockIO.IndiceType;

public class UIRenderStock {

	long stockid;

	String symbol;

	String name;

	String sector;

	long sectorid;

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

	double xirr;
	
	String category;

	double researchPrice;

	double peDifference;

	LocalDate researchDate;

	IndiceType indice;

	boolean overValued;

	boolean deathCross;

	boolean goldenCross;

	TechnicalsResearchService.RsiTrend rsiTrend;

	public boolean isGoldenCross() {
		return goldenCross;
	}

	public void setGoldenCross(boolean goldenCross) {
		this.goldenCross = goldenCross;
	}

	public UIRenderStock() {
		this.researchDate = LocalDate.now();
	}

	public UIRenderStock(UserPortfolio userPortfolioStock, double profitPer, boolean overValued, boolean deathCross, double rsi) {
		super();
		this.symbol = userPortfolioStock.getStock().getNseSymbol();
		this.qunatity = userPortfolioStock.getQuantity();
		this.averagePrice = userPortfolioStock.getAveragePrice();
		this.currentPrice = userPortfolioStock.getStock().getStockPrice().getCurrentPrice();
		this.yearLow = userPortfolioStock.getStock().getStockPrice().getYearLow();
		this.yearHigh = userPortfolioStock.getStock().getStockPrice().getYearHigh();
		this.overValued = overValued;
		this.profitPer = profitPer;
		this.researchDate = LocalDate.now();
		this.name = userPortfolioStock.getStock().getCompanyName();
		this.sector = userPortfolioStock.getStock().getSector().getSectorName();
		this.deathCross = deathCross;
		this.rsi = rsi;
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

	public UIRenderStock(ResearchLedgerFundamental researchLedger, double profitPer, double pe, double pb, boolean goldenCross, TechnicalsResearchService.RsiTrend rsiTrend) {

		this.symbol = researchLedger.getStock().getNseSymbol();
		this.currentPrice = researchLedger.getStock().getStockPrice().getCurrentPrice();
		this.yearLow = researchLedger.getStock().getStockPrice().getYearLow();
		this.yearHigh = researchLedger.getStock().getStockPrice().getYearHigh();
		this.researchPrice = researchLedger.getEntryValuation().getPrice();
		if (researchLedger.getEntryValuation() != null) {
			this.researchDate = researchLedger.getEntryValuation().getResearchDate();
		}
		this.profitPer = profitPer;
		this.indice = researchLedger.getStock().getPrimaryIndice();
		this.pe = pe;
		this.pb = pb;
		this.debtEquity = researchLedger.getStock().getStockFactor().getDebtEquity();
		this.roe = researchLedger.getStock().getStockFactor().getReturnOnEquity();

		this.sma50 = researchLedger.getStock().getTechnicals().getSma50();
		this.sma200 = researchLedger.getStock().getTechnicals().getSma200();
		this.rsiTrend = rsiTrend;
		this.goldenCross = goldenCross;

	}
	public UIRenderStock(ResearchLedgerTechnical researchLedger, double profitPer, double pe, double pb) {

		this.symbol = researchLedger.getStock().getNseSymbol();
		this.currentPrice = researchLedger.getStock().getStockPrice().getCurrentPrice();
		this.yearLow = researchLedger.getStock().getStockPrice().getYearLow();
		this.yearHigh = researchLedger.getStock().getStockPrice().getYearHigh();
		this.researchPrice = researchLedger.getEntryCrossOver().getPrice();
		if (researchLedger.getEntryCrossOver() != null) {
			this.researchDate = researchLedger.getEntryCrossOver().getResearchDate();
		}
		this.profitPer = profitPer;
		this.indice = researchLedger.getStock().getPrimaryIndice();
		this.pe = pe;
		this.pb = pb;
		this.debtEquity = researchLedger.getStock().getStockFactor().getDebtEquity();
		this.roe = researchLedger.getStock().getStockFactor().getReturnOnEquity();

		this.sma50 = researchLedger.getStock().getTechnicals().getSma50();
		this.sma200 = researchLedger.getStock().getTechnicals().getSma200();
		this.rsi = researchLedger.getStock().getTechnicals().getRsi();
		
		this.category = researchLedger.getEntryCrossOver().getCrossOverCategory().toString();

	}
	public UIRenderStock(ResearchLedgerFundamental researchLedger, double profitPer, double pe, double pb,
						 double peDifference, boolean goldenCross, TechnicalsResearchService.RsiTrend rsiTrend) {

		this.symbol = researchLedger.getStock().getNseSymbol();
		this.currentPrice = researchLedger.getStock().getStockPrice().getCurrentPrice();
		this.yearLow = researchLedger.getStock().getStockPrice().getYearLow();
		this.yearHigh = researchLedger.getStock().getStockPrice().getYearHigh();
		this.researchPrice = researchLedger.getEntryValuation().getPrice();
		if (researchLedger.getEntryValuation() != null) {
			this.researchDate = researchLedger.getEntryValuation().getResearchDate();
		}
		this.profitPer = profitPer;
		this.indice = researchLedger.getStock().getPrimaryIndice();
		this.pe = pe;
		this.pb = pb;
		this.debtEquity = researchLedger.getStock().getStockFactor().getDebtEquity();
		this.roe = researchLedger.getStock().getStockFactor().getReturnOnEquity();
		this.roc = researchLedger.getStock().getStockFactor().getReturnOnCapital();
		//this.sma50 = researchLedger.getStock().getTechnicals().getSma50();
		//this.sma200 = researchLedger.getStock().getTechnicals().getSma200();
		//this.rsi = researchLedger.getStock().getTechnicals().getRsi();
		this.peDifference = peDifference;
		this.goldenCross = goldenCross;
		this.rsiTrend = rsiTrend;
	}

	public UIRenderStock(ResearchLedgerTechnical researchLedger, double profitPer, double pe, double pb,
			double peDifference) {

		this.symbol = researchLedger.getStock().getNseSymbol();
		this.currentPrice = researchLedger.getStock().getStockPrice().getCurrentPrice();
		this.yearLow = researchLedger.getStock().getStockPrice().getYearLow();
		this.yearHigh = researchLedger.getStock().getStockPrice().getYearHigh();
		this.researchPrice = researchLedger.getEntryCrossOver().getPrice();
		if (researchLedger.getEntryCrossOver() != null) {
			this.researchDate = researchLedger.getEntryCrossOver().getResearchDate();
		}
		this.profitPer = profitPer;
		this.indice = researchLedger.getStock().getPrimaryIndice();
		this.pe = pe;
		this.pb = pb;
		this.debtEquity = researchLedger.getStock().getStockFactor().getDebtEquity();
		this.roe = researchLedger.getStock().getStockFactor().getReturnOnEquity();

		this.sma50 = researchLedger.getStock().getTechnicals().getSma50();
		this.sma200 = researchLedger.getStock().getTechnicals().getSma200();
		this.rsi = researchLedger.getStock().getTechnicals().getRsi();
		this.peDifference = peDifference;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	public long getSectorid() {
		return sectorid;
	}

	public void setSectorid(long sectorid) {
		this.sectorid = sectorid;
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

	public boolean isOverValued() {
		return overValued;
	}

	public void setOverValued(boolean overValued) {
		this.overValued = overValued;
	}

	public double getPeDifference() {
		return peDifference;
	}

	public void setPeDifference(double peDifference) {
		this.peDifference = peDifference;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public boolean isDeathCross() {
		return deathCross;
	}

	public void setDeathCross(boolean deathCross) {
		this.deathCross = deathCross;
	}

	public TechnicalsResearchService.RsiTrend getRsiTrend() {
		return rsiTrend;
	}

	public double getXirr() {
		return xirr;
	}

	public void setXirr(double xirr) {
		this.xirr = xirr;
	}

	public void setRsiTrend(TechnicalsResearchService.RsiTrend rsiTrend) {
		this.rsiTrend = rsiTrend;
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
