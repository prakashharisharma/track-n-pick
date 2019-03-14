package com.example.util;

import org.springframework.stereotype.Service;

@Service
public class FormulaService {

	public double calculateRs(double averageGain, double averageLoss) {

		double rs = averageGain / averageLoss;

		return rs;
	}

	public double calculateRsi(double rs) {

		double rsi = (100 - (100 / (1 + rs)));

		return rsi;
	}

	public double calculateSmoothedRsi(double smoothedRs) {
		double smoothedRsi = this.calculateRsi(smoothedRs);

		return smoothedRsi;
	}

	public double calculateSmoothedRs(double prevAverageGain, double prevAverageLoss, double currentGain,
			double currentLoss) {

		double smoothedRs = (((prevAverageGain * 13) + currentGain) / 14)
				/ (((prevAverageLoss * 13) + currentLoss) / 14);

		return smoothedRs;
	}

	public double calculatePe(double currentPrice, double eps) {

		double pe = 0.0;

		pe = currentPrice / eps;
		;

		return pe;
	}

	public double calculatePb(double currentPrice, double bookValue) {

		double pb = 0.0;

		pb = currentPrice / bookValue;

		return pb;
	}
	
	public double calculatePercentRate(double baseNumber, double percentage) {

		double rate = (percentage / baseNumber) * 100;

		return rate;
	}
	
	public double calculatePercentage(double baseNumber, double rate) {
		
		double percentage = (rate / 100) * baseNumber;
		
		return percentage;
	}
}
