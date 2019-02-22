package com.example.storage.model.result;

public class YearHighLowResult {
	private String _id;
	
	private String nseSymbol;

	private double resultPrice;

	public YearHighLowResult() {
		super();
		// TODO Auto-generated constructor stub
	}

	public YearHighLowResult(String _id, String nseSymbol, double resultPrice) {
		super();
		this._id = _id;
		this.nseSymbol = nseSymbol;
		this.resultPrice = resultPrice;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getNseSymbol() {
		return nseSymbol;
	}

	public void setNseSymbol(String nseSymbol) {
		this.nseSymbol = nseSymbol;
	}

	public double getResultPrice() {
		return resultPrice;
	}

	public void setResultPrice(double resultPrice) {
		this.resultPrice = resultPrice;
	}

	@Override
	public String toString() {
		return "YearHighLowResult [_id=" + _id + ", nseSymbol=" + nseSymbol + ", resultPrice=" + resultPrice + "]";
	}
	
	
	
}
