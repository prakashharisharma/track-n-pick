package com.example.storage.model;

public class Momentum {

	private RSI rsi;

	private StochasticOscillator stochasticOscillator;
	
	public Momentum(RSI rsi,StochasticOscillator stochasticOscillator) {
		super();
		this.rsi = rsi;
		this.stochasticOscillator = stochasticOscillator;
	}

	public RSI getRsi() {
		return rsi;
	}

	public void setRsi(RSI rsi) {
		this.rsi = rsi;
	}

	public StochasticOscillator getStochasticOscillator() {
		return stochasticOscillator;
	}

	public void setStochasticOscillator(StochasticOscillator stochasticOscillator) {
		this.stochasticOscillator = stochasticOscillator;
	}

	@Override
	public String toString() {
		return "Momentum [rsi=" + rsi + ", stochasticOscillator=" + stochasticOscillator + "]";
	}
	
}
