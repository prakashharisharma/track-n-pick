package com.example.ui.model;

public class UIOverallGainLoss {

	double ytdInvestmentValue;
	
	double currentValue;

	double fyXirr;

	double fyCagr;
	
	double ytdRealizedGainPer;
	
	double ytdUnrealizedGainPer;

	double fyInvestmentValue;
	
	double fyRealizedGainPer;
	
	double fyUnrealizedGainPer;
	
	
	double fyNetGain;
	
	double fyNetDividends;
	
	double fyNetTaxPaid;
	
	double fyNetExpense;
	
	double fyNetTaxLiability;
	
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


	public double getFyNetGain() {
		return fyNetGain;
	}


	public void setFyNetGain(double fyNetGain) {
		this.fyNetGain = fyNetGain;
	}


	public double getFyNetDividends() {
		return fyNetDividends;
	}


	public void setFyNetDividends(double fyNetDividends) {
		this.fyNetDividends = fyNetDividends;
	}


	public double getFyNetTaxPaid() {
		return fyNetTaxPaid;
	}


	public void setFyNetTaxPaid(double fyNetTaxPaid) {
		this.fyNetTaxPaid = fyNetTaxPaid;
	}


	public double getFyNetExpense() {
		return fyNetExpense;
	}


	public void setFyNetExpense(double fyNetExpense) {
		this.fyNetExpense = fyNetExpense;
	}


	public double getFyNetTaxLiability() {
		return fyNetTaxLiability;
	}


	public void setFyNetTaxLiability(double fyNetTaxLiability) {
		this.fyNetTaxLiability = fyNetTaxLiability;
	}

	public double getFyXirr() {
		return fyXirr;
	}

	public void setFyXirr(double fyXirr) {
		this.fyXirr = fyXirr;
	}

	public double getFyCagr() {
		return fyCagr;
	}

	public void setFyCagr(double fyCagr) {
		this.fyCagr = fyCagr;
	}

	@Override
	public String toString() {
		return "UIOverallGainLoss [ytdInvestmentValue=" + ytdInvestmentValue + ", currentValue=" + currentValue
				+ ", ytdRealizedGainPer=" + ytdRealizedGainPer + ", ytdUnrealizedGainPer=" + ytdUnrealizedGainPer
				+ ", fyInvestmentValue=" + fyInvestmentValue + ", fyRealizedGainPer=" + fyRealizedGainPer
				+ ", fyUnrealizedGainPer=" + fyUnrealizedGainPer + "]";
	}


	
}
