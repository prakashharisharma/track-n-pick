package com.example.storage.model.deprecated;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Deprecated
@Document(collection = "stocks")
public class Stock{

	@Id
	private String id;

	String isinCode;

	String companyName;

	String nseSymbol;

	String bseCode;

	String sectorName;
	
	List<StockPriceD> stockPrices;

	//List<StockTechnicals> stockTechnicals;
	
	public Stock() {
		super();
		this.stockPrices = new ArrayList<>();
		//this.stockTechnicals = new ArrayList<>();
	}

	public Stock(String nseSymbol) {
		super();
		this.nseSymbol = nseSymbol;
		this.stockPrices = new ArrayList<>();
		//this.stockTechnicals = new ArrayList<>();
	}

	public Stock(String isinCode, String companyName, String nseSymbol, String bseCode, String sectorName) {
		super();
		this.isinCode = isinCode;
		this.companyName = companyName;
		this.nseSymbol = nseSymbol;
		this.bseCode = bseCode;
		this.sectorName = sectorName;
		this.stockPrices = new ArrayList<>();
		//this.stockTechnicals = new ArrayList<>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIsinCode() {
		return isinCode;
	}

	public void setIsinCode(String isinCode) {
		this.isinCode = isinCode;
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

	public String getSectorName() {
		return sectorName;
	}

	public void setSectorName(String sectorName) {
		this.sectorName = sectorName;
	}

	public List<StockPriceD> getStockPrices() {
		return stockPrices;
	}

	public void setStockPrices(List<StockPriceD> stockPrices) {
		this.stockPrices = stockPrices;
	}


	@Override
	public String toString() {
		return "Stock [id=" + id + ", isinCode=" + isinCode + ", companyName=" + companyName + ", nseSymbol="
				+ nseSymbol + ", bseCode=" + bseCode + ", sectorName=" + sectorName + ", stockPrices=" + stockPrices
				+ "]";
	}

}
