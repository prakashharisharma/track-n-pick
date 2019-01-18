package com.example.model.stocks;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.example.model.master.Stock;

@Entity
@Table(name = "STOCK_FACTORS")
public class StockFactor implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3247824333011472859L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "STOCK_FACTOR_ID")
	long stockFactorId;
	
	@OneToOne(fetch = FetchType.LAZY,  optional = false)
	@JoinColumn(name = "STOCK_ID", referencedColumnName ="STOCK_ID",  nullable = false)
	Stock stock;
	
	@Column(name = "MARKET_CAPITAL", columnDefinition="Decimal(10,2) default '0.00'")
	double marketCap;
	
	@Column(name = "DEBT_EQUITY", columnDefinition="Decimal(10,2) default '0.00'")
	double debtEquity;
	
	@Column(name = "DIVIDEND", columnDefinition="Decimal(10,2) default '0.00'")
	double dividend;
	
	//Price / BookValue
	@Column(name = "BOOK_VALUE", columnDefinition="Decimal(10,2) default '0.00'")
	double bookValue;
	
	//Price arning
	@Column(name = "EPS", columnDefinition="Decimal(10,2) default '0.00'")
	double eps;

	//Return On Equity / Return on Net Worth 
	@Column(name = "ROE", columnDefinition="Decimal(10,2) default '0.00'")
	double returnOnEquity;
	
	//Return On Capital Employed
	@Column(name = "ROCE", columnDefinition="Decimal(10,2) default '0.00'")
	double returnOnCapital;

	@Column(name = "FACE_VALUE", columnDefinition="Decimal(10,2) default '0.00'")
	double faceValue;

	@Column(name = "LAST_MODIFIED")
	LocalDate lastModified = LocalDate.now();
	
	
	public StockFactor() {
		super();
		
	}

	public StockFactor(StockFactor stockFactor) {
		
	}

	public long getStockFactorId() {
		return stockFactorId;
	}

	public void setStockFactorId(long stockFactorId) {
		this.stockFactorId = stockFactorId;
	}

	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
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

	public double getDividend() {
		return dividend;
	}

	public void setDividend(double dividend) {
		this.dividend = dividend;
	}

	public double getBookValue() {
		return bookValue;
	}

	public void setBookValue(double bookValue) {
		this.bookValue = bookValue;
	}

	public double getEps() {
		return eps;
	}

	public void setEps(double eps) {
		this.eps = eps;
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

	public double getFaceValue() {
		return faceValue;
	}

	public void setFaceValue(double faceValue) {
		this.faceValue = faceValue;
	}

	public LocalDate getLastModified() {
		return lastModified;
	}

	public void setLastModified(LocalDate lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String toString() {
		return "StockFactor [stockFactorId=" + stockFactorId + ",  marketCap=" + marketCap
				+ ", debtEquity=" + debtEquity + ", dividend=" + dividend + ", bookValue=" + bookValue + ", eps=" + eps
				+ ", returnOnEquity=" + returnOnEquity + ", returnOnCapital=" + returnOnCapital + ", faceValue="
				+ faceValue + ", lastModified=" + lastModified + "]";
	}

}
