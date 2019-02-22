package com.example.storage.model;

public class RsiCountResult {

	
	private String _id;

	private int count;

	public RsiCountResult() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RsiCountResult(String _id, int count) {
		super();
		this._id = _id;
		this.count = count;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "RsiCountResult [_id=" + _id + ", count=" + count + "]";
	}
	
	
}
