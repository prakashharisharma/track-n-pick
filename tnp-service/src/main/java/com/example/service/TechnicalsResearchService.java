package com.example.service;

import com.example.model.ledger.BreakoutLedger;
import com.example.model.stocks.StockPrice;
import com.example.model.stocks.StockTechnicals;
import com.example.util.FormulaService;
import com.example.util.rules.RulesFundamental;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.BreakoutLedger.BreakoutCategory;
import com.example.model.ledger.BreakoutLedger.BreakoutType;
import com.example.model.master.Stock;

@Service
public class TechnicalsResearchService {

	public enum RsiTrend {OVERBAUGHT, OVERSOLD, NUETRAL}

	@Autowired
	private BreakoutLedgerService breakoutLedgerService;

	@Autowired
	private RulesFundamental rulesFundamental;

	@Autowired
	private FormulaService formulaService;

	@Autowired
	private BreakoutService breakoutService;

	@Autowired
	private VolumeService volumeActionService;

	@Autowired
	private CandleStickService candleStickService;

	@Autowired
	private StockPriceService stockPriceService;

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

	/**
	 * 1. Green Candle
	 * 2. Close above EMA5
	 * 4. EMA20 > EMA200
	 * 5. EMA50 > EMA200
	 * 6. EMA5 increasing
	 * 7. EMA20 increasing
	 * @param stock
	 * @return
	 */
	public boolean isBullishMovingAverage(Stock stock){

		boolean isBullish = Boolean.FALSE;
			StockPrice stockPrice = stock.getStockPrice();
			StockTechnicals stockTechnicals = stock.getTechnicals();
			if(stockPrice!=null && stockTechnicals!=null){

				if(
						(stockPrice.getClose() > stockPrice.getOpen())
						&&
						(stockPrice.getClose() > stockTechnicals.getEma20())
				)
				{
					//if(
					//		(stockTechnicals.getEma20() > stockTechnicals.getPrevEma20())
							//&&
							//(stockTechnicals.getEma50() > stockTechnicals.getPrevEma50())
							//&&
							//(stockPrice.getClose() > stockTechnicals.getEma50())
					//)
					//{
							isBullish = Boolean.TRUE;
					//}
				}
			}

		return isBullish;
	}

	/**
	 * 1. Red Candle
	 * 2. Close below EMA5
	 * 3. EMA5 decreasing
	 * 4. EMA20 decreasing
	 * @param stock
	 * @return
	 */
	public boolean isBearishMovingAverage(Stock stock) {

		StockPrice stockPrice = stock.getStockPrice();
		StockTechnicals stockTechnicals = stock.getTechnicals();

		if(stockPrice!=null && stockTechnicals!=null){
			if(
					(stockPrice.getClose() < stockPrice.getOpen())
					&&
					(stockPrice.getClose() < stockTechnicals.getEma50())
			)
			{
					return Boolean.TRUE;
			}
		}

		return Boolean.FALSE;
	}

	public boolean isMovingAverageBreakout(Stock stock){
		StockPrice stockPrice = stock.getStockPrice();
		StockTechnicals stockTechnicals = stock.getTechnicals();
		if (stockPriceService.changePerIntraday(stockPrice) >= 2.0 && stockPriceService.changePerIntraday(stockPrice) < 10.0) {
			//if (stockPriceService.changePer(stockPrice) >= 2.0 && stockPriceService.changePer(stockPrice) < 12.0) {
			if (candleStickService.upperWickSize(stock) < 1.5) {
				if (this.isHistogramAndMacdIncreased(stock)) {
					if (this.isSignalNearHistogram(stock)) {
						if (this.isRsiEnteredBullishZone(stock)) {
							if (!candleStickService.isSellingWickPresent(stock, 21.0)) {
								if (volumeActionService.isVolumeAboveWeeklyAndMonthlyAverage(stock, 5.0, 2.5)) {
									if (stockTechnicals.getEma5() > stockTechnicals.getPrevEma5()) {
										if (stockTechnicals.getEma20() > stockTechnicals.getPrevEma20()) {
											if (this.isMacdAndSignalBelowZeroLine(stock)) {
												if (stockTechnicals.getEma5() > stockTechnicals.getEma20()) {
													return Boolean.TRUE;
												} else if (this.isPositiveBreakout20(stock)) {
													return Boolean.TRUE;
												} else if (this.isSilverCross(stock)) {
													return Boolean.TRUE;
												} else if (this.isSignalCrossedMacd(stock)) {
													return Boolean.TRUE;
												}
											}

											if (stockTechnicals.getEma50() > stockTechnicals.getPrevEma50()) {
												if (stockTechnicals.getEma20() > stockTechnicals.getEma50()) {
													return Boolean.TRUE;
												}
												if (this.isPositiveBreakout20(stock) || this.isPositiveBreakout50(stock) || this.isPositiveBreakout200(stock)) {
													return Boolean.TRUE;
												} else if (this.isLowRecovery20(stock) || this.isLowRecovery50(stock)) {
													return Boolean.TRUE;
												} else if (this.isSilverCross(stock) || this.isGoldenCross(stock)) {
													return Boolean.TRUE;
												} else if (this.isSignalCrossedMacd(stock)) {
													return Boolean.TRUE;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			//}
		}


		return Boolean.FALSE;
	}

	private double calculateSellingWickBenchmark(StockPrice stockPrice){

		double changePer = stockPriceService.changePerIntraday(stockPrice);

		double benchmark = 35.0;

		if(changePer >= 5 ){
			benchmark =  22.5;
		}else if (changePer >= 3.5){
			benchmark =  25.0;
		}else if (changePer >= 2.5){
			benchmark =  27.5;
		}else if (changePer >= 1.5){
			benchmark =  30.0;
		}

		return benchmark;
	}

	public boolean isPriceActionBreakOut(Stock stock){

		StockTechnicals stockTechnicals = stock.getTechnicals();
		//if(stockTechnicals.getEma20() > stockTechnicals.getPrevEma20()) {
			//if(stockTechnicals.getEma50() > stockTechnicals.getPrevEma50()) {
				if (this.positiveBreakoutScore(stock) > 1 && !candleStickService.isSellingWickPresent(stock)) {
					return Boolean.TRUE;
				}
			//}
		//}
		return Boolean.FALSE;
	}

	public boolean isMacdHistogramBreakout(Stock stock){

		//if(volumeActionService.isLiquidityInVolume(stock) && this.isHistogramAndMacdIncreased(stock) && !candleStickService.isSellingWickPresent(stock, 35.0)){
			if(stock.getStockPrice().getClose() > stock.getTechnicals().getEma5()) {
				if(stock.getTechnicals().getEma5() > stock.getTechnicals().getPrevEma5()){
					if(stock.getTechnicals().getMacd() < 0.0 || stock.getTechnicals().getSignal() < 0.0) {
						if(stock.getTechnicals().getRsi() > 45.0 && stock.getTechnicals().getRsi() < 65.0){
							breakoutLedgerService.addPositive(stock, BreakoutCategory.HISTOGRAM_REVERSED);
							return Boolean.TRUE;
						}
					}
				}
			}
		//}
		return Boolean.FALSE;
	}

	public boolean isMacdAndSignalBelowZeroLine(Stock stock){
		StockTechnicals stockTechnicals = stock.getTechnicals();
		if(stockTechnicals.getMacd() > 0.0 && stockTechnicals.getSignal() > 0.0) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	public double getHistogram(Stock stock){

		StockTechnicals stockTechnicals = stock.getTechnicals();

		return formulaService.calculateHistogram(stockTechnicals.getMacd(), stockTechnicals.getSignal());
	}

	public boolean isHistogramAndMacdIncreased(Stock stock){
		StockTechnicals stockTechnicals = stock.getTechnicals();

		if(stockTechnicals ==null){
			return Boolean.FALSE;
		}

		double histogram = this.getHistogram(stock);

		double prevHistogram = formulaService.calculateHistogram(stockTechnicals.getPrevMacd(), stockTechnicals.getPrevSignal());
			if(histogram > prevHistogram)  {
				//if(Math.floor(prevHistogram) <= 1.0){
					if(stockTechnicals.getMacd() > stockTechnicals.getPrevMacd()) {
						return Boolean.TRUE;
					}
				//}
			}
		return Boolean.FALSE;
	}

	public boolean isHistogramIncreased(Stock stock){
		StockTechnicals stockTechnicals = stock.getTechnicals();

		if(stockTechnicals ==null){
			return Boolean.FALSE;
		}

		double histogram = this.getHistogram(stock);

		double prevHistogram = formulaService.calculateHistogram(stockTechnicals.getPrevMacd(), stockTechnicals.getPrevSignal());
		if(histogram > prevHistogram)  {
			//if(Math.floor(prevHistogram) <= 1.0){
			//if(stockTechnicals.getMacd() > stockTechnicals.getPrevMacd()) {
				return Boolean.TRUE;
			//}
			//}
		}
		return Boolean.FALSE;
	}

	public boolean isMacdIncreased(Stock stock){
		StockTechnicals stockTechnicals = stock.getTechnicals();

		if(stockTechnicals ==null){
			return Boolean.FALSE;
		}

		//double histogram = this.getHistogram(stock);

		//double prevHistogram = formulaService.calculateHistogram(stockTechnicals.getPrevMacd(), stockTechnicals.getPrevSignal());
		//if(histogram > prevHistogram)  {
			//if(Math.floor(prevHistogram) <= 1.0){
			if(stockTechnicals.getMacd() > stockTechnicals.getPrevMacd()) {
				return Boolean.TRUE;
			}
			//}
		//}
		return Boolean.FALSE;
	}

	public boolean isHistogramAndMacdDecreased(Stock stock){

		StockTechnicals stockTechnicals = stock.getTechnicals();

		if(stockTechnicals ==null){
			return Boolean.FALSE;
		}

		double histogram = formulaService.calculateHistogram(stockTechnicals.getMacd(), stockTechnicals.getSignal());

		double prevHistogram = formulaService.calculateHistogram(stockTechnicals.getPrevMacd(), stockTechnicals.getPrevSignal());

		if(histogram < prevHistogram) {
			if(stockTechnicals.getMacd() < stockTechnicals.getPrevMacd()) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	public boolean isHistogramDecreased(Stock stock){

		StockTechnicals stockTechnicals = stock.getTechnicals();

		if(stockTechnicals ==null){
			return Boolean.FALSE;
		}

		double histogram = formulaService.calculateHistogram(stockTechnicals.getMacd(), stockTechnicals.getSignal());

		double prevHistogram = formulaService.calculateHistogram(stockTechnicals.getPrevMacd(), stockTechnicals.getPrevSignal());

		if(histogram < prevHistogram) {
			//if(stockTechnicals.getMacd() < stockTechnicals.getPrevMacd()) {
				return Boolean.TRUE;
			//}
		}
		return Boolean.FALSE;
	}

	public boolean isMacdDecreased(Stock stock){

		StockTechnicals stockTechnicals = stock.getTechnicals();

		if(stockTechnicals ==null){
			return Boolean.FALSE;
		}

		//double histogram = formulaService.calculateHistogram(stockTechnicals.getMacd(), stockTechnicals.getSignal());

		//double prevHistogram = formulaService.calculateHistogram(stockTechnicals.getPrevMacd(), stockTechnicals.getPrevSignal());

		//if(histogram < prevHistogram) {
			if(stockTechnicals.getMacd() < stockTechnicals.getPrevMacd()) {
				return Boolean.TRUE;
			}
		//}
		return Boolean.FALSE;
	}

	public boolean isUpTrend(Stock stock){
		StockTechnicals stockTechnicals= stock.getTechnicals();
		StockPrice stockPrice= stock.getStockPrice();
		if(stockTechnicals==null || stockPrice==null){
			return Boolean.FALSE;
		}

		if(stockTechnicals.getEma200() > stockTechnicals.getPrevEma200()){
			if(stockTechnicals.getEma50() > stockTechnicals.getPrevEma50()){
				if(stockTechnicals.getEma20() > stockTechnicals.getPrevEma20()){
					if(stockPrice.getPrevClose() > stockTechnicals.getPrevEma5()){
						return Boolean.TRUE;
					}
				}
			}
		}
		else if(stockTechnicals.getEma200() < stockTechnicals.getPrevEma200()){
			if(stockTechnicals.getEma50() > stockTechnicals.getPrevEma50()) {
				if (stockTechnicals.getEma20() > stockTechnicals.getPrevEma20()) {
					if (stockPrice.getPrevClose() > stockTechnicals.getPrevEma5()) {
						return Boolean.TRUE;
					}
				}
			}else if(stockTechnicals.getEma50() < stockTechnicals.getPrevEma50()){
				if(stockTechnicals.getEma20() > stockTechnicals.getPrevEma20()){
					if(stockPrice.getPrevClose() > stockTechnicals.getPrevEma5()){
						return Boolean.TRUE;
					}
				}
			}
		}



		return Boolean.FALSE;
	}
	public boolean isDownTrend(Stock stock){
		StockTechnicals stockTechnicals= stock.getTechnicals();
		StockPrice stockPrice= stock.getStockPrice();

		if(stockTechnicals==null || stockPrice==null || stockTechnicals.getEma200() == 0.0){
			return Boolean.FALSE;
		}

		if(stockTechnicals.getEma200() < stockTechnicals.getPrevEma200()){
			if(stockTechnicals.getEma50() < stockTechnicals.getPrevEma50()){
				if(stockTechnicals.getEma20() < stockTechnicals.getPrevEma20()){
					if(stockPrice.getPrevClose() < stockTechnicals.getPrevEma5()){
						return Boolean.TRUE;
					}
				}
			}
		}
		else if(stockTechnicals.getEma200() > stockTechnicals.getPrevEma200()){
			if(stockTechnicals.getEma50() < stockTechnicals.getPrevEma50()) {
				if (stockTechnicals.getEma20() < stockTechnicals.getPrevEma20()) {
					if (stockPrice.getPrevClose() < stockTechnicals.getPrevEma5()) {
						return Boolean.TRUE;
					}
				}
			}else if(stockTechnicals.getEma50() > stockTechnicals.getPrevEma50()){
				if(stockTechnicals.getEma20() < stockTechnicals.getPrevEma20()){
					if(stockPrice.getPrevClose() < stockTechnicals.getPrevEma5()){
						return Boolean.TRUE;
					}
				}
			}
		}



		return Boolean.FALSE;
	}
	public boolean isBreakDownOnTop(Stock stock){

		StockTechnicals stockTechnicals = stock.getTechnicals();
		StockPrice stockPrice = stock.getStockPrice();
		if(stockTechnicals!=null && (Math.ceil(stockTechnicals.getPrevRsi()) >= 65.0)) {

				if (this.isHistogramAndMacdDecreased(stock)) {
					/*stock*/
					//StockPrice stockPrice = stock.getStockPrice();
					//boolean isDayOpenHighestPointOfTheDay = this.isDayOpenHighestPointOfTheDay(stock);

					//boolean isBearishMarubozu = this.isBearishMarubozu(stock);

					if (this.isGapDown(stock)) {
						return Boolean.TRUE;
					} else if (candleStickService.isSellingWickPresent(stock, 67.0)) {
						return Boolean.TRUE;
					} else if (candleStickService.isBuyingWickPresent(stock, 67.0)) {
						return Boolean.TRUE;
					}  else if (candleStickService.isSellingWickPresent(stockPrice.getPrevOpen(), stockPrice.getPrevHigh(), stockPrice.getPrevLow(), stockPrice.getPrevClose(), 67.0)) {
						return Boolean.TRUE;
					}

					if (stockPriceService.changePerIntraday(stockPrice) >= 2.0 && stockPriceService.changePerIntraday(stockPrice) < 15.0) {
						//if(candleStickService.lowerWickSize(stock) < 1.5) {
							if (candleStickService.isBearishEngulfing(stock)) {
								return Boolean.TRUE;
							} else if (candleStickService.isDarkCloudCover(stock)) {
								return Boolean.TRUE;
							} else if (candleStickService.isTweezerTop(stock)) {
								return Boolean.TRUE;
							} else if (candleStickService.isBearishHarami(stock)) {
								return Boolean.TRUE;
							}else if (candleStickService.isBearishKicker(stock)) {
								return Boolean.TRUE;
							}
						//}
					}else if(candleStickService.isShootingStar(stock)){
						return Boolean.TRUE;
					}
			}
		}
		return Boolean.FALSE;
	}

	public boolean isBreakDown(Stock stock){
		 if(this.negativeBreakdownScore(stock) > 1.0){
			 return Boolean.TRUE;
		 }
		return Boolean.FALSE;
	}

	public boolean isHistogramBreakDown(Stock stock){

		if(stock.getStockPrice().getClose() < stock.getStockPrice().getPrevLow()) {
			if(this.isHistogramAndMacdDecreased(stock)){
				breakoutLedgerService.addNegative(stock, BreakoutCategory.MACD_BREAKDOWN_CONFIRMED);
				return Boolean.TRUE;
			}else if( !candleStickService.isBuyingWickPresent(stock)){
				if(stock.getStockPrice().getClose() < stock.getTechnicals().getEma10()){
					if (stock.getStockPrice().getPrevClose() < stock.getTechnicals().getPrevEma10()) {
						if (stock.getStockPrice().getPrevClose() < stock.getStockPrice().getPrevOpen()) {
							breakoutLedgerService.addNegative(stock, BreakoutCategory.MACD_BREAKDOWN_CONFIRMED);
							return Boolean.TRUE;
						}
					}
				}
			}
		}
		return Boolean.FALSE;
	}

	private double positiveBreakoutScore(Stock stock){
		return 0.0;
	}

	private double negativeBreakdownScore(Stock stock){

		double score = 0.00;

		boolean isGapDown = this.isGapDown(stock);

		boolean isDayOpenHighestPointOfTheDay = this.isDayOpenHighestPointOfTheDay(stock);

		boolean isBearishMarubozu = this.isBearishMarubozu(stock);

		boolean isSignalCrossedMacd = this.isSignalCrossedMacd(stock);

		StockPrice stockPrice = stock.getStockPrice();

		StockTechnicals stockTechnicals = stock.getTechnicals();

		//double ema10And20Diff = Math.abs(stockTechnicals.getEma20() - stockTechnicals.getEma10());
		//double ema10And5Diff = Math.abs(stockTechnicals.getEma20() - stockTechnicals.getEma5());

		if(stockPrice.getClose() < stockPrice.getPrevLow()){
			if(stockPrice.getClose() < stockTechnicals.getEma10()) {
				//Previous Candle Red
				if(stockPrice.getPrevClose() < stockPrice.getPrevOpen()){
					if(stockPrice.getPrevClose() < stockTechnicals.getPrevEma10()){
						score = score + 1;
						breakoutLedgerService.addNegative(stock, BreakoutCategory.BREAKDOWN_CONFIRMED);
						score = score + 1;
					}
				}// Closee Below EMA10 and Selling Wick
				else if(candleStickService.isSellingWickPresent(stock, 67.0)) {
					score = score + 1;
				}// Closee Below EMA10 and Bearish Engulfing
				else if(candleStickService.isBearishEngulfing(stock)){
					score = score + 1;
				}//Close Below EMA10 and Prevday Selling Wick
				else if(candleStickService.isSellingWickPresent(stockPrice.getPrevOpen(), stockPrice.getPrevHigh(), stockPrice.getPrevLow(), stockPrice.getPrevClose(), 67.0)){
					score = score + 1;
				}
				else if(isGapDown || isDayOpenHighestPointOfTheDay || isBearishMarubozu){
					score = score + 1;
				}else if(isSignalCrossedMacd){
					score = score + 1;
				}
				/*
				else if(this.isNegativeBreakout200(stock) ){
					score = score + 1;
				}

				else if(this.isNegativeBreakout50(stock) ){
					score = score + 1;
				}

				else if(this.isNegativeBreakout20(stock) ){
					score = score + 1;
				}

				if(this.isDeathCross(stock)){
					score = score + 1;
				}
				 */
			}
		}

		return score;
	}

	private double validatePendingBreakDownConfirmation(Stock stock){

		StockPrice stockPrice = stock.getStockPrice();
		StockTechnicals stockTechnicals = stock.getTechnicals();
		BreakoutLedger breakoutLedger = breakoutLedgerService.get(stock, BreakoutType.NEGATIVE, BreakoutCategory.BREAKDOWN_PENDING_CONFIRMATION);

		if(breakoutLedger!=null){
			if((stockPrice.getClose() < stockPrice.getOpen()) && (stockPrice.getClose() <= stockPrice.getPrevClose())) {
				breakoutLedger.setBreakoutCategory(BreakoutCategory.BREAKDOWN_CONFIRMED);
				breakoutLedgerService.update(breakoutLedger);
				return 4.0;
			}else if(stockPrice.getClose() < stockTechnicals.getPrevFirstSupport()){
				breakoutLedger.setBreakoutCategory(BreakoutCategory.BREAKDOWN_CONFIRMED_S1_BREAKDOWN);
				breakoutLedgerService.update(breakoutLedger);
				return 4.0;
			}
			else{
				breakoutLedger.setBreakoutCategory(BreakoutCategory.BREAKDOWN_NOT_CONFIRMED);
				breakoutLedgerService.update(breakoutLedger);
			}
		}

		return 0.0;
	}

	private boolean isEma20Below50(Stock stock){

		if(stock.getTechnicals().getEma50() > stock.getTechnicals().getEma20()){
			breakoutLedgerService.addNegative(stock, BreakoutCategory.EMA20BELOW50);
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	private boolean isOpenAndCloseBelowEma50(Stock stock){

		double ema50 = stock.getTechnicals().getEma50();

		if((stock.getStockPrice().getOpen() < ema50 )
		&&
				(stock.getStockPrice().getClose() < ema50 )){
			breakoutLedgerService.addNegative(stock, BreakoutCategory.BREAK50EMA);
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}



	/**
	 * 1. DownTrend
	 * 2. Bearish Candle
	 * 3. Bullish Candle
	 * 4. Body of green engulf body of red
	 * @param stock
	 * @return
	 */
	public boolean isBullishEngulfing(Stock stock){
		StockPrice stockPrice = stock.getStockPrice();

		if(stockPrice!=null){

			if(stockPrice.getPrevClose() < stockPrice.getPrevOpen()){
				if(stockPrice.getClose() > stockPrice.getOpen()){
					if(stockPrice.getOpen() < stockPrice.getPrevLow()){
						if(stockPrice.getClose() > stockPrice.getPrevHigh()){
							breakoutLedgerService.addPositive(stock, BreakoutCategory.BULLISH_ENGULFING);
							return Boolean.TRUE;
						}
					}
				}
			}
		}


		return Boolean.FALSE;
	}

	public boolean isBearishEngulfing(Stock stock){
		StockPrice stockPrice = stock.getStockPrice();

		if(stockPrice!=null){

			if(stockPrice.getPrevClose() > stockPrice.getPrevOpen()){
				if(stockPrice.getClose() < stockPrice.getOpen()){
					if(stockPrice.getOpen() > stockPrice.getPrevHigh()){
						if(stockPrice.getClose() < stockPrice.getPrevLow()){
							breakoutLedgerService.addNegative(stock, BreakoutCategory.BEARISH_ENGULFING);
							return Boolean.TRUE;
						}
					}
				}
			}
		}


		return Boolean.FALSE;
	}

	public boolean isBullishhMarubozu(Stock stock){
		StockPrice stockPrice = stock.getStockPrice();

		if(stockPrice!=null){
			if(
					(stockPrice.getClose() > stockPrice.getOpen())
							&&
							(stockPrice.getClose() == stockPrice.getHigh())
							&&
							(stockPrice.getOpen() == stockPrice.getLow())
			){
				breakoutLedgerService.addPositive(stock, BreakoutCategory.BULLISH_MURUBOZU);
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	public boolean isBearishMarubozu(Stock stock){
		StockPrice stockPrice = stock.getStockPrice();

		if(stockPrice!=null){
			if(
					(stockPrice.getClose() < stockPrice.getOpen())
					&&
					(stockPrice.getClose() == stockPrice.getLow())
					&&
					(stockPrice.getOpen() == stockPrice.getHigh())
			){
				breakoutLedgerService.addNegative(stock, BreakoutCategory.BEARISH_MURUBOZU);
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	public boolean isYearHigh(Stock stock){

		StockPrice stockPrice = stock.getStockPrice();

		if(stockPrice.getYearHigh() == stockPrice.getHigh()){
			breakoutLedgerService.addPositive(stock, BreakoutCategory.YEAR_HIGH);
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	@Deprecated
	public boolean isSellingWickPresent(Stock stock){

		StockPrice stockPrice = stock.getStockPrice();

		if(stockPrice.getHigh() == stockPrice.getOpen()){
			return Boolean.FALSE;
		}

		double openHighdiff = stockPrice.getHigh() - stockPrice.getOpen();
		double closeHighDiff = stockPrice.getHigh() - stockPrice.getClose();

		double highWickPerOfBody =  formulaService.calculatePercentage(openHighdiff, closeHighDiff);

		if(highWickPerOfBody > 22.0){
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	@Deprecated
	public boolean isGoldenCross(Stock stock){
		boolean goldenCross  =  this. isShortCrossedLongFromLow(stock.getTechnicals().getPrevEma50(),
				stock.getTechnicals().getPrevEma200(), stock.getTechnicals().getEma50(),
				stock.getTechnicals().getEma200());

		if(goldenCross){
			breakoutLedgerService.addPositive(stock, BreakoutCategory.GOLDEN_CROSS);
		}

		return goldenCross;
	}

	@Deprecated
	public boolean isSilverCross(Stock stock){
		boolean silverCross  =  this. isShortCrossedLongFromLow(stock.getTechnicals().getPrevEma20(),
				stock.getTechnicals().getPrevEma50(), stock.getTechnicals().getEma20(),
				stock.getTechnicals().getEma50());

		if(silverCross){
			breakoutLedgerService.addPositive(stock, BreakoutCategory.SILVER_CROSS);
		}

		return silverCross;
	}

	@Deprecated
	public boolean isDeathCross50(Stock stock){
		boolean isDeathCross  = this.isLongCrossedShortFromHigh(stock.getTechnicals().getPrevEma20(), stock.getTechnicals().getPrevEma50(), stock.getTechnicals().getEma20(), stock.getTechnicals().getEma50());

		if(isDeathCross){
			breakoutLedgerService.addNegative(stock, BreakoutCategory.DEATHCROSS50);
		}

		return isDeathCross;
	}

	@Deprecated
	public boolean isDeathCross(Stock stock){
		boolean isDeathCross  = this.isLongCrossedShortFromHigh(stock.getTechnicals().getPrevSma50(), stock.getTechnicals().getPrevSma200(), stock.getTechnicals().getSma50(), stock.getTechnicals().getSma200());

		if(isDeathCross){
			breakoutLedgerService.addNegative(stock, BreakoutCategory.DEATHCROSS200);
		}

		return isDeathCross;
	}
	public boolean isCloseBelowEma20(Stock stock){

		if( stock.getStockPrice().getClose() < stock.getTechnicals().getEma20()){
			breakoutLedgerService.addNegative(stock, BreakoutCategory.CLOSE_BELOW_20);
			return  Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	public boolean isCloseBelowEma50(Stock stock){

		if( stock.getStockPrice().getClose() < stock.getTechnicals().getEma50()){
			breakoutLedgerService.addNegative(stock, BreakoutCategory.CLOSE_BELOW_50);
			return  Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	/**
	 * If Close < EMA50
	 * prevClose < PrevEma50 ()
	 * prev candle color red
	 * plusDi decreasing
	 * minusDi increasing
	 * histogram is -higher from prev
	 * @param stock
	 * @return
	 */
	public boolean isPrevCloseBelowEma50(Stock stock){
		StockPrice stockPrice = stock.getStockPrice();
		StockTechnicals stockTechnicals = stock.getTechnicals();

		if(stockPrice.getPrevClose() < stockTechnicals.getPrevEma50()){
			if(stockPrice.getPrevClose() < stockPrice.getPrevOpen()){

						double histogram = formulaService.calculateHistogram(stockTechnicals.getMacd(), stockTechnicals.getSignal());

						double prevHistogram = formulaService.calculateHistogram(stockTechnicals.getPrevMacd(), stockTechnicals.getPrevSignal());

						if(histogram > prevHistogram) {
							breakoutLedgerService.addNegative(stock, BreakoutCategory.BREAK_EMA50_SUPPORT);
							return Boolean.TRUE;
						}
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * Ooen > prevHigh
	 * Close > open
	 * @param stock
	 * @return
	 */
	public boolean isGapUp(Stock stock){

		StockPrice stockPrice = stock.getStockPrice();

		if( stockPrice != null
				&&
				(stockPrice.getOpen() >= stockPrice.getPrevHigh())
				&&
				(stockPrice.getLow() >= stockPrice.getPrevClose())

		){
			breakoutLedgerService.addPositive(stock, BreakoutCategory.GAP_UP);
			return Boolean.TRUE;

		}

		return Boolean.FALSE;
	}

	/**
	 * open < prevLow
	 * close < open
	 * @param stock
	 * @return
	 */
	public boolean isGapDown(Stock stock){

		StockPrice stockPrice = stock.getStockPrice();

		if(stockPrice != null
			    &&
				(stockPrice.getOpen() <= stockPrice.getPrevLow())
				&&
				(stockPrice.getHigh() <= stockPrice.getPrevClose())

		){
			breakoutLedgerService.addNegative(stock, BreakoutCategory.GAP_DOWN);

			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	public boolean isDayOpenLowestPointOfTheDay(Stock stock){

		StockPrice stockPrice = stock.getStockPrice();

		if(stockPrice != null
				&&
				(stockPrice.getOpen() < stockPrice.getClose())
				&&
				(formulaService.isEpsilonEqual(stockPrice.getLow(), stockPrice.getOpen()))
		){
			breakoutLedgerService.addPositive(stock, BreakoutCategory.DAY_OPEN_LOWEST);

			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	public boolean isDayOpenHighestPointOfTheDay(Stock stock){

		StockPrice stockPrice = stock.getStockPrice();

		if(stockPrice != null
				&&
				(stockPrice.getOpen() > stockPrice.getClose())
				&&
				(formulaService.isEpsilonEqual(stockPrice.getHigh(), stockPrice.getOpen()))
		){
			breakoutLedgerService.addNegative(stock, BreakoutCategory.DAY_OPEN_HIGHEST);

			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	public boolean isMacdCrossSignal(Stock stock){

		boolean isMAcdCrossedSignal =  this. isShortCrossedLongFromLow(stock.getTechnicals().getPrevMacd(),
				stock.getTechnicals().getPrevSignal(), stock.getTechnicals().getMacd(),
				stock.getTechnicals().getSignal());

		if(isMAcdCrossedSignal){
			breakoutLedgerService.addPositive(stock, BreakoutCategory.MACD_CROSSED_SIGNAL);
		}

		return Boolean.FALSE;
	}

	public boolean isSignalCrossedMacd(Stock stock){

		boolean isSignalCrossedMacd =  this. isShortCrossedLongFromLow(stock.getTechnicals().getPrevSignal(),
				stock.getTechnicals().getPrevMacd(), stock.getTechnicals().getSignal(),
				stock.getTechnicals().getMacd());

		if(isSignalCrossedMacd){
			breakoutLedgerService.addNegative(stock, BreakoutCategory.SIGNAL_CROSSED_MACD);
		}

		return Boolean.FALSE;
	}

	public boolean isSignalNearHistogram(Stock stock){

		StockTechnicals stockTechnicals  = stock.getTechnicals();

		if(stockTechnicals!=null){

			double histogram = formulaService.calculateHistogram(stockTechnicals.getMacd(), stockTechnicals.getSignal());

			if(stockTechnicals.getSignal() < 0.0 || histogram < 0.0){
				return Boolean.TRUE;
			}else if(stockTechnicals.getSignal() <= histogram){
				breakoutLedgerService.addPositive(stock, BreakoutCategory.SIGNAL_NEAR_HISTOGRAM);
				return Boolean.TRUE;
			}
		}

		return Boolean.FALSE;
	}

	public boolean isRsiEnteredBullishZone(Stock stock){

		StockTechnicals stockTechnicals  = stock.getTechnicals();

			if(stockTechnicals.getRsi() > stockTechnicals.getPrevRsi()){
				if(Math.ceil(stockTechnicals.getRsi()) < 65.0) {
					boolean isRsiEnteredBullishZone = this.isShortCrossedLongFromLow(Math.floor(stockTechnicals.getPrevRsi()), 50.0, Math.ceil(stockTechnicals.getRsi()), 50.0);
					if (isRsiEnteredBullishZone) {
						breakoutLedgerService.addPositive(stock, BreakoutCategory.RSI_ENTERED_BULLISH);
						return Boolean.TRUE;
					} else if (Math.ceil(stockTechnicals.getPrevRsi()) >= 50.0) {
						breakoutLedgerService.addPositive(stock, BreakoutCategory.RSI_CONTINUED_BULLISH);
						return Boolean.TRUE;
					}
				}
			}

		return Boolean.FALSE;
	}

	public boolean isRsiEnteredBearishZone(Stock stock){

		StockTechnicals stockTechnicals  = stock.getTechnicals();

		if( stockTechnicals!=null
				&&
				(stockTechnicals.getRsi() < stockTechnicals.getPrevRsi())
				&&
				(stockTechnicals.getRsi() < 40 && stockTechnicals.getPrevRsi() >= 40)
		){
			if(
				(stockTechnicals.getPrevRsi() < 40)
				||
				(stockTechnicals.getRsi() < 40 && stockTechnicals.getPrevRsi() >= 40)
			){

			breakoutLedgerService.addNegative(stock, BreakoutCategory.RSI_ENTERED_BEARISH);
			}
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	public boolean isPositiveBreakoutPastResistance(Stock stock){

		StockTechnicals stockTechnicals = stock.getTechnicals();
		StockPrice stockPrice = stock.getStockPrice();

		if(stockPrice.getClose() > stockTechnicals.getPrevThirdResistance()){
			breakoutLedgerService.addPositive(stock, BreakoutCategory.BREAKOUT_PAST_RESISTANCE);
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	public boolean isNegativeBreakdownPastSupport(Stock stock){

		StockTechnicals stockTechnicals = stock.getTechnicals();
		StockPrice stockPrice = stock.getStockPrice();

		if(stockPrice.getClose() < stockTechnicals.getPrevSecondResistance()){
			breakoutLedgerService.addNegative(stock, BreakoutCategory.BREAKDOWN_PAST_SUPPORT);
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	@Deprecated
	public boolean isVolumeAboveAverage(Stock stock){

		StockTechnicals stockTechnicals  = stock.getTechnicals();


		if(stockTechnicals.getVolumeAvg5() >= 10000000 &&  (stockTechnicals.getVolume() > (stockTechnicals.getVolumeAvg5() * 2))
		){
			breakoutLedgerService.addPositive(stock, BreakoutCategory.VOLUME_HIGH);
			return Boolean.TRUE;
		}

		if(stockTechnicals.getVolumeAvg5() >= 5000000 &&  (stockTechnicals.getVolume() > (stockTechnicals.getVolumeAvg5() * 3))
		){
			breakoutLedgerService.addPositive(stock, BreakoutCategory.VOLUME_HIGH);
			return Boolean.TRUE;
		}

		if(stockTechnicals.getVolumeAvg5() >= 2500000 &&  (stockTechnicals.getVolume() > (stockTechnicals.getVolumeAvg5() * 4))
		){
			breakoutLedgerService.addPositive(stock, BreakoutCategory.VOLUME_HIGH);
			return Boolean.TRUE;
		}

		/*
		if(stockTechnicals.getWeeklyVolume() >= 500000 &&  (stockTechnicals.getVolume() > (stockTechnicals.getWeeklyVolume() * 7.5))
		){
			breakoutLedgerService.addPositive(stock, BreakoutCategory.VOLUME_HIGH);
			return Boolean.TRUE;
		}
		 */

		if((stockTechnicals.getVolume() > (stockTechnicals.getVolumeAvg5() * 5))
		){
			breakoutLedgerService.addPositive(stock, BreakoutCategory.VOLUME_HIGH);
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	@Deprecated
	public boolean isBullishCrossOver200(Stock stock) {

		boolean isBullishCrossOver = false;

		isBullishCrossOver = isShortCrossedLongFromLow(stock.getTechnicals().getPrevSma50(),
				stock.getTechnicals().getPrevSma200(), stock.getTechnicals().getSma50(),
				stock.getTechnicals().getSma200());

		return isBullishCrossOver;
	}

	public boolean isEMA200Increasing(Stock stock){

		StockTechnicals stockTechnicals = stock.getTechnicals();

		if(stockTechnicals.getEma200() > stockTechnicals.getPrevEma200()){
			breakoutLedgerService.addPositive(stock, BreakoutCategory.ALL_EMA_TREND_UP);
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	public boolean isPositiveLowAboveEma20(Stock stock){

		if(stock.getStockPrice().getLow() > stock.getTechnicals().getEma20()){
			breakoutLedgerService.addPositive(stock, BreakoutCategory.LOW_ABOVE_EMA20);
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	@Deprecated
	public boolean isPositiveBreakout20(Stock stock) {
		return false;
	}

	public boolean isLowRecovery20(Stock stock){
		if(stock.getStockPrice().getOpen() < stock.getTechnicals().getEma20()){
			if(stock.getStockPrice().getClose() > stock.getTechnicals().getEma20()){
				breakoutLedgerService.addPositive(stock, BreakoutCategory.RECOVERY_20);
				return  Boolean.TRUE;
			}
		}

		return  Boolean.FALSE;
	}

	public boolean isLowRecovery50(Stock stock){
		if(stock.getStockPrice().getOpen() < stock.getTechnicals().getEma50()){
			if(stock.getStockPrice().getClose() > stock.getTechnicals().getEma50()){
				breakoutLedgerService.addPositive(stock, BreakoutCategory.RECOVERY_50);
				return  Boolean.TRUE;
			}
		}

		return  Boolean.FALSE;
	}

	public boolean isLowRecovery200(Stock stock){
		if(stock.getStockPrice().getOpen() < stock.getTechnicals().getEma200()){
			if(stock.getStockPrice().getClose() > stock.getTechnicals().getEma200()){
				breakoutLedgerService.addPositive(stock, BreakoutCategory.RECOVERY_200);
				return  Boolean.TRUE;
			}
		}

		return  Boolean.FALSE;
	}

	public boolean isHighFall200(Stock stock){
		if(stock.getStockPrice().getOpen() > stock.getTechnicals().getEma200()){
			if(stock.getStockPrice().getClose() < stock.getTechnicals().getEma200()){
				breakoutLedgerService.addNegative(stock, BreakoutCategory.FALL_200);
				return  Boolean.TRUE;
			}
		}

		return  Boolean.FALSE;
	}

	public boolean isHighFall50(Stock stock){
		if(stock.getStockPrice().getOpen() > stock.getTechnicals().getEma50()){
			if(stock.getStockPrice().getClose() < stock.getTechnicals().getEma50()){
				breakoutLedgerService.addNegative(stock, BreakoutCategory.FALL_50);
				return  Boolean.TRUE;
			}
		}

		return  Boolean.FALSE;
	}

	public boolean isHighFall20(Stock stock){
		if(stock.getStockPrice().getOpen() > stock.getTechnicals().getEma20()){
			if(stock.getStockPrice().getClose() < stock.getTechnicals().getEma20()){
				breakoutLedgerService.addNegative(stock, BreakoutCategory.FALL_20);
				return  Boolean.TRUE;
			}
		}

		return  Boolean.FALSE;
	}

	public boolean isHighFall10(Stock stock){
		if(stock.getStockPrice().getOpen() > stock.getTechnicals().getEma10()){
			if(stock.getStockPrice().getClose() < stock.getTechnicals().getEma10()){
				breakoutLedgerService.addNegative(stock, BreakoutCategory.FALL_10);
				return  Boolean.TRUE;
			}
		}

		return  Boolean.FALSE;
	}

	public boolean isHighFall5(Stock stock){
		if(stock.getStockPrice().getOpen() > stock.getTechnicals().getEma5()){
			if(stock.getStockPrice().getClose() < stock.getTechnicals().getEma5()){
				breakoutLedgerService.addNegative(stock, BreakoutCategory.FALL_10);
				return  Boolean.TRUE;
			}
		}

		return  Boolean.FALSE;
	}

	@Deprecated
	public boolean isPositiveBreakout50(Stock stock) {
		return false;
	}

	@Deprecated
	public boolean isPositiveBreakout100(Stock stock) {
		return false;
	}

	@Deprecated
	public boolean isPositiveBreakout200(Stock stock) {
		return false;
	}

	public boolean isShortCrossedLongFromLow(double prevShortTermAvg, double prevLongTermAvg, double shortTermAvg,
			double longTermAvg) {

		return CrossOverUtil.isFastCrossesAboveSlow(prevShortTermAvg, prevLongTermAvg, shortTermAvg, longTermAvg);
	}

	public boolean isLongCrossedShortFromHigh(double prevShortTermAvg, double prevLongTermAvg, double shortTermAvg,
			double longTermAvg) {

		return CrossOverUtil.isSlowCrossesBelowFast(prevShortTermAvg, prevLongTermAvg, shortTermAvg, longTermAvg);
	}


	/**
	 * A bearish crossover occurs when the shorter moving average crosses below the
	 * longer moving average. This is known as a dead cross.
	 * 
	 * @param stock
	 * @return
	 */
	@Deprecated
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
	@Deprecated
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

	public boolean isNegativeBreakout20(Stock stock) {
		return false;
	}

	public boolean isNegativeBreakout50(Stock stock) {
		return false;
	}

	public boolean isNegativeBreakout100(Stock stock) {
		return false;
	}

	public boolean isNegativeBreakout200(Stock stock) {
		return false;
	}

}
