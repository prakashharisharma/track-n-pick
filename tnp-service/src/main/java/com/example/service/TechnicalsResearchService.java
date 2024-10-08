package com.example.service;

import java.util.ArrayList;
import java.util.List;

import com.example.model.stocks.StockPrice;
import com.example.model.stocks.StockTechnicals;
import com.example.util.rules.RulesFundamental;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.BreakoutLedger.BreakoutCategory;
import com.example.model.ledger.BreakoutLedger.BreakoutType;
import com.example.model.master.Stock;
import com.example.util.io.model.StockIO.IndiceType;

@Service
public class TechnicalsResearchService {

	public enum RsiTrend {OVERBAUGHT, OVERSOLD, NUETRAL}

	@Autowired
	private BreakoutLedgerService breakoutLedgerService;

	@Autowired
	private RulesFundamental rulesFundamental;

	/**
	 * A bullish crossover occurs when the shorter moving average crosses above the
	 * longer moving average. This is also known as a golden cross.
	 * 
	 * @param stocksList
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
	 * @param stock
	 * @return
	 */
	public boolean isBullishCrossOver50(Stock stock) {

		boolean isBullishCrossOver = false;

		isBullishCrossOver = isShortCrossedLongFromLow(stock.getTechnicals().getPrevEma20(),
				stock.getTechnicals().getPrevEma50(), stock.getTechnicals().getEma20(),
				stock.getTechnicals().getEma50());

		return isBullishCrossOver;
	}

	/**
	 * A bullish crossover occurs when the shorter moving average crosses above the
	 * longer moving average. This is also known as a golden cross.
	 *
	 * @param stock
	 * @return
	 */
	public boolean isBullishCrossOver20(Stock stock) {

		boolean isBullishCrossOver = false;

		isBullishCrossOver = isShortCrossedLongFromLow(stock.getTechnicals().getPrevEma5(),
				stock.getTechnicals().getPrevEma20(), stock.getTechnicals().getEma5(),
				stock.getTechnicals().getEma20());

		return isBullishCrossOver;
	}

	/**
	 * when the price crosses above a moving average
	 * and RSI moves out of oversold territory (above 30),
	 * it may signal a potential long entry.
	 * @return
	 */
	public boolean isBullishWithRsiAndMovingAverage(Stock stock){

		return Boolean.FALSE;
	}

	/**
	 *
	 * Conversely, when the price crosses below the moving average
	 * and RSI moves into overbought territory (above 70),
	 * it could indicate a short entry point.
	 * @return
	 */
	public boolean isBearishWithRsiAndMovingAverage(Stock stock){

		return Boolean.FALSE;
	}

	public RsiTrend currentTrend(Stock stock){

		if(this.isOverBaught(stock)){
			return RsiTrend.OVERBAUGHT;
		}

		if(this.isOverSold(stock)){
			return RsiTrend.OVERSOLD;
		}

		return RsiTrend.NUETRAL;
	}


	/**
	 * Relatively short-term moving average crossovers,
	 * such as the 5 EMA crossing over the 10 EMA,
	 * are best suited to complement RSI.
	 * The 5 EMA crossing from above to below the 10 EMA
	 * confirms the RSI's indication of overbought conditions
	 * and possible trend reversal.
	 * @param stock
	 * @return
	 */
	public boolean isOverBaught(Stock stock){

		if(this.isLongCrossedShortFromHigh(stock.getTechnicals().getPrevEma5(),
				stock.getTechnicals().getPrevEma10(), stock.getTechnicals().getEma5(),
				stock.getTechnicals().getEma10())
				&&
				stock.getTechnicals().getRsi() > 70.0
		){
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}



	/**
	 * Relatively short-term moving average crossovers,
	 * such as the 5 EMA crossing over the 10 EMA,
	 * are best suited to complement RSI.
	 * The 5 EMA crossing from below to above the 10 EMA
	 * confirms the RSI's indication of oversold conditions
	 * and possible trend reversal.
	 * @param stock
	 * @return
	 */
	public boolean isOverSold(Stock stock){

		if(this.isShortCrossedLongFromLow(stock.getTechnicals().getPrevEma5(),
				stock.getTechnicals().getPrevEma10(), stock.getTechnicals().getEma5(),
				stock.getTechnicals().getEma10())
		&&
				stock.getTechnicals().getRsi() < 30.0
		){
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}
	@Deprecated
	public boolean isBullishCrossOver100(Stock stock) {

		boolean isBullishCrossOver = false;

		isBullishCrossOver = isShortCrossedLongFromLow(stock.getTechnicals().getPrevSma50(),
				stock.getTechnicals().getPrevSma100(), stock.getTechnicals().getSma50(),
				stock.getTechnicals().getSma100());

		return isBullishCrossOver;
	}

	public boolean isBullishRule1(Stock stock){

		boolean isBullish = Boolean.FALSE;

		if(stock.getStockFactor()!=null && stock.getStockFactor().getMarketCap() > rulesFundamental.getMcap()){
			StockPrice stockPrice = stock.getStockPrice();
			StockTechnicals stockTechnicals = stock.getTechnicals();
			if(stockPrice!=null && stockTechnicals!=null){
				if( (stockTechnicals.getEma20() > stockTechnicals.getEma50())
				&&
						(stockTechnicals.getEma50() > stockTechnicals.getEma200())
				){
					if((stockTechnicals.getAdx() > stockTechnicals.getPrevAdx())
					&&
							(stockTechnicals.getPlusDi() > stockTechnicals.getMinusDi())
					&&
							(stockTechnicals.getRsi() > stockTechnicals.getPrevRsi())
					&&
							(stockTechnicals.getMacd() > stockTechnicals.getPrevMacd())
					&&
							(stockTechnicals.getMacd() > 0.00)
					&&
							(stockTechnicals.getMacd() > stockTechnicals.getSignal())){

						if((stockTechnicals.getEma5() > stockTechnicals.getPrevEma5())
								&&
								(stockPrice.getClose() > stockPrice.getOpen())

								&&
								(stockPrice.getCurrentPrice() > stockTechnicals.getEma5())
						){
							isBullish = Boolean.TRUE;
						}

					}
				}

			}
		}

		return isBullish;
	}

	public boolean isBullishRule2(Stock stock){

		boolean isBullish = Boolean.FALSE;

		if(stock.getStockFactor()!=null && stock.getStockFactor().getMarketCap() > rulesFundamental.getMcap()){
			StockPrice stockPrice = stock.getStockPrice();
			StockTechnicals stockTechnicals = stock.getTechnicals();
			if(stockPrice!=null && stockTechnicals!=null){
				if( (stockTechnicals.getEma20() > stockTechnicals.getEma50())
						&&
						(stockTechnicals.getEma50() > stockTechnicals.getEma200())
				){
					if((stockTechnicals.getAdx() > stockTechnicals.getPrevAdx())
							&&
							(stockTechnicals.getAdx() > 25.0 && stockTechnicals.getAdx() < 30.0)
							&&
							(stockTechnicals.getPlusDi() > stockTechnicals.getMinusDi())
							&&
							(stockTechnicals.getRsi() > stockTechnicals.getPrevRsi())
							&&
							(stockTechnicals.getRsi() > 60.0)
							&&
							(stockTechnicals.getMacd() > stockTechnicals.getPrevMacd())
							&&
							(stockTechnicals.getMacd() > 0.00)
							&&
							(stockTechnicals.getMacd() > stockTechnicals.getSignal())){

						if(

								(stockPrice.getCurrentPrice() > stockTechnicals.getEma20())
						){
							isBullish = Boolean.TRUE;
						}

					}
				}

			}
		}

		return isBullish;
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
	 * @param stocksList
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
	 * @param stock
	 * @return
	 */
	public boolean isBearishCrossover50(Stock stock) {
		boolean isBearishCrossover = false;

		isBearishCrossover = isLongCrossedShortFromHigh(stock.getTechnicals().getPrevEma20(),
				stock.getTechnicals().getPrevEma50(), stock.getTechnicals().getEma20(),
				stock.getTechnicals().getEma50());

		return isBearishCrossover;
	}

	/**
	 * A bearish crossover occurs when the shorter moving average crosses below the
	 * longer moving average. This is known as a dead cross.
	 *
	 * @param stock
	 * @return
	 */
	public boolean isBearishCrossover20(Stock stock) {
		boolean isBearishCrossover = false;

		isBearishCrossover = isLongCrossedShortFromHigh(stock.getTechnicals().getPrevEma5(),
				stock.getTechnicals().getPrevEma20(), stock.getTechnicals().getEma5(),
				stock.getTechnicals().getEma20());

		return isBearishCrossover;
	}

	@Deprecated
	public boolean isBearishCrossover100(Stock stock) {
		boolean isBearishCrossover = false;

		isBearishCrossover = isLongCrossedShortFromHigh(stock.getTechnicals().getPrevSma50(),
				stock.getTechnicals().getPrevSma100(), stock.getTechnicals().getSma50(),
				stock.getTechnicals().getSma100());

		return isBearishCrossover;
	}

	public boolean isBearishRule1(Stock stock) {

		boolean isBearish = Boolean.FALSE;

		StockPrice stockPrice = stock.getStockPrice();
		StockTechnicals stockTechnicals = stock.getTechnicals();

		if(stockPrice!=null && stockTechnicals!=null){

			if((stockTechnicals.getAdx() < stockTechnicals.getPrevAdx())
					&&
					(stockTechnicals.getRsi() < stockTechnicals.getPrevRsi())
					&&
					(stockTechnicals.getMacd() < stockTechnicals.getPrevMacd())
					&&
					(stockTechnicals.getMacd() < stockTechnicals.getSignal())){

				if((stockTechnicals.getEma5() < stockTechnicals.getPrevEma5())
						&&
						(stockPrice.getClose() < stockPrice.getOpen())

						&&
						(stockPrice.getCurrentPrice() < stockTechnicals.getEma5())
				){
					isBearish = Boolean.TRUE;
				}

			}


		}


		return isBearish;
	}

	public boolean isBearishRule2(Stock stock) {

		boolean isBearish = Boolean.FALSE;

			StockPrice stockPrice = stock.getStockPrice();
			StockTechnicals stockTechnicals = stock.getTechnicals();

			if(stockPrice!=null && stockTechnicals!=null){

					if((stockTechnicals.getAdx() < stockTechnicals.getPrevAdx())
							&&
							(stockTechnicals.getRsi() < stockTechnicals.getPrevRsi())
							&&
							(stockTechnicals.getMacd() < stockTechnicals.getPrevMacd())
							&&
							(stockTechnicals.getMacd() < stockTechnicals.getSignal())
							&&
							(stockTechnicals.getAdx() < 30.0)
					){
						if(
								(stockPrice.getClose() < stockPrice.getOpen())
							)
						{
							isBearish = Boolean.TRUE;
						}

					}


			}


		return isBearish;
	}

	public boolean isBearishCrossover200(Stock stock) {
		boolean isBearishCrossover = false;

		isBearishCrossover = isLongCrossedShortFromHigh(stock.getTechnicals().getPrevEma50(),
				stock.getTechnicals().getPrevEma200(), stock.getTechnicals().getEma50(),
				stock.getTechnicals().getEma200());

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

	/**
	 * 1. Volume up and price up.
	 * Volume and price rising together presents a bullish signal for short and medium-term investments.
	 *
	 * 2. Volume up and price down.
	 * Falling price with rising volume is a bullish signal. A rally off the bottom is expected to occur.
	 * @param stock
	 * @return
	 */
	public boolean isPriceVolumeBullish(Stock stock) {
		boolean isPriceVolumeBullish = false;

		long volume = stock.getTechnicals().getVolume();
		long avgVolume = stock.getTechnicals().getAvgVolume();
		double prevClose = stock.getStockPrice().getPrevClose();

		double openPrice = stock.getStockPrice().getOpen();
		double high = stock.getStockPrice().getHigh();
		double low = stock.getStockPrice().getLow();
		double close = stock.getStockPrice().getCurrentPrice();

		if (volume > (avgVolume * 5)) {
			//if (stock.getTechnicals().getSma50() > stock.getTechnicals().getSma200()) {
			if (stock.getTechnicals().getEma20() > stock.getTechnicals().getEma50()) {
				if (volume > 500) {
					if ((openPrice > prevClose) && (close > openPrice)) {
						isPriceVolumeBullish = true;
					}
					if((openPrice < prevClose) && (close < openPrice)){
						isPriceVolumeBullish = true;
					}
				}
			}

		}


		return isPriceVolumeBullish;
	}

	public boolean isPriceVolumeBearish(Stock stock) {
		boolean isPriceVolumeBearish = false;

		long volume = stock.getTechnicals().getVolume();
		long avgVolume = stock.getTechnicals().getAvgVolume();
		double prevClose = stock.getStockPrice().getPrevClose();
		double openPrice = stock.getStockPrice().getOpen();
		double high = stock.getStockPrice().getHigh();
		double low = stock.getStockPrice().getLow();
		double close = stock.getStockPrice().getCurrentPrice();

		if (volume > (avgVolume * 5)) {
			//if (stock.getStockPrice().getCurrentPrice() > stock.getTechnicals().getSma200()) {
			//if (stock.getStockPrice().getCurrentPrice() > stock.getTechnicals().getSma20()) {
				if (volume > 500) {
					if ((openPrice > prevClose) && (close < openPrice) ) {
						isPriceVolumeBearish = true;
					}
					if((openPrice < prevClose) && (close > openPrice)){
						isPriceVolumeBearish = true;
					}
				}
			//}

		}


		return isPriceVolumeBearish;
	}

	public boolean isHighVolume(Stock stock) {

		boolean isHighVolume = false;

		long volume = stock.getTechnicals().getVolume();
		long avgVolume = stock.getTechnicals().getAvgVolume();

		if (volume > (avgVolume * 2)) {
			isHighVolume = true;
		}

		return isHighVolume;
	}

	@Deprecated
	public boolean isBreakOut50Bullish(Stock stock) {
		boolean isBreakOut50Bullish = false;

		if (breakoutLedgerService.isBreakout(stock, BreakoutType.POSITIVE, BreakoutCategory.CROSS50)) {
			if (this.isHighVolume(stock)) {
				isBreakOut50Bullish = true;
			}
		}

		return isBreakOut50Bullish;
	}

	@Deprecated
	public boolean isBreakOut50Bearish(Stock stock) {
		boolean isBreakOut50Bearish = false;
		if (stock.getPrimaryIndice() == IndiceType.NIFTY50 || stock.getPrimaryIndice() == IndiceType.NIFTY100) {
			if (stock.getStockPrice().getCurrentPrice() > stock.getTechnicals().getSma200()) {
				if (breakoutLedgerService.isBreakout(stock, BreakoutType.NEGATIVE, BreakoutCategory.CROSS50)) {
					if (this.isHighVolume(stock)) {
						isBreakOut50Bearish = true;
					}
				}
			}
		}

		return isBreakOut50Bearish;
	}

	@Deprecated
	public boolean isBreakOut200HighVolumeBullish(Stock stock) {
		boolean isBreakOut200HighVolumeBullish = false;

		if (breakoutLedgerService.isBreakout(stock, BreakoutType.POSITIVE, BreakoutCategory.CROSS200)) {
			if (this.isHighVolume(stock)) {
				isBreakOut200HighVolumeBullish = true;
			}
		}

		return isBreakOut200HighVolumeBullish;
	}

	@Deprecated
	public boolean isBreakOut200HighVolumeBearish(Stock stock) {
		boolean isBreakOut200HighVolumeBearish = false;

		if (breakoutLedgerService.isBreakout(stock, BreakoutType.NEGATIVE, BreakoutCategory.CROSS200)) {
			if (this.isHighVolume(stock)) {
				isBreakOut200HighVolumeBearish = true;
			}
		}

		return isBreakOut200HighVolumeBearish;
	}
}
