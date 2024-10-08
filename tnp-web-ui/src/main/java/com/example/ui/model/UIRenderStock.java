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

	double ema5;

	double ema10;

	double ema20;

	double ema50;

	double ema100;

	double ema200;

	double roc;

	double pe;

	double sectorPe;

	double pb;

	double sectorPb;

	double sma50;

	double sma200;

	double rsi;

	double xirr;
	
	String category;

	double researchPrice;

	double peDifference;

	LocalDate researchDate;

	IndiceType indice;

	double invested;

	double currentValue;

	boolean overValued;

	boolean bearish;

	boolean bullish;

	TechnicalsResearchService.RsiTrend rsiTrend;

	public boolean isBullish() {
		return bullish;
	}

	public void setBullish(boolean bullish) {
		this.bullish = bullish;
	}

	public UIRenderStock() {
		this.researchDate = LocalDate.now();
	}

	public UIRenderStock(UserPortfolio userPortfolioStock, double profitPer, double invested, double currentValue) {
		super();
		this.symbol = userPortfolioStock.getStock().getNseSymbol();
		this.qunatity = userPortfolioStock.getQuantity();
		this.averagePrice = userPortfolioStock.getAveragePrice();
		this.currentPrice = userPortfolioStock.getStock().getStockPrice().getCurrentPrice();
		this.profitPer = profitPer;
		this.researchDate = LocalDate.now();
		this.name = userPortfolioStock.getStock().getCompanyName();
		this.sector = userPortfolioStock.getStock().getSector().getSectorName();
		this.invested = invested;
		this.currentValue = currentValue;
	}

	public UIRenderStock(Stock stock) {

		this.symbol = stock.getNseSymbol();
		this.indice = stock.getPrimaryIndice();
		this.currentPrice = stock.getStockPrice().getCurrentPrice();
		//this.yearLow = stock.getStockPrice().getYearLow();
		//this.yearHigh = stock.getStockPrice().getYearHigh();
		//this.debtEquity = stock.getStockFactor().getDebtEquity();
		//this.roe = stock.getStockFactor().getReturnOnEquity();
		//this.roc = stock.getStockFactor().getReturnOnCapital();
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
		this.bullish = goldenCross;

	}
	public UIRenderStock(ResearchLedgerTechnical researchLedger, double profitPer, double pe, double pb) {

		this.symbol = researchLedger.getStock().getNseSymbol();
		this.currentPrice = researchLedger.getStock().getStockPrice().getCurrentPrice();
		this.yearLow = researchLedger.getStock().getStockPrice().getYearLow();
		this.yearHigh = researchLedger.getStock().getStockPrice().getYearHigh();
		this.researchPrice = researchLedger.getResearchPrice();
		//if (researchLedger.getEntryCrossOver() != null) {
			this.researchDate = researchLedger.getResearchDate();
		//}
		this.profitPer = profitPer;
		this.indice = researchLedger.getStock().getPrimaryIndice();
		this.pe = pe;
		this.pb = pb;
		this.debtEquity = researchLedger.getStock().getStockFactor().getDebtEquity();
		this.roe = researchLedger.getStock().getStockFactor().getReturnOnEquity();

		this.sma50 = researchLedger.getStock().getTechnicals().getSma50();
		this.sma200 = researchLedger.getStock().getTechnicals().getSma200();
		this.rsi = researchLedger.getStock().getTechnicals().getRsi();
		
		this.category = researchLedger.getResearchRule().toString();

	}
	public UIRenderStock(ResearchLedgerFundamental researchLedger, double profitPer, double pe, double pb,
						 double sectorPe, double sectorPb, boolean isBullish) {

		this.symbol = researchLedger.getStock().getNseSymbol();
		this.currentPrice = researchLedger.getStock().getStockPrice().getCurrentPrice();
		//this.yearLow = researchLedger.getStock().getStockPrice().getYearLow();
		//this.yearHigh = researchLedger.getStock().getStockPrice().getYearHigh();
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
		this.sectorPe = sectorPe;
		this.sectorPb = sectorPb;
		this.bullish = isBullish;

	}

	public UIRenderStock(ResearchLedgerTechnical researchLedger, double profitPer, double pe, double pb,
			double peDifference) {

		this.symbol = researchLedger.getStock().getNseSymbol();
		this.currentPrice = researchLedger.getStock().getStockPrice().getCurrentPrice();
		this.yearLow = researchLedger.getStock().getStockPrice().getYearLow();
		this.yearHigh = researchLedger.getStock().getStockPrice().getYearHigh();
		this.researchPrice = researchLedger.getResearchPrice();
		//if (researchLedger.getEntryCrossOver() != null) {
			this.researchDate = researchLedger.getResearchDate();
		//}
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

	public boolean isBearish() {
		return bearish;
	}

	public void setBearish(boolean bearish) {
		this.bearish = bearish;
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
