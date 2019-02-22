package com.example.storage.model;

public class RSI {

	double rs;
	
	double rsi;
	
	double smoothedRs;
	
	double smoothedRsi;

	public RSI() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RSI(double rs, double rsi, double smoothedRs, double smoothedRsi) {
		super();
		this.rs = rs;
		this.rsi = rsi;
		this.smoothedRs = smoothedRs;
		this.smoothedRsi = smoothedRsi;
	}

	public double getRs() {
		return rs;
	}

	public void setRs(double rs) {
		this.rs = rs;
	}

	public double getRsi() {
		return rsi;
	}

	public void setRsi(double rsi) {
		this.rsi = rsi;
	}

	public double getSmoothedRs() {
		return smoothedRs;
	}

	public void setSmoothedRs(double smoothedRs) {
		this.smoothedRs = smoothedRs;
	}

	public double getSmoothedRsi() {
		return smoothedRsi;
	}

	public void setSmoothedRsi(double smoothedRsi) {
		this.smoothedRsi = smoothedRsi;
	}

	@Override
	public String toString() {
		return "RSI [rs=" + rs + ", rsi=" + rsi + ", smoothedRs=" + smoothedRs + ", smoothedRsi=" + smoothedRsi + "]";
	}
	
	
	
}
