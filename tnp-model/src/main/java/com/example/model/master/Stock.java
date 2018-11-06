package com.example.model.master;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.example.model.stocks.StockFactor;
import com.example.model.stocks.StockPrice;
import com.example.model.stocks.UserPortfolio;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "STOCK_MASTER")
public class Stock {

	//INSERT INTO STOCK_MASTER(ISIN_CODE,COMPANY_NAME,NSE_SYMBOL, BSE_CODE,SECTOR, IS_ACTIVE) VALUES ('INE470A01017','3M India Ltd.','3MINDIA','5022','SECTOR_NAME',1);
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "STOCK_ID")
	long stockId;
	
	@Column(name = "ISIN_CODE")
	String isinCode;
	
	@Column(name = "COMPANY_NAME")
	String companyName;
	
	@JsonProperty("symbol")
	@Column(name = "NSE_SYMBOL")
	String nseSymbol;
	
	@Column(name = "BSE_CODE")
	String bseCode;

	@Column(name = "SECTOR")
	String sector;

	@Column(name = "IS_ACTIVE")
	boolean active;
	
	@OneToOne(mappedBy = "stock", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
	private StockPrice stockPrice;
	
	@OneToOne(mappedBy = "stock", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
	private StockFactor stockFactor;
	
	@OneToMany(mappedBy = "portfolioId.stock", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<UserPortfolio> userPortfolio = new HashSet<>();
	
	public Stock() {
		super();
	}

	public Stock(String companyName, String nseSymbol, String bseCode) {
		super();
		this.companyName = companyName;
		this.nseSymbol = nseSymbol;
		this.bseCode = bseCode;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getNseSymbol() {
		return nseSymbol;
	}

	public void setNseSymbol(String nseSymbol) {
		this.nseSymbol = nseSymbol;
	}

	public String getBseCode() {
		return bseCode;
	}

	public void setBseCode(String bseCode) {
		this.bseCode = bseCode;
	}

	public String getIsinCode() {
		return isinCode;
	}

	public void setIsinCode(String isinCode) {
		this.isinCode = isinCode;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
	}

	public StockPrice getStockPrice() {
		return stockPrice;
	}

	public void setStockPrice(StockPrice stockPrice) {
		this.stockPrice = stockPrice;
	}

	public StockFactor getStockFactor() {
		return stockFactor;
	}

	public void setStockFactor(StockFactor stockFactor) {
		this.stockFactor = stockFactor;
	}

	@Override
	public String toString() {
		return "Stock [stockId=" + stockId + ", isinCode=" + isinCode + ", companyName=" + companyName + ", nseSymbol="
				+ nseSymbol + ", bseCode=" + bseCode + ", sector=" + sector + ", active=" + active + ", stockPrice="
				+ stockPrice + ", stockFactor=" + stockFactor + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nseSymbol == null) ? 0 : nseSymbol.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Stock other = (Stock) obj;
		if (nseSymbol == null) {
			if (other.nseSymbol != null)
				return false;
		} else if (!nseSymbol.equals(other.nseSymbol))
			return false;
		return true;
	}

}
