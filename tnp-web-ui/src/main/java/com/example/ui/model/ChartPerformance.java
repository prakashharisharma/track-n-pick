package com.example.ui.model;

public class ChartPerformance {

	String date;
	
	double value;

	public ChartPerformance() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ChartPerformance(String date, double value) {
		super();
		this.date = date;
		this.value = value;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
	
	
}
