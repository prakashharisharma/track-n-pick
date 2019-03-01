package com.example.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import com.example.model.master.Stock;

@Service
public class TechnicalsResearchService {

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

		if (stock.getTechnicals().getPrevSma200() >= stock.getTechnicals().getPrevSma50()) {
			if (stock.getTechnicals().getSma50() > stock.getTechnicals().getSma200()) {
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

		if (stock.getTechnicals().getPrevSma50() >= stock.getTechnicals().getPrevSma200()) {
			if (stock.getTechnicals().getSma50() < stock.getTechnicals().getSma200()) {
				isBearishCrossover = true;
			}
		}

		return isBearishCrossover;
	}

}
