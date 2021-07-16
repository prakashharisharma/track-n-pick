package com.example.storage.model;

public class Trend {

	private MovingAverage movingAverage;

	public Trend(MovingAverage movingAverage) {
		super();
		this.movingAverage = movingAverage;
	}

	public MovingAverage getMovingAverage() {
		return movingAverage;
	}

	public void setMovingAverage(MovingAverage movingAverage) {
		this.movingAverage = movingAverage;
	}

	@Override
	public String toString() {
		return "Trend [movingAverage=" + movingAverage + "]";
	}
	
	
}
