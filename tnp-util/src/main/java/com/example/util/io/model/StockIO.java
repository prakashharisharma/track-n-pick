package com.example.util.io.model;

import java.io.Serializable;

public class StockIO implements Serializable {

	public enum IndiceType implements Serializable{
		NIFTY50, NIFTY100, NIFTY250, NIFTY500, NIFTY750, NIFTY1000, NIFTY1500, NSE, BSE_A, BSE_B, BSE_M
	}

	public enum Exchange implements Serializable{
		NSE, BSE
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1344671851364579159L;

	private String companyName;

	private String sector;

	private String nseSymbol;

	private String bseCode;

	private String series;

	private String isin;

	private IndiceType indice;

	private Exchange exchange;
	
	public StockIO(String companyName, String sector, String nseSymbol, String series, String isin, IndiceType indice) {
		super();
		this.companyName = companyName;
		this.sector = sector;
		this.nseSymbol = nseSymbol;
		this.series = series;
		this.isin = isin;
		this.indice = indice;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getSector() {
		return sector;
	}

	public void setSector(String sector) {
		this.sector = sector;
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

	public String getSeries() {
		return series;
	}

	public void setSeries(String series) {
		this.series = series;
	}

	public String getIsin() {
		return isin;
	}

	public void setIsin(String isin) {
		this.isin = isin;
	}

	public IndiceType getIndice() {
		return indice;
	}

	public void setIndice(IndiceType indice) {
		this.indice = indice;
	}

	public Exchange getExchange() {
		return exchange;
	}

	public void setExchange(Exchange exchange) {
		this.exchange = exchange;
	}

	@Override
	public String toString() {
		return "StockIO [companyName=" + companyName + ", sector=" + sector + ", nseSymbol=" + nseSymbol + ", series="
				+ series + ", isin=" + isin + ", indice=" + indice + "]";
	}

}
