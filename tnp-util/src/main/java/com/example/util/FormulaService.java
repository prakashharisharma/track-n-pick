package com.example.util;

import org.springframework.stereotype.Service;

@Service
public class FormulaService {

	public double calculateRs(double averageGain, double averageLoss) {

		double rs = averageGain / averageLoss;

		return rs;
	}

	public double calculateRsi(double rs) {

		double rsi = 0.00;
		try {
			rsi = (100 - (100 / (1 + rs)));
		}catch(Exception e) {
			rsi = 1.00;
		}
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

	public double calculateStochasticOscillatorValue(double currentPrice, double low14, double high14) {
		double stochasticOscillatorValue = 0.00;
		
		if((high14 - low14 != 0)  ) {
			stochasticOscillatorValue = ((currentPrice - low14) / (high14 - low14)) * 100;
		}else {
			stochasticOscillatorValue = 0.00;
		}
		return stochasticOscillatorValue;
	}
	
	public double calculateRateOfChange(double current, double previous) {
		double rateOfChange = 0.00;
		if(previous !=0) {
		rateOfChange = ((current - previous) / previous) * 100;
		}else {
			rateOfChange = current;
		}
		return rateOfChange;
		
	}
	
	public double calculateRateOfChange(long current, long previous) {
		double rateOfChange = 0.00;
		
		if(previous !=0) {
		
			rateOfChange = (((double) current - (double)previous) / (double)previous) * 100;
		}else {
			rateOfChange = current;
		}
		return rateOfChange;
		
	}
	
	public double calculatePe(double currentPrice, double eps) {

		double pe = 0.0;

		if(eps != 0.00) {
			pe = currentPrice / eps;
		}

		return pe;
	}

	public double calculatePb(double currentPrice, double bookValue) {

		double pb = 0.0;

		if(bookValue != 0.00) {
			pb = currentPrice / bookValue;
		}
		
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
	
	public long calculateOBV(long prevOBV, double prevClose, double currentClose, long currentVolume) {
		long OBV = 0;
		
		if(currentClose > prevClose) {
			OBV = prevOBV + currentVolume;
		}else if(currentClose < prevClose) {
			OBV = prevOBV - currentVolume;
		}else {
			OBV = prevOBV;
		}
		
		return OBV;
	}
	
	public double calculateAverage(double num1, double num2) {
		
		return (num1 + num2) / 2;
	}
	
	public double getEMAMultiplier(int timePeriod) {
		
		double multiplier = (2.0 / ((double)timePeriod + 1.0) );

		return multiplier;
	}

	public double calculateEMA(double close, double prevEMA, int timePeriod) {
		double ema;
		
		double K = this.getEMAMultiplier(timePeriod);
		
		//System.out.println("K" + K);
		
		ema = (close  * K) + (prevEMA * (1 - K));
		 // (22.15 * 0.1818) + (22.22 * ( 1 - 0.1818)) = 4.02687 + (22.22 * (0.8182)) = 18.180404 = 22.207274
		return ema;
	}
	
}
