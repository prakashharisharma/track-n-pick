package com.example.storage.model.result;

public class HighLowResult {

	private String _id;

	private double high;
	
	private double low;

	public HighLowResult(String _id, double high, double low) {
		super();
		this._id = _id;
		this.high = high;
		this.low = low;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	@Override
	public String toString() {
		return "HighLowResult [_id=" + _id + ", high=" + high + ", low=" + low + "]";
	}

	
	
}
