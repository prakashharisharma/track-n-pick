package com.example.storage.model;

public class MovingAverage {

	private Simple simple;
	
	private Exponential exponential;
	
	public MovingAverage(Simple simple, Exponential exponential) {
		super();
		
		this.simple = simple;
		this.exponential = exponential;
		
	}

	public Simple getSimple() {
		return simple;
	}

	public void setSimple(Simple simple) {
		this.simple = simple;
	}

	public Exponential getExponential() {
		return exponential;
	}

	public void setExponential(Exponential exponential) {
		this.exponential = exponential;
	}

	@Override
	public String toString() {
		return "MovingAverage [simple=" + simple + ", exponential=" + exponential + "]";
	}

}
