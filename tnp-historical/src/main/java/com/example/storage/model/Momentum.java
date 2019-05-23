package com.example.storage.model;

public class Momentum {

	private RSI rsi;

	private StochasticOscillator stochasticOscillator;
	
	private PriceVolume priceVolume;
	
	public Momentum() {
		super();
		
	}

	public Momentum(RSI rsi,StochasticOscillator stochasticOscillator, PriceVolume priceVolume) {
		super();
		this.rsi = rsi;
		this.stochasticOscillator = stochasticOscillator;
		this.priceVolume = priceVolume;
	}

	public RSI getRsi() {
		return rsi;
	}

	public void setRsi(RSI rsi) {
		this.rsi = rsi;
	}

	public PriceVolume getPriceVolume() {
		return priceVolume;
	}

	public void setPriceVolume(PriceVolume priceVolume) {
		this.priceVolume = priceVolume;
	}

	@Override
	public String toString() {
		return "Momentum [rsi=" + rsi + ", stochasticOscillator=" + stochasticOscillator + ", priceVolume="
				+ priceVolume + "]";
	}


	
}
