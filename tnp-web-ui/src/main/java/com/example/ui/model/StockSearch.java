package com.example.ui.model;

public class StockSearch {

	long id;

	String companyNameAndSymbol;

	public StockSearch() {
		super();
	}

	public StockSearch(long id, String companyNameAndSymbol) {
		super();
		this.id = id;
		this.companyNameAndSymbol = companyNameAndSymbol;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCompanyNameAndSymbol() {
		return companyNameAndSymbol;
	}

	public void setCompanyNameAndSymbol(String companyNameAndSymbol) {
		this.companyNameAndSymbol = companyNameAndSymbol;
	}

	@Override
	public String toString() {
		return "StockSearch [id=" + id + ", companyNameAndSymbol=" + companyNameAndSymbol + "]";
	}

	
}
