package com.example.storage.model;

public class Momentum {

	private RSI rsi;

	
	public Momentum() {
		super();
		
	}

	public Momentum(RSI rsi) {
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
