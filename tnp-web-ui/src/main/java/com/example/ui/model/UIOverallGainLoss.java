package com.example.ui.model;

public class UIOverallGainLoss {

	double ytdInvestmentValue;
	
	double currentValue;
	
	double ytdRealizedGainPer;
	
	double ytdUnrealizedGainPer;

	double fyInvestmentValue;
	
	double fyRealizedGainPer;
	
	double fyUnrealizedGainPer;
	
	
	public UIOverallGainLoss() {
		super();
		// TODO Auto-generated constructor stub
	}


	public double getYtdInvestmentValue() {
		return ytdInvestmentValue;
	}


	public void setYtdInvestmentValue(double ytdInvestmentValue) {
		this.ytdInvestmentValue = ytdInvestmentValue;
	}


	public double getCurrentValue() {
		return currentValue;
	}


	public void setCurrentValue(double currentValue) {
		this.currentValue = currentValue;
	}


	public double getYtdRealizedGainPer() {
		return ytdRealizedGainPer;
	}


	public void setYtdRealizedGainPer(double ytdRealizedGainPer) {
		this.ytdRealizedGainPer = ytdRealizedGainPer;
	}


	public double getYtdUnrealizedGainPer() {
		return ytdUnrealizedGainPer;
	}


	public void setYtdUnrealizedGainPer(double ytdUnrealizedGainPer) {
		this.ytdUnrealizedGainPer = ytdUnrealizedGainPer;
	}


	public double getFyInvestmentValue() {
		return fyInvestmentValue;
	}


	public void setFyInvestmentValue(double fyInvestmentValue) {
		this.fyInvestmentValue = fyInvestmentValue;
	}


	public double getFyRealizedGainPer() {
		return fyRealizedGainPer;
	}


	public void setFyRealizedGainPer(double fyRealizedGainPer) {
		this.fyRealizedGainPer = fyRealizedGainPer;
	}


	public double getFyUnrealizedGainPer() {
		return fyUnrealizedGainPer;
	}


	public void setFyUnrealizedGainPer(double fyUnrealizedGainPer) {
		this.fyUnrealizedGainPer = fyUnrealizedGainPer;
	}


	@Override
	public String toString() {
		return "UIOverallGainLoss [ytdInvestmentValue=" + ytdInvestmentValue + ", currentValue=" + currentValue
				+ ", ytdRealizedGainPer=" + ytdRealizedGainPer + ", ytdUnrealizedGainPer=" + ytdUnrealizedGainPer
				+ ", fyInvestmentValue=" + fyInvestmentValue + ", fyRealizedGainPer=" + fyRealizedGainPer
				+ ", fyUnrealizedGainPer=" + fyUnrealizedGainPer + "]";
	}


	
}
