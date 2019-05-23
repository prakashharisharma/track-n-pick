package com.example.storage.model;

public class MovingAverage {

	double sma21;
	
	double sma50;
	
	double sma100;
	
	double sma200;

	double ema20;
	
	double ema50;
	
	double ema100;
	
	double ema200;
	
	public double getSma21() {
		return sma21;
	}

	public void setSma21(double sma21) {
		this.sma21 = sma21;
	}

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

	
	public double getEma20() {
		return ema20;
	}

	public void setEma20(double ema20) {
		this.ema20 = ema20;
	}

	public double getEma50() {
		return ema50;
	}

	public void setEma50(double ema50) {
		this.ema50 = ema50;
	}

	public double getEma100() {
		return ema100;
	}

	public void setEma100(double ema100) {
		this.ema100 = ema100;
	}

	public double getEma200() {
		return ema200;
	}

	public void setEma200(double ema200) {
		this.ema200 = ema200;
	}

	public MovingAverage(double sma21, double sma50, double sma100, double sma200,double ema20, double ema50, double ema100, double ema200) {
		super();
		this.sma21= sma21;
		this.sma50 = sma50;
		this.sma100 = sma100;
		this.sma200 = sma200;
		this.ema20= ema20;
		this.ema50 = ema50;
		this.ema100 = ema100;
		this.ema200 = ema200;
	}

	public MovingAverage() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "MovingAverage [sma21=" + sma21 + ", sma50=" + sma50 + ", sma100=" + sma100 + ", sma200=" + sma200
				+ ", ema20=" + ema20 + ", ema50=" + ema50 + ", ema100=" + ema100 + ", ema200=" + ema200 + "]";
	}



}
