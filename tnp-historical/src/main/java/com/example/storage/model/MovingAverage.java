package com.example.storage.model;

public class MovingAverage {

	double sma50;
	
	double sma100;
	
	double sma200;

	public double getSma50() {
		return sma50;
	}

	public void setSma50(double sma50) {
		this.sma50 = sma50;
	}

	public double getSma100() {
		return sma100;
	}

	public void setSma100(double sma100) {
		this.sma100 = sma100;
	}

	public double getSma200() {
		return sma200;
	}

	public void setSma200(double sma200) {
		this.sma200 = sma200;
	}

	
	public MovingAverage(double sma50, double sma100, double sma200) {
		super();
		this.sma50 = sma50;
		this.sma100 = sma100;
		this.sma200 = sma200;
	}

	public MovingAverage() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "MovingAverage [sma50=" + sma50 + ", sma100=" + sma100 + ", sma200=" + sma200 + "]";
	}

}
