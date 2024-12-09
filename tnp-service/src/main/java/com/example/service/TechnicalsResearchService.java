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
						(stockPrice.getClose() > stockTechnicals.getEma5())
				)
				{
					if(
							(stockTechnicals.getEma20() > stockTechnicals.getPrevEma20())
							&&
							(stockTechnicals.getEma50() > stockTechnicals.getPrevEma50())
							&&
							(stockPrice.getClose() > stockTechnicals.getEma200())
					)
					{
							isBullish = Boolean.TRUE;
					}
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

		boolean isBearish = Boolean.FALSE;

		StockPrice stockPrice = stock.getStockPrice();
		StockTechnicals stockTechnicals = stock.getTechnicals();

		if(stockPrice!=null && stockTechnicals!=null){

			if(
					(stockPrice.getClose() < stockPrice.getOpen())
			)
			{
				if(
						(stockTechnicals.getEma5() < stockTechnicals.getPrevEma5())
						&&
						(stockTechnicals.getEma20() < stockTechnicals.getPrevEma20())
						&&
						(stockTechnicals.getEma50() < stockTechnicals.getPrevEma50())
				){
					isBearish = Boolean.TRUE;
				}
			}

		}

		return isBearish;
	}


	public boolean isAdxTrendUp(Stock stock){

		boolean isBullish = Boolean.FALSE;

		//if(stock.getStockFactor()!=null && stock.getStockFactor().getMarketCap() > rulesFundamental.getMcap()){
			StockPrice stockPrice = stock.getStockPrice();
			StockTechnicals stockTechnicals = stock.getTechnicals();
			if(stockPrice!=null && stockTechnicals!=null){
					if(
							(
							((stockTechnicals.getAdx() > 25.0)
							&&
							(stockTechnicals.getPrevAdx() <= 25))
							||
							(stockTechnicals.getAdx() > 25.0)
							)
							&&
							(stockTechnicals.getPlusDi() > stockTechnicals.getMinusDi())
						    &&
							(stockTechnicals.getPlusDi() > stockTechnicals.getPrevPlusDi())
							&&
							(stockTechnicals.getMinusDi() < stockTechnicals.getPrevMinusDi())
					){
								breakoutLedgerService.addPositive(stock, BreakoutCategory.ADX_TREND_UP);
								isBullish = Boolean.TRUE;
						}
				}
		//}
		return isBullish;
	}

	public double breakoutScore(Stock stock){

		StockPrice stockPrice  = stock.getStockPrice();

		if(stockPrice.getClose() > stockPrice.getOpen()){
			return this.positiveBreakoutScore(stock);
		}

		return this.negativeBreakdownScore(stock);

	}

	private double positiveBreakoutScore(Stock stock){

		double score = 0.00;

		boolean isVolumeAboveAverage = this.isVolumeAboveAverage(stock);

		boolean isMacdCrossSignal = this.isMacdCrossSignal(stock);

		boolean isRsiEnteredBullishZone = this.isRsiEnteredBullishZone(stock);

		boolean isGapUp = this.isGapUp(stock);

		boolean isAdxTrendUp = this.isAdxTrendUp(stock);

		boolean isPositiveBreakout20 = this.isPositiveBreakout20(stock);

		boolean isPositiveBreakout50 = this.isPositiveBreakout50(stock);

		boolean isPositiveBreakout200 = this.isPositiveBreakout200(stock);

		boolean isDayOpenLowestPointOfTheDay = this.isDayOpenLowestPointOfTheDay(stock);

		boolean isPositiveBreakoutPastResistance = this.isPositiveBreakoutPastResistance(stock);

		boolean isPositiveLowAboveEma20 = this.isPositiveLowAboveEma20(stock);

		//boolean isBullishEngulfing = this.isBullishEngulfing(stock);

		boolean isBullishhMarubozu = this.isBullishhMarubozu(stock);

		boolean isEMA200Increasing = this.isEMA200Increasing(stock);

		boolean isSilverCross = this.isSilverCross(stock);

		score = score + this.validatePendingBreakDownConfirmation(stock);

		if(isVolumeAboveAverage){
			score = score + 0.5;
			if(isGapUp || isBullishhMarubozu || isDayOpenLowestPointOfTheDay ||  isPositiveLowAboveEma20){
				score = score + 0.5;

				if(isEMA200Increasing){
					score = score + 0.75;
				}

				if(isRsiEnteredBullishZone){
					score = score + 0.75;
				}

				if(isPositiveBreakoutPastResistance){
					score = score + 0.5;
				}

				if(isPositiveBreakout200){
					score = score + 1;
				}

				if(isSilverCross){
					score = score + 1.25;
				}

				if(isMacdCrossSignal){
					score = score + 1.25;
				}

				if(isAdxTrendUp){
					score = score + 1;
				}
			}

		}

		if(this.isGoldenCross(stock)){
			score = score + 2;
		}

		return score;
	}

	private double negativeBreakdownScore(Stock stock){

		double score = 0.00;

		boolean isGapDown = this.isGapDown(stock);
		//boolean isVolumeAboveAverage = this.isVolumeAboveAverage(stock);
		boolean isSignalCrossedMacd = this.isSignalCrossedMacd(stock);
		//boolean isRsiEnteredBearishZone = this.isRsiEnteredBearishZone(stock);

		boolean isDayOpenHighestPointOfTheDay = this.isDayOpenHighestPointOfTheDay(stock);

		//boolean isBearishEngulfing = this.isBearishEngulfing(stock);
		boolean isBearishMarubozu = this.isBearishMarubozu(stock);
		//boolean isDoji = this.isDoji(stock);
		boolean isCloseBelowEma50 = this.isCloseBelowEma50(stock);

		//StockTechnicals stockTechnicals = stock.getTechnicals();

		score = score + this.validatePendingBreakDownConfirmation(stock);

		if(this.isNegativeBreakout50(stock) || this.isNegativeBreakout20(stock) || isCloseBelowEma50){
			if(isGapDown || isDayOpenHighestPointOfTheDay || isBearishMarubozu || isSignalCrossedMacd ) {
				breakoutLedgerService.addNegative(stock, BreakoutCategory.BREAKDOWN_PENDING_CONFIRMATION);
			}
		}

		if(this.isNegativeBreakout200(stock) ){
			score = score + 3;
		}

		if(this.isDeathCross(stock)){
			score = score + 3;
		}

		return score;
	}

	private double validatePendingBreakDownConfirmation(Stock stock){

		StockPrice stockPrice = stock.getStockPrice();
		StockTechnicals stockTechnicals = stock.getTechnicals();
		BreakoutLedger breakoutLedger = breakoutLedgerService.get(stock, BreakoutType.NEGATIVE, BreakoutCategory.BREAKDOWN_PENDING_CONFIRMATION);

		if(breakoutLedger!=null){
			if((stockPrice.getClose() < stockPrice.getOpen()) && (stockPrice.getClose() < stockPrice.getPrevClose())) {
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

		double highWickPerOfBody =  formulaService.calculatePercentRate(openHighdiff, closeHighDiff);

		if(highWickPerOfBody > 22.0){
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	@Deprecated
	public boolean isGoldenCross(Stock stock){
		boolean goldenCross  =  this. isShortCrossedLongFromLow(stock.getTechnicals().getPrevSma50(),
				stock.getTechnicals().getPrevSma200(), stock.getTechnicals().getSma50(),
				stock.getTechnicals().getSma200());

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
				(stockPrice.getClose() > stockPrice.getOpen())
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
				(stockPrice.getClose() < stockPrice.getOpen())
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

			double histogram = stockTechnicals.getMacd() - stockTechnicals.getSignal();

			if(stockTechnicals.getSignal() <= histogram ){
				breakoutLedgerService.addPositive(stock, BreakoutCategory.SIGNAL_NEAR_HISTOGRAM);
				return Boolean.TRUE;
			}
		}

		return Boolean.FALSE;
	}

	public boolean isRsiEnteredBullishZone(Stock stock){

		StockTechnicals stockTechnicals  = stock.getTechnicals();

		if( stockTechnicals!=null
		&&
				(stockTechnicals.getRsi() > stockTechnicals.getPrevRsi())

		){
			if(stockTechnicals.getPrevRsi() > 60 ){
				breakoutLedgerService.addPositive(stock, BreakoutCategory.RSI_CONTINUED_BULLISH);
				return Boolean.TRUE;
			}
			else if(
					stockTechnicals.getRsi() > 60 && stockTechnicals.getPrevRsi() <= 60
			) {
				breakoutLedgerService.addPositive(stock, BreakoutCategory.RSI_ENTERED_BULLISH);
				return Boolean.TRUE;
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

			if((stockTechnicals.getVolume() > (stockTechnicals.getWeeklyVolume() * 5))
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

		boolean isPositiveBreakout = false;

		isPositiveBreakout = breakoutService.isBreakOut20(stock);

		if(isPositiveBreakout) {
			breakoutLedgerService.addPositive(stock, BreakoutCategory.CROSS20);
		}

		return isPositiveBreakout;
	}

	@Deprecated
	public boolean isPositiveBreakout50(Stock stock) {

		boolean isPositiveBreakout = false;

		isPositiveBreakout = breakoutService.isBreakOut50(stock);

		if(isPositiveBreakout) {
			breakoutLedgerService.addPositive(stock, BreakoutCategory.CROSS50);
		}

		return isPositiveBreakout;
	}

	@Deprecated
	public boolean isPositiveBreakout100(Stock stock) {

		boolean isPositiveBreakout = false;

		isPositiveBreakout = breakoutService.isBreakOut100(stock);

		return isPositiveBreakout;
	}

	@Deprecated
	public boolean isPositiveBreakout200(Stock stock) {

		boolean isPositiveBreakout = false;

		isPositiveBreakout = breakoutService.isBreakOut200(stock);
		if(isPositiveBreakout) {
			breakoutLedgerService.addPositive(stock, BreakoutCategory.CROSS200);
		}
		return isPositiveBreakout;
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
		boolean isNegativeBreakout = false;

		isNegativeBreakout = breakoutService.isBreakDown20(stock);
		if(isNegativeBreakout) {
			breakoutLedgerService.addNegative(stock, BreakoutCategory.CROSS20);
		}
		return isNegativeBreakout;
	}

	public boolean isNegativeBreakout50(Stock stock) {
		boolean isNegativeBreakout = false;

		isNegativeBreakout = breakoutService.isBreakDown50(stock);

		if(isNegativeBreakout) {
			breakoutLedgerService.addNegative(stock, BreakoutCategory.CROSS50);
		}
		return isNegativeBreakout;
	}

	public boolean isNegativeBreakout100(Stock stock) {
		boolean isNegativeBreakout = false;

		isNegativeBreakout = breakoutService.isBreakDown100(stock);

		return isNegativeBreakout;
	}

	public boolean isNegativeBreakout200(Stock stock) {
		boolean isNegativeBreakout = false;

		isNegativeBreakout = breakoutService.isBreakDown200(stock);

		if(isNegativeBreakout){
			breakoutLedgerService.addNegative(stock, BreakoutCategory.CROSS200);
		}

		return isNegativeBreakout;
	}

}
