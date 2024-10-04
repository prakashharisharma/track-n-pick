package com.example.storage.model;

public class MovingAverage {

	private SimpleMovingAverage simple;
	
	private ExponentialMovingAverage exponential;
	
	public MovingAverage(SimpleMovingAverage simple, ExponentialMovingAverage exponential) {
		super();
		
		this.simple = simple;
		this.exponential = exponential;
		
	}

	public SimpleMovingAverage getSimple() {
		return simple;
	}

	public void setSimple(SimpleMovingAverage simple) {
		this.simple = simple;
	}

	public ExponentialMovingAverage getExponential() {
		return exponential;
	}

	public void setExponential(ExponentialMovingAverage exponential) {
		this.exponential = exponential;
	}

	@Override
	public String toString() {
		return "MovingAverage [simple=" + simple + ", exponential=" + exponential + "]";
	}

}
