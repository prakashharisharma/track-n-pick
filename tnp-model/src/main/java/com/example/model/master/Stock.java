package com.example.model.master;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.example.model.stocks.StockFactor;
import com.example.model.stocks.StockPrice;
import com.example.model.stocks.StockTechnicals;
import com.example.model.stocks.UserPortfolio;
import com.example.util.io.model.StockIO.IndiceType;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "STOCK_MASTER")
public class Stock implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 291996847590635596L;

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

	@Column(name = "SECTOR_NAME")
	String sectorName;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sectorId", nullable = false)
	Sector sector;
	
	@Column(name = "IS_ACTIVE")
	boolean active = true;
	
	@Column(name = "INDICE")
	@Enumerated(EnumType.STRING)
	private IndiceType primaryIndice;
	
	@OneToOne(mappedBy = "stock", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
	private StockPrice stockPrice;
	
	@OneToOne(mappedBy = "stock", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
	private StockFactor stockFactor;
	
	@OneToOne(mappedBy = "stock", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
	private StockTechnicals technicals;
	
	@OneToMany(mappedBy = "portfolioId.stock", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<UserPortfolio> userPortfolio = new HashSet<>();
	
	public Stock() {
		super();
		this.active = true;
	}

	public Stock(String companyName, String nseSymbol, String bseCode) {
		super();
		this.companyName = companyName;
		this.nseSymbol = nseSymbol;
		this.bseCode = bseCode;
		this.active = true;
	}

	public Stock(String isinCode, String companyName, String nseSymbol, IndiceType primaryIndice, Sector sectorName) {
		super();
		this.isinCode = isinCode;
		this.companyName = companyName;
		this.nseSymbol = nseSymbol;
		this.primaryIndice = primaryIndice;
		this.sector = sectorName;
		this.sectorName = sectorName.getSectorName();
		this.active = true;
	}
	
	public Stock(String isinCode, String companyName, String nseSymbol, String sector) {
		super();
		this.isinCode = isinCode;
		this.companyName = companyName;
		this.nseSymbol = nseSymbol;
		this.sectorName = sector;
		this.active = true;
	}

	public long getStockId() {
		return stockId;
	}

	public void setStockId(long stockId) {
		this.stockId = stockId;
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

	public String getSectorName() {
		return sectorName;
	}

	public void setSectorName(String sector) {
		this.sectorName = sector;
	}

	public Sector getSector() {
		return sector;
	}

	public void setSector(Sector sectorName) {
		this.sector = sectorName;
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

	public StockTechnicals getTechnicals() {
		return technicals;
	}

	public void setTechnicals(StockTechnicals technicals) {
		this.technicals = technicals;
	}

	public IndiceType getPrimaryIndice() {
		return primaryIndice;
	}

	public void setPrimaryIndice(IndiceType primaryIndice) {
		this.primaryIndice = primaryIndice;
	}

	@Override
	public String toString() {
		return "Stock [stockId=" + stockId + ", isinCode=" + isinCode + ", companyName=" + companyName + ", nseSymbol="
				+ nseSymbol + ", bseCode=" + bseCode + ", sector=" + sectorName + ", active=" + active + ", IndiceType=" + primaryIndice+ ", stockPrice="
				+ stockPrice + ", stockFactor=" + stockFactor + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((isinCode == null) ? 0 : isinCode.hashCode());
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
		if (isinCode == null) {
			if (other.isinCode != null)
				return false;
		} else if (!isinCode.equals(other.isinCode))
			return false;
		if (nseSymbol == null) {
			if (other.nseSymbol != null)
				return false;
		} else if (!nseSymbol.equals(other.nseSymbol))
			return false;
		return true;
	}

	
}
