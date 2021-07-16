package com.example.storage.model.result;

public class StockPriceResult {
	
	private String _id;

	private double resultPrice;

	public StockPriceResult(String _id, double resultPrice) {
		super();
		this._id = _id;
		this.resultPrice = resultPrice;
	}


	public String get_id() {
		return _id;
	}


	public void set_id(String _id) {
		this._id = _id;
	}


	public double getResultPrice() {
		return resultPrice;
	}

	public void setResultPrice(double resultPrice) {
		this.resultPrice = resultPrice;
	}

	@Override
	public String toString() {
		return "StockPriceResult [nseSymbol=" + _id + ", resultPrice=" + resultPrice + "]";
	}
	
	
}
