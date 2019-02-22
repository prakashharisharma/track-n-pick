package com.example.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.master.Stock;
import com.example.model.stocks.Direction;
import com.example.external.ta.service.TechnicalRatioService;

@Service
public class TechnicalsResearchService {

	@Autowired
	private StockService stockService;

	@Autowired
	private WatchListService watchListService;

	@Autowired
	private UserService userService;

	@Autowired
	private RuleService ruleService;

	@Autowired
	private TechnicalRatioService technicalRatioService;

	@Autowired
	private UndervalueLedgerService undervalueLedgerService;

	public void technicalsResearch() {

		List<Stock> stocksList = undervalueLedgerService.getCurrentUndervalueStocks();

		System.out.println("-------------PRINT--------------------");

		System.out.println("-------------TECHNICALS RESEARCH--------------------");
		System.out.println("-------------bullishCrossOver RESEARCH--------------------");
		this.bullishCrossOver(stocksList).forEach(stock -> {
			this.printTechnicals(stock);
		});
		System.out.println("-------------bearishCrossover RESEARCH--------------------");
		this.bearishCrossover(stocksList).forEach(stock -> {
			this.printTechnicals(stock);
		});
		System.out.println("-------------uptrendReversalList RESEARCH--------------------");
		this.uptrendReversal(stocksList).forEach(stock -> {
			this.printTechnicals(stock);
		});

		System.out.println("-------------downtrendReversal RESEARCH--------------------");
		this.downtrendReversal(stocksList).forEach(stock -> {
			this.printTechnicals(stock);
		});
		System.out.println("-------------positiveOversold RESEARCH--------------------");
		this.positiveOversold(stocksList).forEach(stock -> {
			this.printTechnicals(stock);
		});
		System.out.println("-------------negativeOversold RESEARCH--------------------");
		this.negativeOversold(stocksList).forEach(stock -> {
			this.printTechnicals(stock);
		});

		System.out.println("-------------LongUpMidUp RESEARCH--------------------");
		this.LongUpMidUp(stocksList).forEach(stock -> {
			this.printTechnicals(stock);
		});
		System.out.println("-------------LongUpMidDown RESEARCH--------------------");
		this.LongUpMidDown(stocksList).forEach(stock -> {
			this.printTechnicals(stock);
		});
		System.out.println("-------------LongDownMidUp RESEARCH--------------------");
		this.LongDownMidUp(stocksList).forEach(stock -> {
			this.printTechnicals(stock);
		});
		System.out.println("-------------LongDownMidDown RESEARCH--------------------");
		this.LongDownMidDown(stocksList).forEach(stock -> {
			this.printTechnicals(stock);
		});
	}

	private void printTechnicals(Stock stock) {
		System.out.println(stock.getNseSymbol() + " : " + stock.getStockPrice().getCurrentPrice() + " : "
				+ stock.getTechnicals().getSma50() + " : " + stock.getTechnicals().getSma200() + " "
				+ stock.getTechnicals().getRsi());
	}

	/**
	 * A bullish crossover occurs when the shorter moving average crosses above the
	 * longer moving average. This is also known as a golden cross.
	 * 
	 * @param nifty200stocksList
	 * @return
	 */
	public List<Stock> bullishCrossOver(List<Stock> stocksList) {

		// List<Stock> stocksList = this.fundamentalnifty50Stocks();

		List<Stock> bullishCrossOverList = new ArrayList<>();

		for (Stock stock : stocksList) {
			if (stock.getTechnicals().getCurrentTrend() == Direction.UPTREND) {
				if (stock.getTechnicals().getPrevSma200() > stock.getTechnicals().getPrevSma50()) {
					if (stock.getTechnicals().getSma50() > stock.getTechnicals().getSma200()) {
						bullishCrossOverList.add(stock);
					}
				}
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
		// List<Stock> stocksList = this.fundamentalnifty50Stocks();

		List<Stock> bearishCrossoverList = new ArrayList<>();
		for (Stock stock : stocksList) {
			if (stock.getTechnicals().getCurrentTrend() == Direction.DOWNTREND) {
				if (stock.getTechnicals().getPrevSma200() < stock.getTechnicals().getPrevSma50()) {
					if (stock.getTechnicals().getSma50() < stock.getTechnicals().getSma200()) {
						bearishCrossoverList.add(stock);
					}
				}
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
		// List<Stock> stocksList = this.fundamentalnifty50Stocks();

		boolean isBearishCrossover = false;

		if (stock.getTechnicals().getPrevSma200() <= stock.getTechnicals().getPrevSma50()) {
			if (stock.getTechnicals().getSma50() < stock.getTechnicals().getSma200()) {
				isBearishCrossover = true;
			}
		}

		return isBearishCrossover;
	}

	/**
	 * Uptrend - But RSI below 30
	 * 
	 * @param nifty200stocksList
	 * @return
	 */
	public List<Stock> uptrendReversal(List<Stock> stocksList) {
		// List<Stock> stocksList = this.fundamentalnifty50Stocks();

		List<Stock> uptrendReversalList = new ArrayList<>();
		for (Stock stock : stocksList) {
			if (stock.getTechnicals().getMidTermTrend() == Direction.UPTREND) {
				if (stock.getTechnicals().getRsi() <= 30.00) {
					uptrendReversalList.add(stock);
				}
			}
		}
		return uptrendReversalList;
	}

	/**
	 * DownTrend - But RSI above 70
	 * 
	 * @param nifty200stocksList
	 * @return
	 */
	public List<Stock> downtrendReversal(List<Stock> stocksList) {
		// List<Stock> stocksList = this.fundamentalnifty50Stocks();

		List<Stock> downtrendReversalList = new ArrayList<>();
		for (Stock stock : stocksList) {
			if (stock.getTechnicals().getMidTermTrend() == Direction.DOWNTREND) {
				if (stock.getTechnicals().getRsi() >= 70.00) {
					downtrendReversalList.add(stock);
				}
			}
		}
		return downtrendReversalList;
	}

	/**
	 * If SMA50 > SMA200 and RSI < 35
	 * 
	 * @return
	 */

	public List<Stock> positiveOversold(List<Stock> stocksList) {
		// List<Stock> stocksList = this.fundamentalnifty50Stocks();

		List<Stock> positiveOversoldList = new ArrayList<>();
		for (Stock stock : stocksList) {
			if (stock.getTechnicals().getLongTermTrend() == Direction.UPTREND
					&& stock.getTechnicals().getMidTermTrend() == Direction.DOWNTREND) {
				if (stock.getTechnicals().getSma50() > stock.getTechnicals().getSma200()) {
					if (stock.getTechnicals().getRsi() <= 33.00) {
						positiveOversoldList.add(stock);
					}
				}
			}
		}
		return positiveOversoldList;
	}

	/**
	 * If SMA50 < SMA200 and RSI < 35
	 * 
	 * @return
	 */
	public List<Stock> negativeOversold(List<Stock> stocksList) {
		// List<Stock> stocksList = this.fundamentalnifty50Stocks();

		List<Stock> negativeOversoldList = new ArrayList<>();
		for (Stock stock : stocksList) {
			if (stock.getTechnicals().getLongTermTrend() == Direction.DOWNTREND
					&& stock.getTechnicals().getMidTermTrend() == Direction.UPTREND) {
				if (stock.getTechnicals().getSma50() < stock.getTechnicals().getSma200()) {
					if (stock.getTechnicals().getRsi() >= 67.00) {
						negativeOversoldList.add(stock);
					}
				}
			}
		}
		return negativeOversoldList;
	}

	public List<Stock> LongUpMidUp(Collection<Stock> stocks) {
		return stocks.stream().filter(stock -> stock.getTechnicals().getLongTermTrend() == Direction.UPTREND
				&& stock.getTechnicals().getMidTermTrend() == Direction.UPTREND).collect(Collectors.toList());
	}

	public List<Stock> LongUpMidDown(Collection<Stock> stocks) {
		return stocks.stream().filter(stock -> stock.getTechnicals().getLongTermTrend() == Direction.UPTREND
				&& stock.getTechnicals().getMidTermTrend() == Direction.DOWNTREND).collect(Collectors.toList());
	}

	public List<Stock> LongDownMidUp(Collection<Stock> stocks) {
		return stocks.stream().filter(stock -> stock.getTechnicals().getLongTermTrend() == Direction.DOWNTREND
				&& stock.getTechnicals().getMidTermTrend() == Direction.UPTREND).collect(Collectors.toList());
	}

	public List<Stock> LongDownMidDown(Collection<Stock> stocks) {
		return stocks.stream().filter(stock -> stock.getTechnicals().getLongTermTrend() == Direction.DOWNTREND
				&& stock.getTechnicals().getMidTermTrend() == Direction.DOWNTREND).collect(Collectors.toList());
	}

	public List<Stock> longTermUptrend(Collection<Stock> stocks) {
		return stocks.stream().filter(stock -> stock.getTechnicals().getLongTermTrend() == Direction.UPTREND)
				.collect(Collectors.toList());
	}

	public List<Stock> longTermDowntrend(Collection<Stock> stocks) {
		return stocks.stream().filter(stock -> stock.getTechnicals().getLongTermTrend() == Direction.DOWNTREND)
				.collect(Collectors.toList());
	}

	public List<Stock> midTermUptrend(Collection<Stock> stocks) {
		return stocks.stream().filter(stock -> stock.getTechnicals().getMidTermTrend() == Direction.UPTREND)
				.collect(Collectors.toList());
	}

	public List<Stock> midTermDowntrend(Collection<Stock> stocks) {
		return stocks.stream().filter(stock -> stock.getTechnicals().getMidTermTrend() == Direction.DOWNTREND)
				.collect(Collectors.toList());
	}

}
