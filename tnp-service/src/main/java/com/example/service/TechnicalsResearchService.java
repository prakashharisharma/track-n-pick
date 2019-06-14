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
			if (this.isBullishCrossOver100(stock)) {

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
	public boolean isBullishCrossOver50(Stock stock) {

		boolean isBullishCrossOver = false;

		isBullishCrossOver = isShortCrossedLongFromLow(stock.getTechnicals().getPrevSma21(),
				stock.getTechnicals().getPrevSma50(), stock.getTechnicals().getSma21(),
				stock.getTechnicals().getSma50());

		return isBullishCrossOver;
	}

	public boolean isBullishCrossOver100(Stock stock) {

		boolean isBullishCrossOver = false;

		isBullishCrossOver = isShortCrossedLongFromLow(stock.getTechnicals().getPrevSma50(),
				stock.getTechnicals().getPrevSma100(), stock.getTechnicals().getSma50(),
				stock.getTechnicals().getSma100());

		return isBullishCrossOver;
	}

	public boolean isBullishCrossOver200(Stock stock) {

		boolean isBullishCrossOver = false;

		isBullishCrossOver = isShortCrossedLongFromLow(stock.getTechnicals().getPrevSma50(),
				stock.getTechnicals().getPrevSma200(), stock.getTechnicals().getSma50(),
				stock.getTechnicals().getSma200());

		return isBullishCrossOver;
	}

	public boolean isPositiveBreakout50(Stock stock) {

		boolean isPositiveBreakout = false;

		isPositiveBreakout = isShortCrossedLongFromLow(stock.getStockPrice().getPrevClose(),
				stock.getTechnicals().getPrevSma50(), stock.getStockPrice().getCurrentPrice(),
				stock.getTechnicals().getSma50());

		return isPositiveBreakout;
	}

	public boolean isPositiveBreakout100(Stock stock) {

		boolean isPositiveBreakout = false;

		isPositiveBreakout = isShortCrossedLongFromLow(stock.getStockPrice().getPrevClose(),
				stock.getTechnicals().getPrevSma100(), stock.getStockPrice().getCurrentPrice(),
				stock.getTechnicals().getSma100());

		return isPositiveBreakout;
	}

	public boolean isPositiveBreakout200(Stock stock) {

		boolean isPositiveBreakout = false;

		isPositiveBreakout = isShortCrossedLongFromLow(stock.getStockPrice().getPrevClose(),
				stock.getTechnicals().getPrevSma200(), stock.getStockPrice().getCurrentPrice(),
				stock.getTechnicals().getSma200());

		return isPositiveBreakout;
	}

	public boolean isShortCrossedLongFromLow(double prevShortTermAvg, double prevLongTermAvg, double shortTermAvg,
			double longTermAvg) {

		boolean isBullishCrossOver = false;

		if (prevLongTermAvg >= prevShortTermAvg) {
			if (shortTermAvg > longTermAvg) {
				isBullishCrossOver = true;
			}
		}

		return isBullishCrossOver;
	}

	public boolean isLongCrossedShortFromHigh(double prevShortTermAvg, double prevLongTermAvg, double shortTermAvg,
			double longTermAvg) {
		boolean isBearishCrossover = false;

		if (prevShortTermAvg >= prevLongTermAvg) {
			if (shortTermAvg < longTermAvg) {
				isBearishCrossover = true;
			}
		}

		return isBearishCrossover;
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
			if (this.isBearishCrossover100(stock)) {

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
	public boolean isBearishCrossover50(Stock stock) {
		boolean isBearishCrossover = false;

		isBearishCrossover = isLongCrossedShortFromHigh(stock.getTechnicals().getPrevSma21(),
				stock.getTechnicals().getPrevSma50(), stock.getTechnicals().getSma21(),
				stock.getTechnicals().getSma50());

		return isBearishCrossover;
	}

	public boolean isBearishCrossover100(Stock stock) {
		boolean isBearishCrossover = false;

		isBearishCrossover = isLongCrossedShortFromHigh(stock.getTechnicals().getPrevSma50(),
				stock.getTechnicals().getPrevSma100(), stock.getTechnicals().getSma50(),
				stock.getTechnicals().getSma100());

		return isBearishCrossover;
	}

	public boolean isBearishCrossover200(Stock stock) {
		boolean isBearishCrossover = false;

		isBearishCrossover = isLongCrossedShortFromHigh(stock.getTechnicals().getPrevSma50(),
				stock.getTechnicals().getPrevSma200(), stock.getTechnicals().getSma50(),
				stock.getTechnicals().getSma200());

		return isBearishCrossover;
	}

	public boolean isNegativeBreakout50(Stock stock) {
		boolean isNegativeBreakout = false;

		isNegativeBreakout = isLongCrossedShortFromHigh(stock.getStockPrice().getPrevClose(),
				stock.getTechnicals().getPrevSma50(), stock.getStockPrice().getCurrentPrice(),
				stock.getTechnicals().getSma50());

		return isNegativeBreakout;
	}

	public boolean isNegativeBreakout100(Stock stock) {
		boolean isNegativeBreakout = false;

		isNegativeBreakout = isLongCrossedShortFromHigh(stock.getStockPrice().getPrevClose(),
				stock.getTechnicals().getPrevSma100(), stock.getStockPrice().getCurrentPrice(),
				stock.getTechnicals().getSma100());

		return isNegativeBreakout;
	}

	public boolean isNegativeBreakout200(Stock stock) {
		boolean isNegativeBreakout = false;

		isNegativeBreakout = isLongCrossedShortFromHigh(stock.getStockPrice().getPrevClose(),
				stock.getTechnicals().getPrevSma200(), stock.getStockPrice().getCurrentPrice(),
				stock.getTechnicals().getSma200());

		return isNegativeBreakout;
	}

	public boolean isPriceVolumeBullish(Stock stock) {
		boolean isPriceVolumeBullish = false;

		long volume = stock.getTechnicals().getVolume();
		long avgVolume = stock.getTechnicals().getAvgVolume();
		double price = stock.getStockPrice().getCurrentPrice();
		double prevClose = stock.getStockPrice().getPrevClose();

		if (volume > (avgVolume * 2)) {
			if (volume > 499) {
				if (price > prevClose) {
					isPriceVolumeBullish = true;
				}
			}
		}

		return isPriceVolumeBullish;
	}

	public boolean isPriceVolumeBearish(Stock stock) {
		boolean isPriceVolumeBearish = false;

		long volume = stock.getTechnicals().getVolume();
		long avgVolume = stock.getTechnicals().getAvgVolume();
		double price = stock.getStockPrice().getCurrentPrice();
		double prevClose = stock.getStockPrice().getPrevClose();

		if (volume > (avgVolume * 2)) {
			if (volume > 499) {
				if (price < prevClose) {
					isPriceVolumeBearish = true;
				}
			}
		}

		return isPriceVolumeBearish;
	}
}
