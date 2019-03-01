package com.example.storage.model;

public class Indicator {

	private RSI rsi;

	
	public Indicator() {
		super();
		
	}

	public Indicator(RSI rsi) {
		super();
		this.rsi = rsi;
	}

	public RSI getRsi() {
		return rsi;
	}

	public void setRsi(RSI rsi) {
		this.rsi = rsi;
	}

	@Override
	public String toString() {
		return "Indicator [rsi=" + rsi + "]";
	}
	
	
	
}
