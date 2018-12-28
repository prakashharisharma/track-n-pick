package com.example.ui.model;

public class UIDividend {

	long stockid;

	String symbol;
	
	long quantity;
	
	double perShareAmount;
	
	double totalAmount;

	String exDate;

	String recordDate;

	String transactionDate;

	public UIDividend() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UIDividend(String symbol, long quantity, double perShareAmount, String exDate, String recordDate,
			String transactionDate) {
		super();
		this.symbol = symbol;
		this.quantity = quantity;
		this.perShareAmount = perShareAmount;
		this.exDate = exDate;
		this.recordDate = recordDate;
		this.transactionDate = transactionDate;
		this.totalAmount = perShareAmount * quantity;
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

	public long getQuantity() {
		return quantity;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	public double getPerShareAmount() {
		return perShareAmount;
	}

	public void setPerShareAmount(double perShareAmount) {
		this.perShareAmount = perShareAmount;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getExDate() {
		return exDate;
	}

	public void setExDate(String exDate) {
		this.exDate = exDate;
	}

	public String getRecordDate() {
		return recordDate;
	}

	public void setRecordDate(String recordDate) {
		this.recordDate = recordDate;
	}

	public String getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(String transactionDate) {
		this.transactionDate = transactionDate;
	}

	@Override
	public String toString() {
		return "UIDividend [stockid=" + stockid + ", symbol=" + symbol + ", quantity=" + quantity + ", perShareAmount="
				+ perShareAmount + ", exDate=" + exDate + ", recordDate=" + recordDate + ", transactionDate="
				+ transactionDate + "]";
	}
}
