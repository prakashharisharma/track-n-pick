package com.example.storage.model;

public class Trend {

	private MovingAverage movingAverage;

	private AverageDirectionalIndex adx;

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

	public AverageDirectionalIndex getAdx() {
		return adx;
	}

	public void setAdx(AverageDirectionalIndex adx) {
		this.adx = adx;
	}

	@Override
	public String toString() {
		return "Trend [movingAverage=" + movingAverage + "]";
	}
	
	
}
