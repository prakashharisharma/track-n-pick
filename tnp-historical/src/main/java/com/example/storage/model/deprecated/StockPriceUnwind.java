package com.example.storage.model.deprecated;

import java.util.List;

import com.example.storage.model.deprecated.StockPriceD;

@Deprecated
public class StockPriceUnwind {

	private String nseSymbol;
	
    private StockPriceD stockPrices;
    
	public String getNseSymbol() {
		return nseSymbol;
	}
	public void setNseSymbol(String nseSymbol) {
		this.nseSymbol = nseSymbol;
	}

	public StockPriceD getStockPrices() {
		return stockPrices;
	}
	public void setStockPrices(StockPriceD stockPrices) {
		this.stockPrices = stockPrices;
	}
	
	
	public StockPriceUnwind() {
		super();
		// TODO Auto-generated constructor stub
	}
	public StockPriceUnwind(String nseSymbol, StockPriceD stockPrices) {
		super();
		this.nseSymbol = nseSymbol;
		this.stockPrices = stockPrices;
	}
	@Override
	public String toString() {
		return "StockPriceUnwind [nseSymbol=" + nseSymbol + ", stockPrices=" + stockPrices + "]";
	}
    
    
    
}
