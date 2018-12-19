package com.example.model;

import java.io.Serializable;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

@CsvRecord(separator = ",", skipFirstLine = true)
public class StockMaster implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8695186732771189316L;

	@DataField(pos = 1)
    private String companyName;
	
	@DataField(pos = 2)
    private String sector;
	
	@DataField(pos = 3)
    private String nseSymbol;
	
	@DataField(pos = 4)
    private String series;
	
	@DataField(pos = 5)
    private String isin;

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

	@Override
	public String toString() {
		return "StockMaster [companyName=" + companyName + ", sector=" + sector + ", nseSymbol=" + nseSymbol
				+ ", series=" + series + ", isin=" + isin + "]";
	}
	
}
