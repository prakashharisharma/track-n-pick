package com.example.storage.model;

public class Momentum {

	private Double macd1;

	private Macd macd;

	private RSI rsi;

	private StochasticOscillator stochasticOscillator;
	
	public Momentum(Macd macd, RSI rsi,StochasticOscillator stochasticOscillator) {
		super();
		this.macd = macd;
		this.rsi = rsi;
		this.stochasticOscillator = stochasticOscillator;
	}

	public Macd getMacd() {
		return macd;
	}

	public void setMacd(Macd macd) {
		this.macd = macd;
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
