package com.example.storage.model;

public class StochasticOscillator {

	private double k;
	
	private double d;

	public StochasticOscillator() {
		super();
		
	}

	public StochasticOscillator(double k, double d) {
		super();
		this.k = k;
		this.d = d;
	}

	public double getK() {
		return k;
	}

	public void setK(double k) {
		this.k = k;
	}

	public double getD() {
		return d;
	}

	public void setD(double d) {
		this.d = d;
	}

	@Override
	public String toString() {
		return "StochasticOscillator [k=" + k + ", d=" + d + "]";
	}

	
	
	
}
