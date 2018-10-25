package com.example.dylh.model;

import java.util.List;

import com.example.model.master.Stock;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NseResponseData {

	@JsonProperty("data")
	List<Stock> stockList;
	
	public NseResponseData() {
		super();
	}

	public NseResponseData(List<Stock> stockList) {
		super();
		this.stockList = stockList;
	}

	public List<Stock> getStockList() {
		return stockList;
	}

	public void setStockList(List<Stock> stockList) {
		this.stockList = stockList;
	}

	@Override
	public String toString() {
		return "Data [stockList=" + stockList + "]";
	}

	
}
