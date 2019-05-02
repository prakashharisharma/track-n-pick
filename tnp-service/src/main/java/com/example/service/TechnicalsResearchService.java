package com.example.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.model.master.Stock;
import com.example.util.rules.RulesResearch;

@Service
public class TechnicalsResearchService {

	@Autowired
	private RulesResearch rulesResearch;
	
	/**
	 * A bullish crossover occurs when the shorter moving average crosses above the
	 * longer moving average. This is also known as a golden cross.
	 * 
	 * @param nifty200stocksList
	 * @return
	 */
	public List<Stock> bullishCrossOver(List<Stock> stocksList) {

		List<Stock> bullishCrossOverList = new ArrayList<>();

		for (Stock stock : stocksList) {
			if (this.isBullishCrossOver(stock)) {

				bullishCrossOverList.add(stock);

			}
		}

		return bullishCrossOverList;
	}

	/**
	 * A bullish crossover occurs when the shorter moving average crosses above the
	 * longer moving average. This is also known as a golden cross.
	 * 
	 * @param nifty200stocksList
	 * @return
	 */
	public boolean isBullishCrossOver(Stock stock) {

		boolean isBullishCrossOver = false;

/*		if (stock.getTechnicals().getPrevSma200() >= stock.getTechnicals().getPrevSma50()) {
			if (stock.getTechnicals().getSma50() > stock.getTechnicals().getSma200()) {
				isBullishCrossOver = true;
			}
		}
*/
		if(rulesResearch.getCrossOverDays() > 100) {
			isBullishCrossOver = isBullishCrossOver(stock.getTechnicals().getPrevSma50(), stock.getTechnicals().getPrevSma200(), stock.getTechnicals().getSma50(), stock.getTechnicals().getSma200());
		}else {
			isBullishCrossOver = isBullishCrossOver(stock.getTechnicals().getPrevSma50(), stock.getTechnicals().getPrevSma100(), stock.getTechnicals().getSma50(), stock.getTechnicals().getSma100());
		}
		
		return isBullishCrossOver;
	}

	public boolean isBullishCrossOver(double prevShortTermAvg, double prevLongTermAvg, double shortTermAvg, double longTermAvg) {

		boolean isBullishCrossOver = false;

		if (prevLongTermAvg >= prevShortTermAvg) {
			if (shortTermAvg > longTermAvg) {
				isBullishCrossOver = true;
			}
		}

		return isBullishCrossOver;
	}
	
	
	/**
	 * A bearish crossover occurs when the shorter moving average crosses below the
	 * longer moving average. This is known as a dead cross.
	 * 
	 * @param nifty200stocksList
	 * @return
	 */
	public List<Stock> bearishCrossover(List<Stock> stocksList) {
		List<Stock> bearishCrossoverList = new ArrayList<>();
		for (Stock stock : stocksList) {
			if (this.isBearishCrossover(stock)) {

				bearishCrossoverList.add(stock);

			}
		}
		return bearishCrossoverList;
	}

	/**
	 * A bearish crossover occurs when the shorter moving average crosses below the
	 * longer moving average. This is known as a dead cross.
	 * 
	 * @param nifty200stocksList
	 * @return
	 */
	public boolean isBearishCrossover(Stock stock) {
		boolean isBearishCrossover = false;

	/*	if (stock.getTechnicals().getPrevSma50() >= stock.getTechnicals().getPrevSma200()) {
			if (stock.getTechnicals().getSma50() < stock.getTechnicals().getSma200()) {
				isBearishCrossover = true;
			}
		}*/

		if(rulesResearch.getCrossOverDays() > 100) {
			isBearishCrossover = isBearishCrossover(stock.getTechnicals().getPrevSma50(), stock.getTechnicals().getPrevSma200(), stock.getTechnicals().getSma50(), stock.getTechnicals().getSma200());
		}else {
			isBearishCrossover = isBearishCrossover(stock.getTechnicals().getPrevSma50(), stock.getTechnicals().getPrevSma100(), stock.getTechnicals().getSma50(), stock.getTechnicals().getSma100());
		}
		
		return isBearishCrossover;
	}

	public boolean isBearishCrossover(double prevShortTermAvg, double prevLongTermAvg, double shortTermAvg, double longTermAvg) {
		boolean isBearishCrossover = false;

		if (prevShortTermAvg >= prevLongTermAvg) {
			if (shortTermAvg < longTermAvg) {
				isBearishCrossover = true;
			}
		}

		return isBearishCrossover;
	}
	
}
