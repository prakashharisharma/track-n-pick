package com.example.service;

import javax.transaction.Transactional;

import com.example.model.ledger.BreakoutLedger;
import com.example.model.master.Stock;
import com.example.model.stocks.StockPrice;
import com.example.model.stocks.StockTechnicals;
import com.example.util.FibonacciRatio;
import com.example.util.FormulaService;
import com.example.util.io.model.type.Trend;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.example.service.CandleStickService.MAX_WICK_SIZE;

@Transactional
@Service
public class StockPriceService {

	@Autowired
	private BreakoutLedgerService breakoutLedgerService;
	@Autowired
	private FormulaService formulaService;


	public boolean isYearHigh(Stock stock){

		StockPrice stockPrice = stock.getStockPrice();

		if(stockPrice.getYearHigh() == stockPrice.getHigh()){
			breakoutLedgerService.addPositive(stock, BreakoutLedger.BreakoutCategory.YEAR_HIGH);
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	public boolean isTmaConvergenceAndDivergence(Stock stock, Trend trend){

		if(this.isTmaDivergence(stock, trend) && this.isTmaInAverageRange(stock, trend)){
			return Boolean.TRUE;
		}

		return Boolean.FALSE;
	}

	public boolean isTmaInPriceRange(Stock stock, Trend trend){
		StockTechnicals stockTechnicals = stock.getTechnicals();
		StockPrice stockPrice = stock.getStockPrice();
		if(trend == Trend.LONG){
			return this.isTmaInPriceRange(stockPrice.getLow(), stockPrice.getHigh(), stockTechnicals.getEma50(), stockTechnicals.getEma100(), stockTechnicals.getEma200());
		}else if(trend == Trend.MEDIUM){
			return this.isTmaInPriceRange(stockPrice.getLow(), stockPrice.getHigh(),stockTechnicals.getEma20(), stockTechnicals.getEma50(), stockTechnicals.getEma100());
		}else if(trend == Trend.SHORT){
			return this.isTmaInPriceRange(stockPrice.getLow(), stockPrice.getHigh(),stockTechnicals.getEma5(), stockTechnicals.getEma10(), stockTechnicals.getEma20());
		}

		return Boolean.FALSE;
	}

	private boolean isTmaDivergence(Stock stock, Trend trend){
		StockTechnicals stockTechnicals = stock.getTechnicals();

		if(trend == Trend.LONG){
			return this.isTmaDivergence(stockTechnicals.getEma50(), stockTechnicals.getEma100(), stockTechnicals.getEma200());
		}else if(trend == Trend.MEDIUM){
			return this.isTmaDivergence(stockTechnicals.getEma20(), stockTechnicals.getEma50(), stockTechnicals.getEma100());
		}else if(trend == Trend.SHORT){
			return this.isTmaDivergence(stockTechnicals.getEma5(), stockTechnicals.getEma10(), stockTechnicals.getEma20());
		}

		return Boolean.FALSE;
	}

	private boolean isTmaDivergence(double immediateLow, double average, double immediateHigh){

		if(immediateLow > average){
			if(average > immediateHigh){
				return Boolean.TRUE;
			}
		}

		return Boolean.FALSE;
	}

	private boolean isTmaInAverageRange(Stock stock, Trend trend){
		StockTechnicals stockTechnicals = stock.getTechnicals();

		if(trend == Trend.LONG){
			return this.isTmaInAverageRange(stockTechnicals.getEma50(), stockTechnicals.getEma100(), stockTechnicals.getEma200());
		}else if(trend == Trend.MEDIUM){
			return this.isTmaInAverageRange(stockTechnicals.getEma20(), stockTechnicals.getEma50(), stockTechnicals.getEma100());
		}else if(trend == Trend.SHORT){
			return this.isTmaInAverageRange(stockTechnicals.getEma5(), stockTechnicals.getEma10(), stockTechnicals.getEma20());
		}

		return Boolean.FALSE;
	}

	private boolean isTmaInAverageRange(double immediateLow, double average, double immediateHigh){
		double averageTMA = this.averageTMA(immediateLow, average, immediateHigh);
		double avgLowerRange = formulaService.applyPercentChange(averageTMA, -1 * this.maxTmaRange());
		double avgHigherRange = formulaService.applyPercentChange(averageTMA, 1 * this.maxTmaRange());
		if(formulaService.inRange(Math.floor(avgLowerRange), Math.ceil(avgHigherRange), immediateLow)) {
			if (formulaService.inRange(Math.floor(avgLowerRange), Math.ceil(avgHigherRange), average)) {
				if (formulaService.inRange(Math.floor(avgLowerRange), Math.ceil(avgHigherRange), immediateHigh)) {
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}

	private boolean isTmaInPriceRange(double low, double high, double immediateLow, double average, double immediateHigh){

		double avgLowerRange = formulaService.applyPercentChange(low, -1 * this.maxTmaRange());
		double avgHigherRange = formulaService.applyPercentChange(high, 1 * this.maxTmaRange());
		if(formulaService.inRange(Math.floor(avgLowerRange), Math.ceil(avgHigherRange), immediateLow)) {
			if (formulaService.inRange(Math.floor(avgLowerRange), Math.ceil(avgHigherRange), average)) {
				if (formulaService.inRange(Math.floor(avgLowerRange), Math.ceil(avgHigherRange), immediateHigh)) {
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}

	private double averageTMA(double immediateLow, double average, double immediateHigh){
		return (immediateLow + average + immediateHigh) / 3;
	}

	public boolean isCloseAboveEma20(Stock stock){
		return CrossOverUtil.isSlowCrossesBelowFast(stock.getStockPrice().getPrevClose(), stock.getTechnicals().getPrevEma20(), stock.getStockPrice().getClose(), stock.getTechnicals().getEma20());

	}

	public boolean isCloseAboveEma50(Stock stock){
		return CrossOverUtil.isSlowCrossesBelowFast(stock.getStockPrice().getPrevClose(), stock.getTechnicals().getPrevEma50(), stock.getStockPrice().getClose(), stock.getTechnicals().getEma50());
	}

	public boolean isCloseAboveEma100(Stock stock){
		return CrossOverUtil.isSlowCrossesBelowFast(stock.getStockPrice().getPrevClose(), stock.getTechnicals().getPrevEma100(), stock.getStockPrice().getClose(), stock.getTechnicals().getEma100());
	}

	public boolean isCloseAboveEma200(Stock stock){
		return CrossOverUtil.isSlowCrossesBelowFast(stock.getStockPrice().getPrevClose(), stock.getTechnicals().getPrevEma200(), stock.getStockPrice().getClose(), stock.getTechnicals().getEma200());
	}

	public double changePer(StockPrice stockPrice){

		return Math.abs(formulaService.calculateChangePercentage(stockPrice.getPrevClose(), stockPrice.getClose()));
	}

	public double changePerIntraday(StockPrice stockPrice){

		return Math.abs(formulaService.calculateChangePercentage(stockPrice.getOpen(), stockPrice.getClose()));
	}

	public double correction(Stock stock){

		StockPrice stockPrice = stock.getStockPrice();

		double yearHigh = stock.getStockPrice().getYearHigh();

		double lowPrev = stockPrice.getPrevLow();
		double low = stockPrice.getLow();

		if(low > lowPrev){
			low = lowPrev;
		}

		if(yearHigh == 0.00 || low == 0.00){
			return 0.00;
		}

		return Math.abs(formulaService.calculateChangePercentage(yearHigh, low));
	}

	private double maxTmaRange(){
		return formulaService.applyPercentChange(MAX_WICK_SIZE, FibonacciRatio.RATIO_161_8*100);
	}
}
