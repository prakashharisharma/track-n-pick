package com.example.storage.model.result;

public class StockOBVResult {

	private String _id;

	private long resultOBV;

	public StockOBVResult(String _id, long resultOBV) {
		super();
		this._id = _id;
		this.resultOBV = resultOBV;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public long getResultOBV() {
		return resultOBV;
	}

	public void setResultOBV(long resultOBV) {
		this.resultOBV = resultOBV;
	}

	@Override
	public String toString() {
		return "StockOBVResult [_id=" + _id + ", resultOBV=" + resultOBV + "]";
	}
	
	
	
}
