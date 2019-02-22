package com.example.storage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.storage.model.type.Direction;
import com.example.storage.model.Trend;
import com.example.storage.repo.TechnicalsTemplate;

@Service
public class TrendServiceImpl {


	@Autowired
	private StorageService storageService;

	@Autowired
	private TechnicalsTemplate technicalsTemplate;
	
	public Trend getMovingAverageTrends(String nseSymbol) {
		
		double sma50 = storageService.getSMA(nseSymbol, 50);

		double sma100 = storageService.getSMA(nseSymbol, 100);

		double sma200 = storageService.getSMA(nseSymbol, 200);

		double avgSma50 = technicalsTemplate.getPriorDaysSma50Average(nseSymbol, 3);
		
		double currentPrice = storageService.getCurrentPrice(nseSymbol);

		Trend trend = new Trend(this.getLongTermTrend(currentPrice, sma200),
				this.getMidTermTrend(currentPrice, sma50, sma100), this.getCurrentTrend(currentPrice, avgSma50)
				);
	
		return trend;
		
	}

	public Trend getRsiTrends(String nseSymbol) {
		
		double smoothedRsi = technicalsTemplate.getCurrentSmoothedRSI(nseSymbol);
		
		Trend trend = new Trend(
				this.getRsiLongTermTrend(nseSymbol, smoothedRsi),
				this.getRsiMidTermTrend(nseSymbol, smoothedRsi),this.getRsiCurrentTrend(smoothedRsi));
		
		return trend;
	}
	
	private Direction getLongTermTrend(double currentPrice, double sma200) {

		if (currentPrice > sma200) {
			return Direction.UPTREND;
		} else {
			return Direction.DOWNTREND;
		}

	}

	private Direction getMidTermTrend(double currentPrice, double sma50, double sma100) {
		if (currentPrice > sma50) {

			if (sma50 > sma100) {
				return Direction.UPTREND;
			} else {
				return Direction.DOWNTREND;
			}
		} else {
			if (sma50 < sma100) {
				return Direction.DOWNTREND;
			} else {
				return Direction.UPTREND;
			}
		}
	}

	private Direction getCurrentTrend(double currentPrice, double avgSma50) {

		if (currentPrice > avgSma50) {
			return Direction.UPTREND;
		} else {
			return Direction.DOWNTREND;
		}

	}
	


	private Direction getRsiLongTermTrend(String nseSymbol, double currentSmoothedRsi) {
		int below30Count = technicalsTemplate.getrsiCountBelow(30.00, nseSymbol, 100);
		int below70Count = technicalsTemplate.getrsiCountBelow(70.00, nseSymbol, 100);
		int above30Count = technicalsTemplate.getrsiCountAbove(30.00, nseSymbol, 100);
		int above70Count = technicalsTemplate.getrsiCountAbove(70.00, nseSymbol, 100);

		// UP Stay above 30, Often hit 70 - 100
		if (below30Count == 0 && above70Count > 0) {
			return Direction.UPTREND;
		} else if (above70Count == 0 && below30Count > 0) {
			return Direction.DOWNTREND;
		} else if (below30Count > 0) {
			return Direction.UPTREND;
		} else { // DN Stay below 70, Often hit 30 - 100

			return Direction.DOWNTREND;
		}

	}

	private Direction getRsiMidTermTrend(String nseSymbol, double currentSmoothedRsi) {

		int below30Count = technicalsTemplate.getrsiCountBelow(30.00, nseSymbol, 50);
		int below70Count = technicalsTemplate.getrsiCountBelow(70.00, nseSymbol, 50);
		int above30Count = technicalsTemplate.getrsiCountAbove(30.00, nseSymbol, 50);
		int above70Count = technicalsTemplate.getrsiCountAbove(70.00, nseSymbol, 50);

		
		// UP Stay above 30, Often hit 70 - 100
		if (below30Count == 0 && above70Count > 0) {
			return Direction.UPTREND;
		} else if (above70Count == 0 && below30Count > 0) {
			return Direction.DOWNTREND;
		} else if (below30Count > 0) {
			return Direction.UPTREND;
		} else { // DN Stay below 70, Often hit 30 - 100

			return Direction.DOWNTREND;
		}
	}
	private Direction getRsiCurrentTrend(double smoothedRsi) {
		
		if(smoothedRsi > 50.0) {
			return Direction.UPTREND;
		} else { // DN Stay below 70, Often hit 30 - 100

			return Direction.DOWNTREND;
		}
	}
	
}
