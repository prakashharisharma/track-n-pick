package com.example.storage.model;

public class StochasticOscillator {

	private Double k;

	private Double d;

	public StochasticOscillator() {
		super();

	}

	public StochasticOscillator(Double k, Double d) {
		super();
		this.k = k != null ? k : 0.00;
		this.d = d != null ? d : 0.00;
	}

	public Double getK() {
		return k;
	}

	public void setK(Double k) {
		this.k = k;
	}

	public Double getD() {
		return d;
	}

	public void setD(Double d) {
		this.d = d;
	}

	@Override
	public String toString() {
		return "StochasticOscillator [k=" + k + ", d=" + d + "]";
	}

}
