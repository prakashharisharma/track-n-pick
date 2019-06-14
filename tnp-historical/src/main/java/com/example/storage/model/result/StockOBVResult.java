package com.example.storage.model.result;

public class StockOBVResult {

	private String _id;

	private Long resultOBV;

	public StockOBVResult(String _id, Long resultOBV) {
		super();
		this._id = _id;
		if(resultOBV != null) {
		this.resultOBV = resultOBV;
		}else {
			this.resultOBV = 0l;
		}
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public Long getResultOBV() {
		return resultOBV;
	}

	public void setResultOBV(Long resultOBV) {
		this.resultOBV = resultOBV;
	}

	@Override
	public String toString() {
		return "StockOBVResult [_id=" + _id + ", resultOBV=" + resultOBV + "]";
	}
	
	
	
}
