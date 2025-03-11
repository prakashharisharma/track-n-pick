package com.example;

import java.io.IOException;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.dto.OHLCV;
import com.example.external.factor.FactorRediff;
import com.example.external.ta.service.McService;
import com.example.model.ledger.ResearchLedgerTechnical;
import com.example.model.stocks.StockPrice;
import com.example.model.stocks.StockTechnicals;
import com.example.model.stocks.UserPortfolio;
import com.example.model.um.UserProfile;
import com.example.repo.ledger.FundsLedgerRepository;
import com.example.repo.ledger.ResearchLedgerTechnicalRepository;
import com.example.repo.ledger.TradeLedgerRepository;
import com.example.repo.master.StockRepository;
import com.example.repo.stocks.StockPriceRepository;
import com.example.repo.stocks.StockTechnicalsRepository;
import com.example.service.*;
import com.example.service.calc.*;
import com.example.service.impl.FundamentalResearchService;
import com.example.service.util.StockPriceUtil;
import com.example.storage.model.assembler.StockPriceOHLCVAssembler;
import com.example.ui.service.UiRenderUtil;
import com.example.util.io.model.ResearchIO;
import com.example.util.io.model.type.Trend;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.external.bhav.service.DownloadBhavService;
import com.example.external.dylh.service.DylhService;
import com.example.integration.service.RestClientService;
import com.example.model.master.Stock;
import com.example.mq.producer.QueueService;
import com.example.repo.master.HolidayCalendarRepository;
import com.example.repo.stocks.PortfolioRepository;
import com.example.storage.repo.PriceTemplate;
import com.example.storage.repo.TechnicalsTemplate;
import com.example.storage.repo.TradingSessionTemplate;
import com.example.storage.service.StorageService;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import com.example.util.io.model.StockPriceIO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.ta4j.core.*;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.criteria.pnl.ReturnCriterion;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.MinusDIIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.volume.OnBalanceVolumeIndicator;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

@Component
@Slf4j
public class AppRunner implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppRunner.class);

	@Autowired
	private RestClientService restClientService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private ResearchLedgerFundamentalService researchLedgerFundamentalService;

	@Autowired
	private FileNameService fileNameService;

	@Autowired
	private StockTechnicalsRepository stockTechnicalsRepository;

	@Autowired
	private StockPriceRepository stockPricesRepository;

	@Autowired
	private OhlcvService ohlcvService;
	@Autowired
	private PortfolioService portfolioService;

	@Autowired
	private DylhService dylhService;

	@Autowired
	private StockService stockService;

	@Autowired
	private SectorService sectorService;

	@Autowired
	private DividendLedgerService dividendLedgerService;

	@Autowired
	private FundsLedgerService fundsLedgerService;
	@Autowired
	private HolidayCalendarRepository holidayCalendarRepository;
	@Autowired
	private CalendarService calendarService;
	@Autowired
	private MiscUtil miscUtil;
	@Autowired
	private PortfolioRepository portfolioRepository;

	@Autowired
	private ExpenseService expenseService;

	@Autowired
	private WatchListService watchListService;
	
	@Autowired
	private StorageService storageService;

	@Autowired
	private TradingSessionTemplate storageTemplate;

	@Autowired
	private DownloadBhavService downloadBhavService;

	@Autowired
	private TechnicalsResearchService technicalsService;

	@Autowired
	private TechnicalsTemplate technicalsTemplate;

	@Autowired
	private PriceTemplate priceTemplate;

	@Autowired
	private TradingSessionTemplate tradingSessionTemplate;

	@Autowired
	private FormulaService formulaService;

	@Autowired
	private StockPriceOHLCVAssembler stockPriceOHLCVAssembler;

	@Autowired
	private QueueService queueService;
	@Autowired
	private ResearchLedgerTechnicalService tecnicalLedger;
	
	@Autowired
	private TechnicalsResearchService technicalsResearchService;

	@Autowired
	private MovingAverageActionService movingAverageActionService;

	@Autowired
	private UiRenderUtil uiRenderUtil;

	@Autowired
	private StockFactorService stockFactorService;

	@Autowired
	private FactorRediff factorRediff;

	@Autowired
	private FundsLedgerRepository fundsLedgerRepository;

	@Autowired
	private TradeLedgerRepository tradeLedgerRepository;

	@Autowired
	private ResearchExecutorService researchExecutorService;
	@Autowired
	private OnBalanceVolumeCalculatorService onBalanceVolumeCalculatorService;

	@Autowired
	private RelativeStrengthIndexCalculatorService rsiService;

	@Autowired
	private ExponentialMovingAverageCalculatorService exponentialMovingAverageService;

	@Autowired
	private MovingAverageConvergenceDivergenceService movingAverageConvergenceDivergenceService;

	@Autowired
	private AverageDirectionalIndexCalculatorService averageDirectionalIndexService;

	@Autowired
	private McService mcService;

	@Autowired
	private ResearchLedgerTechnicalRepository researchLedgerRepository;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockPriceService stockPriceService;

	@Autowired
	private BreakoutService breakoutService;

	@Autowired
	private ResearchLedgerTechnicalRepository researchLedgerTechnicalRepository;

	@Autowired
	private ResearchLedgerTechnicalService researchLedgerTechnicalService;

	@Autowired
	private CandleStickExecutorService candleStickExecutorService;

	@Autowired
	private CandleStickHelperService candleStickHelperService;
	@Autowired
	private SwingActionService swingActionService;

	@Autowired
	private PriceActionService priceActionService;

	@Autowired
	private VolumeActionService volumeActionService;
	@Autowired
	private FundamentalResearchService fundamentalResearchService;
	@Autowired
	private CandleStickService candleStickService;

	@Autowired
	private UpdateTechnicalsService updateTechnicalsService;

	@Autowired
	private VolumeService volumeService;

	@Autowired
	private  PositionService positionService;

	@Autowired
	private TrendService trendService;

	@Autowired
	private QuarterlySupportResistanceService quarterlySupportResistanceService;
	@Autowired
	private MonthlySupportResistanceService monthlySupportResistanceService;

	@Autowired
	private WeeklySupportResistanceService weeklySupportResistanceService;

	@Autowired
	private YearlySupportResistanceService yearlySupportResistanceService;

	@Autowired
	private TimeframeSupportResistanceService timeframeSupportResistanceService;

	@Override
	public void run(String... arg0) throws InterruptedException, IOException {

		log.info("Application started....");

		//this.testCandleStick();
		//this.updateYearHighLow();
		//this.testObv();
		//this.testTimeFrameSR();
		//this.testTrend();
		this.scanCandleStickPattern();
		this.allocatePositions();
		/*
		Stock stock = stockService.getStockByNseSymbol("ISEC");
		Trend tred = trendService.isDownTrend(stock);
		System.out.println("trend " + tred.getStrength() + " Momentum " + tred.getMomentum());
		tred = trendService.isUpTrend(stock);
		System.out.println("trend " + tred.getStrength() + " Momentum " + tred.getMomentum());
		*/


		//this.testScore();
		//this.processBhavFromApi();
		//this.updateSupportAndResistance();
		//updateTechnicalsService.updateTechnicals();
		//this.syncTechnicals();
		/*
		System.out.println(miscUtil.previousMonthFirstDay());
		System.out.println(miscUtil.previousMonthLastDay());
		LocalDate from = calendarService.nextTradingDate(miscUtil.previousMonthFirstDay().minusDays(1));
		LocalDate to  = calendarService.previousTradingSession(miscUtil.currentMonthFirstDay());

		List<StockPrice> stockPriceListNew =  priceTemplate.get("BAJFINANCE", LocalDate.of(2024, 12, 27), LocalDate.of(2025,1,28));

		stockPriceListNew.forEach(stockPrice -> {
			System.out.println(stockPrice);
		});

		System.out.println("*************");
		StockPrice monthlyHigh = Collections.max(stockPriceListNew, Comparator.comparingDouble(p -> p.getHigh()));
		System.out.println(monthlyHigh);
		System.out.println("monthlyHigh: " + monthlyHigh.getHigh());
		System.out.println("*************");
		StockPrice monthlyLow = Collections.min(stockPriceListNew, Comparator.comparingDouble(p -> p.getLow()));
		System.out.println(monthlyLow);
		System.out.println("monthlyLow: " + monthlyLow.getLow());

		stockPriceListNew =  priceTemplate.get("BAJFINANCE", 20);


		stockPriceListNew.forEach(stockPrice -> {
			System.out.println(stockPrice);
		});

		System.out.println("*************");
		 monthlyHigh = Collections.max(stockPriceListNew, Comparator.comparingDouble(p -> p.getHigh()));
		System.out.println(monthlyHigh);
		System.out.println("monthlyHigh: " + monthlyHigh.getHigh());
		System.out.println("*************");
		 monthlyLow = Collections.min(stockPriceListNew, Comparator.comparingDouble(p -> p.getLow()));
		System.out.println(monthlyLow);
		System.out.println("monthlyLow: " + monthlyLow.getLow());

		System.out.println("*************");
		 */



		System.out.println("STARTED");

	}

	private void testScore(){
		List<ResearchLedgerTechnical> researchLedgerTechnicalList = researchLedgerTechnicalService.allActiveResearch();

		for(ResearchLedgerTechnical researchLedgerTechnical: researchLedgerTechnicalList){
			//candleStickExecutorService.executeBearish(researchLedgerTechnical.getStock());
			double score = researchLedgerTechnicalService.calculateScore(researchLedgerTechnical);

			System.out.println(" SYMBOL " + researchLedgerTechnical.getStock().getNseSymbol() + " SCORE " + score);
		}
	}

	/**
	 * Position Size = (Total trading fund * Risk%)/SL%
	 */
	private void allocatePositions(){


		double capital = 750000.0;
		double riskFactor = 1.0;

		List<UserPortfolio> portfolioList =  new ArrayList<>();

		UserProfile user = userService.getUserByUsername("phsdhan");

		List<ResearchLedgerTechnical> researchLedgerTechnicalList = researchLedgerTechnicalService.allActiveResearch();

		for(ResearchLedgerTechnical researchLedgerTechnical : researchLedgerTechnicalList){

			/*
			if(researchLedgerTechnical.getStrategy() == ResearchLedgerTechnical.Strategy.VOLUME){
				risk = RiskFactor.VOLUME_VOLUME;
			}
			else if(researchLedgerTechnical.getStrategy() == ResearchLedgerTechnical.Strategy.PRICE){
				if(researchLedgerTechnical.getSubStrategy() == ResearchLedgerTechnical.SubStrategy.SRMA) {
					risk = RiskFactor.PRICE_SRMA;
				}
				else if(researchLedgerTechnical.getSubStrategy() == ResearchLedgerTechnical.SubStrategy.SRTF) {
					risk = RiskFactor.PRICE_SRTF;
				}else if(researchLedgerTechnical.getSubStrategy() == ResearchLedgerTechnical.SubStrategy.RMAO) {
					risk = RiskFactor.PRICE_RMAO;
				}
			}
			else if(researchLedgerTechnical.getStrategy() == ResearchLedgerTechnical.Strategy.SWING){
				if(researchLedgerTechnical.getSubStrategy() == ResearchLedgerTechnical.SubStrategy.RM) {
					risk = RiskFactor.SWING_RM;
				}
				else if(researchLedgerTechnical.getSubStrategy() == ResearchLedgerTechnical.SubStrategy.TEMA) {
					risk = RiskFactor.SWING_TEMA;
				}
			}*/

			double allottedAmount = ((capital * riskFactor) / researchLedgerTechnical.getRisk());

			long positionSize = (long) (allottedAmount / researchLedgerTechnical.getResearchPrice());

			double reward = formulaService.calculateChangePercentage(researchLedgerTechnical.getResearchPrice(), researchLedgerTechnical.getTarget());

			//if(researchLedgerTechnical.getResearchDate().isEqual(LocalDate.now()) || researchLedgerTechnical.getResearchDate().isEqual(calendarService.previousTradingSession(LocalDate.now()))) {
				//System.out.println(researchLedgerTechnical.getStock().getNseSymbol() + " date: " + researchLedgerTechnical.getResearchDate() +" strategy: "+ researchLedgerTechnical.getStrategy() +" sub strategy: "+ researchLedgerTechnical.getSubStrategy() +" risk: "+ risk + " reward: "+ miscUtil.formatDouble(reward) + " amount:" + miscUtil.formatDouble(allottedAmount) + " positions : " + positionSize +" entry: " +researchLedgerTechnical.getResearchPrice());
			//}

			double risk = formulaService.calculateFraction(capital, riskFactor);
			double stopLoss = (researchLedgerTechnical.getResearchPrice()  - researchLedgerTechnical.getStopLoss());
			positionSize = (long) (risk / stopLoss);
			positionService.calculate(user, researchLedgerTechnical);
			//System.out.println(researchLedgerTechnical.getStock().getNseSymbol() + " date: " + researchLedgerTechnical.getResearchDate() +" strategy: "+ researchLedgerTechnical.getStrategy() +" sub strategy: "+ researchLedgerTechnical.getSubStrategy() +" risk: "+ risk + " sl: "+ miscUtil.formatDouble(stopLoss) + " amount:" + miscUtil.formatDouble(allottedAmount) + " positions : " + positionSize +" entry: " +researchLedgerTechnical.getResearchPrice());

		}

	}

	private void  testTrend(){
		List<Stock> stockList = new ArrayList<>();
		Stock stock = stockService.getStockByNseSymbol("ACC");
		stockList.add(stock);
		stock = stockService.getStockByNseSymbol("AUBANK");
		stockList.add(stock);
		stock = stockService.getStockByNseSymbol("AWL");
		stockList.add(stock);
		stock = stockService.getStockByNseSymbol("BALRAMCHIN");
		stockList.add(stock);

		stockList.forEach(s ->{
			Trend trend = trendService.isUpTrend(s);
			System.out.println(s.getNseSymbol() +  " UP " + trend);
			trend = trendService.isDownTrend(s);
			System.out.println(s.getNseSymbol() + " DOWN " + trend);
		});

	}

	private void updateYearHighLow(){


		List<Stock> stockList = stockService.getForActivity();
		AtomicInteger count = new AtomicInteger(stockList.size());
		stockList.forEach(stk ->{
			try {

				StockPrice stockPrice = stk.getStockPrice();

				OHLCV ohlcv = yearlySupportResistanceService.supportAndResistance(stk.getNseSymbol(), LocalDate.now());

				double yearHigh = ohlcv.getHigh();
				com.example.storage.model.StockPrice stockPrice1 = priceTemplate.getByHigh(stk.getNseSymbol(), yearHigh);
				LocalDate yearHighDate = LocalDate.ofInstant(stockPrice1.getBhavDate(), ZoneOffset.UTC);
				double yearLow = ohlcv.getLow();
				stockPrice1 = priceTemplate.getByLow(stk.getNseSymbol(), ohlcv.getLow());
				LocalDate yearLowDate = LocalDate.ofInstant(stockPrice1.getBhavDate(), ZoneOffset.UTC);

				System.out.println(stk.getNseSymbol() + " yearHigh: " + yearHigh +" on " + yearHighDate +" yearLow: " + yearLow + " on " + yearLowDate);

				stockPrice.setYearHigh(yearHigh);
				stockPrice.setYearHighDate(yearHighDate);
				stockPrice.setYearLow(yearLow);
				stockPrice.setYearLowDate(yearLowDate);

				stockPricesRepository.save(stockPrice);

				System.out.println("Completed for " + stk.getNseSymbol());

				stk.setActivityCompleted(Boolean.TRUE);
				stockRepository.save(stk);
				System.out.println("Remaining " + count.decrementAndGet());


			}catch (Exception e){
				System.out.println("An error occured while updating year high Low " + stk.getNseSymbol() + " " +e);
			}
		});
	}

	private void updateSupportAndResistance(){
		List<Stock> stockList = stockService.getForActivity();
		AtomicInteger count = new AtomicInteger(stockList.size());
		stockList.forEach(stk ->{
			try {
				StockTechnicals stockTechnicals = stk.getTechnicals();

			/*
				OHLCV ohlcv = weeklySupportResistanceService.supportAndResistance(stk);
				stockTechnicals.setPrevWeekOpen(ohlcv.getOpen());
				stockTechnicals.setPrevWeekHigh(ohlcv.getHigh());
				stockTechnicals.setPrevWeekLow(ohlcv.getLow());
				stockTechnicals.setPrevWeekClose(ohlcv.getClose());
			 */
				/*
				OHLCV ohlcv = monthlySupportResistanceService.supportAndResistance(stk);
				stockTechnicals.setPrevMonthOpen(ohlcv.getOpen());
				stockTechnicals.setPrevMonthHigh(ohlcv.getHigh());
				stockTechnicals.setPrevMonthLow(ohlcv.getLow());
				stockTechnicals.setPrevMonthClose(ohlcv.getClose());
				*/

				OHLCV ohlcv = quarterlySupportResistanceService.supportAndResistance(stk);
				stockTechnicals.setPrevQuarterOpen(ohlcv.getOpen());
				stockTechnicals.setPrevQuarterHigh(ohlcv.getHigh());
				stockTechnicals.setPrevQuarterLow(ohlcv.getLow());
				stockTechnicals.setPrevQuarterClose(ohlcv.getClose());

/*
				OHLCV ohlcv = yearlySupportResistanceService.supportAndResistance(stk);
				stockTechnicals.setPrevYearOpen(ohlcv.getOpen());
				stockTechnicals.setPrevYearHigh(ohlcv.getHigh());
				stockTechnicals.setPrevYearLow(ohlcv.getLow());
				stockTechnicals.setPrevYearClose(ohlcv.getClose());
				*/

				stockTechnicalsRepository.save(stockTechnicals);

				System.out.println("Completed for " + stk.getNseSymbol());

				stk.setActivityCompleted(Boolean.TRUE);
				stockRepository.save(stk);
				System.out.println("Remaining " + count.decrementAndGet());
			}catch(Exception e){
				System.out.println("An error occured while updating SR " + stk.getNseSymbol() +" " + e);
			}
		});
	}

	private void testCandleStick(){
		System.out.println("******* Testing CandleSticks *******");
		Stock stock = StockPriceUtil.buildStockPrice("KEI", LocalDate.of(2025,02,14), 3410.0, 3475.0, 3330.95, 3411.65
				,3518.10, 3518.10, 3390.40, 3404.0);

		candleStickService.isDoji(stock);
		// 31-Jan-25
		stock = StockPriceUtil.buildStockPrice("JINDALSTEL", LocalDate.of(2025,01,31), 785.05, 796.80, 723.35, 791.55
				,849.0, 864.20, 821.30, 840.05);
		candleStickService.isHammer(stock);
		stock = StockPriceUtil.buildStockPrice("BHEL", 184.14, 200.52, 184.14, 199.86
				,196.25, 197.45, 186.0, 187.62);
		candleStickService.isBullishEngulfing(stock);
		stock = StockPriceUtil.buildStockPrice("INDIASHLTR", 628.0, 645.0, 607.0, 641.85
				,628.0, 634.95, 607.35, 615.75);
		candleStickService.isBullishOutsideBar(stock);
		stock = StockPriceUtil.buildStockPrice("GABRIEL", 397.25, 422.35, 397.25, 420.0
				,418.0, 422.45, 387.0, 397.25);
		candleStickService.isTweezerBottom(stock);

		stock = StockPriceUtil.buildStockPrice("FDC", 608.0, 625.0, 592.55, 597.25
				,585.0, 658.85, 582.80, 612.65);
		candleStickService.isBearishInsideBar(stock);
		stock = StockPriceUtil.buildStockPrice("FDC", 608.0, 625.0, 592.55, 597.25
				,585.0, 658.85, 582.80, 612.65);
		candleStickService.isBearishHarami(stock);

 		candleStickService.isBullishKicker(stock);
		candleStickService.isBullishSash(stock);
		candleStickService.isBullishSeparatingLine(stock);
		candleStickService.isBullishhMarubozu(stock);

		candleStickService.isDoubleLow(stock);
		candleStickService.isBullishHarami(stock);
		candleStickService.isRisingWindow(stock);

	}


	public Strategy buildStrategy(BarSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}

		final ClosePriceIndicator closePriceIndicator = new ClosePriceIndicator(series);
		final SMAIndicator smaIndicator = new SMAIndicator(closePriceIndicator, 50);

		final int adxBarCount = 14;
		final ADXIndicator adxIndicator = new ADXIndicator(series, adxBarCount);
		final OverIndicatorRule adxOver20Rule = new OverIndicatorRule(adxIndicator, 20);

		final PlusDIIndicator plusDIIndicator = new PlusDIIndicator(series, adxBarCount);
		final MinusDIIndicator minusDIIndicator = new MinusDIIndicator(series, adxBarCount);

		final Rule plusDICrossedUpMinusDI = new CrossedUpIndicatorRule(plusDIIndicator, minusDIIndicator);
		final Rule plusDICrossedDownMinusDI = new CrossedDownIndicatorRule(plusDIIndicator, minusDIIndicator);
		final OverIndicatorRule closePriceOverSma = new OverIndicatorRule(closePriceIndicator, smaIndicator);
		final Rule entryRule = adxOver20Rule.and(plusDICrossedUpMinusDI).and(closePriceOverSma);

		final UnderIndicatorRule closePriceUnderSma = new UnderIndicatorRule(closePriceIndicator, smaIndicator);
		final Rule exitRule = adxOver20Rule.and(plusDICrossedDownMinusDI).and(closePriceUnderSma);

		return new BaseStrategy("ADX", entryRule, exitRule, adxBarCount);
	}
	private void ta4J(String nseSymbol){

		BarSeries barSeries = new BaseBarSeriesBuilder().withName("my_2017_series").build();

		LocalDate to  = calendarService.previousTradingSession(LocalDate.now());
		LocalDate from = to.minusYears(3);
		List<OHLCV> ohlcvList =  ohlcvService.fetch(nseSymbol, from, to);
		ohlcvList.forEach(ohlcv -> {
			barSeries.addBar(ZonedDateTime.ofInstant(ohlcv.getBhavDate(), ZoneOffset.UTC), ohlcv.getOpen(), ohlcv.getHigh(), ohlcv.getLow(), ohlcv.getClose(), ohlcv.getVolume());
		});

		ClosePriceIndicator closePrice = new ClosePriceIndicator(barSeries);

		SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
		// Here is the 5-bars-SMA value at the 42nd index
		System.out.println("5-bars-SMA : " + shortSma.getValue(ohlcvList.size()-1).doubleValue());

		EMAIndicator emaIndicator = new EMAIndicator(closePrice, 5);
		System.out.println("5-bars-EMA : " + emaIndicator.getValue(ohlcvList.size()-1).doubleValue());
		emaIndicator = new EMAIndicator(closePrice, 20);
		System.out.println("20-bars-EMA : " + emaIndicator.getValue(ohlcvList.size()-1).doubleValue());
		emaIndicator = new EMAIndicator(closePrice, 50);
		System.out.println("50-bars-EMA : " + emaIndicator.getValue(ohlcvList.size()-1).doubleValue());
		emaIndicator = new EMAIndicator(closePrice, 200);
		System.out.println("200-bars-EMA : " + emaIndicator.getValue(ohlcvList.size()-1).doubleValue());


		RSIIndicator rsiIndicator = new RSIIndicator(closePrice, 14);
		System.out.println("RSI : " + rsiIndicator.getValue(ohlcvList.size()-1).doubleValue());

		MACDIndicator macdIndicator = new MACDIndicator(closePrice);
		System.out.println("MACD : " + macdIndicator.getValue(ohlcvList.size()-1).doubleValue());
		System.out.println("Signal : " + macdIndicator.getSignalLine(ohlcvList.size()-1));
		ADXIndicator adxIndicator = new ADXIndicator(barSeries,14);
		System.out.println("ADX : " + adxIndicator.getValue(ohlcvList.size()-1).doubleValue());

		OnBalanceVolumeIndicator onBalanceVolumeIndicator = new OnBalanceVolumeIndicator(barSeries);
		SMAIndicator obvAverage = new SMAIndicator(onBalanceVolumeIndicator, 9);
		System.out.println("OBV : " + onBalanceVolumeIndicator.getValue(ohlcvList.size()-1));
		System.out.println("OBV AVERAGE : " + obvAverage.getValue(ohlcvList.size()-1));


		// Building the trading strategy
		Strategy strategy = buildStrategy(barSeries);

		// Running the strategy
		BarSeriesManager seriesManager = new BarSeriesManager(barSeries);
		TradingRecord tradingRecord = seriesManager.run(strategy);
		System.out.println("Number of positions for the strategy: " + tradingRecord.getPositionCount());

		// Analysis
		System.out.println(nseSymbol + " Total return for the strategy: " + new ReturnCriterion().calculate(barSeries, tradingRecord));

	}

	private void testObv(){
		List<OHLCV> ohlcvList = new ArrayList<>();
		ohlcvList.add(new OHLCV(76.0,91.2,76.0,91.2,525179173l));
		ohlcvList.add(new OHLCV(97.0,109.44,95.0,109.44,210295560l));
		ohlcvList.add(new OHLCV(120.98,129.4,105.21,108.17,365044065l));
		ohlcvList.add(new OHLCV(110.0,113.4,100.36,110.9,160539788l));
		ohlcvList.add(new OHLCV(121.0,133.08,117.0,133.08,338763140l));
		ohlcvList.add(new OHLCV(139.39,146.38,136.0,146.38,192359284l));
		ohlcvList.add(new OHLCV(153.8,157.4,132.65,137.79,309136273l));
		ohlcvList.add(new OHLCV(143.0,143.5,135.22,138.05,113015397l));
		ohlcvList.add(new OHLCV(139.0,139.0,126.05,131.32,107214397l));
		ohlcvList.add(new OHLCV(131.0,132.47,125.55,126.26,58941772l));
		ohlcvList.add(new OHLCV(123.43,132.7,118.6,125.3,162419382l));
		ohlcvList.add(new OHLCV(126.0,131.0,125.75,127.53,62973982l));
		ohlcvList.add(new OHLCV(128.7,129.19,125.0,125.69,36055940l));
		ohlcvList.add(new OHLCV(125.95,126.99,119.6,120.28,32236138l));
		ohlcvList.add(new OHLCV(121.3,122.97,117.11,117.93,39363549l));
		ohlcvList.add(new OHLCV(118.5,119.95,114.0,114.93,32100435l));
		ohlcvList.add(new OHLCV(114.0,119.3,112.66,113.49,34112725l));
		ohlcvList.add(new OHLCV(112.55,115.0,109.8,110.8,30257994l));
		ohlcvList.add(new OHLCV(111.36,119.17,110.33,115.16,88498741l));
		ohlcvList.add(new OHLCV(111.1,115.05,109.0,109.57,58560417l));
		ohlcvList.add(new OHLCV(110.0,116.2,103.5,114.58,93620725l));
		ohlcvList.add(new OHLCV(114.8,119.0,113.39,115.5,51113502l));
		ohlcvList.add(new OHLCV(115.49,116.5,109.86,113.02,44903854l));
		ohlcvList.add(new OHLCV(114.6,115.49,111.44,112.64,33770388l));
		ohlcvList.add(new OHLCV(112.7,114.25,111.0,111.58,18101301l));
		ohlcvList.add(new OHLCV(112.0,112.18,106.9,107.6,27173576l));
		ohlcvList.add(new OHLCV(112.95,118.36,110.26,117.96,163398289l));
		ohlcvList.add(new OHLCV(118.0,123.9,116.1,116.95,118683852l));
		ohlcvList.add(new OHLCV(116.7,117.49,109.5,111.67,61441099l));
		ohlcvList.add(new OHLCV(113.0,113.95,110.4,110.99,26659658l));
		ohlcvList.add(new OHLCV(112.0,112.4,107.5,108.15,30248266l));
		ohlcvList.add(new OHLCV(108.15,109.9,103.42,104.05,52943938l));
		ohlcvList.add(new OHLCV(105.37,107.5,101.4,103.02,54743574l));
		ohlcvList.add(new OHLCV(105.79,106.34,102.3,103.49,31139206l));
		ohlcvList.add(new OHLCV(104.6,104.87,101.0,102.18,36452821l));
		ohlcvList.add(new OHLCV(101.71,102.38,97.84,99.62,51879596l));
		ohlcvList.add(new OHLCV(100.0,103.49,99.15,102.62,61227119l));
		ohlcvList.add(new OHLCV(100.0,102.19,99.0,99.26,27994702l));
		ohlcvList.add(new OHLCV(100.0,100.0,95.39,99.05,43646598l));
		ohlcvList.add(new OHLCV(99.9,100.0,89.55,90.82,90107948l));
		ohlcvList.add(new OHLCV(86.0,96.78,86.0,95.41,113352923l));
		ohlcvList.add(new OHLCV(95.32,98.69,93.79,95.78,57074037l));
		ohlcvList.add(new OHLCV(94.0,95.17,90.0,90.79,49223674l));
		ohlcvList.add(new OHLCV(91.0,92.25,89.66,90.2,34321755l));
		ohlcvList.add(new OHLCV(90.0,90.5,87.15,87.46,36287243l));
		ohlcvList.add(new OHLCV(88.79,91.87,88.65,89.51,56875452l));
		ohlcvList.add(new OHLCV(90.0,90.39,88.62,89.17,20025845l));
		ohlcvList.add(new OHLCV(89.39,90.0,87.2,87.57,16578506l));
		ohlcvList.add(new OHLCV(85.75,88.0,85.02,86.95,25522239l));
		ohlcvList.add(new OHLCV(87.19,87.48,81.0,81.65,34971218l));
		ohlcvList.add(new OHLCV(83.98,85.29,80.5,81.08,41665695l));
		ohlcvList.add(new OHLCV(81.2,83.0,79.15,81.76,33119468l));
		ohlcvList.add(new OHLCV(81.81,82.1,79.77,80.0,17908438l));
		ohlcvList.add(new OHLCV(80.05,80.49,76.73,77.29,22420692l));
		ohlcvList.add(new OHLCV(77.99,80.5,77.12,77.59,31800879l));
		ohlcvList.add(new OHLCV(77.7,78.5,74.84,76.32,29301711l));
		ohlcvList.add(new OHLCV(76.32,80.67,75.08,79.83,47417740l));
		ohlcvList.add(new OHLCV(80.0,83.25,79.16,80.88,34098220l));
		ohlcvList.add(new OHLCV(82.39,83.65,82.01,82.84,7901472l));
		ohlcvList.add(new OHLCV(84.45,84.8,79.61,80.84,30671468l));
		ohlcvList.add(new OHLCV(80.04,80.29,73.84,74.18,89679787l));
		ohlcvList.add(new OHLCV(75.41,75.43,73.5,74.4,51882572l));
		ohlcvList.add(new OHLCV(75.0,78.25,74.07,74.56,55664096l));
		ohlcvList.add(new OHLCV(74.92,74.99,72.6,72.72,30134626l));
		ohlcvList.add(new OHLCV(72.72,77.6,70.55,74.33,98999523l));
		ohlcvList.add(new OHLCV(75.2,76.65,73.71,74.29,36932621l));
		ohlcvList.add(new OHLCV(73.8,74.19,70.55,70.95,29804499l));
		ohlcvList.add(new OHLCV(71.04,71.88,69.54,70.12,34166367l));
		ohlcvList.add(new OHLCV(70.0,70.9,68.1,68.93,38573962l));
		ohlcvList.add(new OHLCV(68.99,71.18,68.99,69.32,22429780l));
		ohlcvList.add(new OHLCV(69.5,69.7,66.86,67.21,29121532l));
		ohlcvList.add(new OHLCV(67.21,69.74,66.66,69.14,28730513l));
		ohlcvList.add(new OHLCV(70.55,72.3,69.3,69.58,21405109l));
		ohlcvList.add(new OHLCV(69.58,74.8,69.58,73.42,57391461l));
		ohlcvList.add(new OHLCV(77.7,88.1,75.26,88.1,244701933l));
		ohlcvList.add(new OHLCV(90.65,94.5,88.77,92.99,186599196l));
		ohlcvList.add(new OHLCV(92.15,92.49,86.7,87.41,76161884l));
		ohlcvList.add(new OHLCV(84.11,94.47,81.2,93.29,168055394l));
		ohlcvList.add(new OHLCV(94.09,101.78,92.51,98.54,236992751l));
		ohlcvList.add(new OHLCV(98.75,102.5,97.61,98.36,133838392l));
		ohlcvList.add(new OHLCV(98.73,100.2,95.48,98.52,76865512l));
		ohlcvList.add(new OHLCV(96.9,98.3,95.25,95.91,48472086l));
		ohlcvList.add(new OHLCV(96.4,97.0,91.5,92.25,40583292l));
		ohlcvList.add(new OHLCV(92.25,95.5,92.23,94.57,50853886l));
		ohlcvList.add(new OHLCV(94.89,98.13,93.01,96.12,66954410l));
		ohlcvList.add(new OHLCV(96.12,96.6,93.75,93.99,24825087l));
		ohlcvList.add(new OHLCV(93.5,94.88,93.0,93.81,29241541l));
		ohlcvList.add(new OHLCV(93.81,99.62,93.56,96.8,67357404l));
		ohlcvList.add(new OHLCV(97.3,100.4,96.91,97.43,47461145l));
		ohlcvList.add(new OHLCV(97.95,98.2,94.8,96.42,37709666l));
		ohlcvList.add(new OHLCV(94.0,96.39,93.15,95.12,26289361l));
		ohlcvList.add(new OHLCV(95.6,97.83,92.13,93.67,38323235l));
		ohlcvList.add(new OHLCV(93.97,95.24,92.1,92.69,30468393l));
		ohlcvList.add(new OHLCV(93.4,95.66,92.78,93.98,22492287l));
		ohlcvList.add(new OHLCV(97.0,99.95,93.1,93.55,69591245l));
		ohlcvList.add(new OHLCV(93.85,94.25,88.51,89.93,37601364l));
		ohlcvList.add(new OHLCV(87.1,88.08,84.5,85.07,37606547l));
		ohlcvList.add(new OHLCV(85.19,87.64,84.87,85.73,29578957l));
		ohlcvList.add(new OHLCV(86.4,86.67,85.25,86.22,16274841l));
		ohlcvList.add(new OHLCV(86.24,88.59,83.58,84.71,37564777l));
		ohlcvList.add(new OHLCV(84.7,85.11,82.26,82.71,30982641l));
		ohlcvList.add(new OHLCV(82.8,83.1,77.5,77.95,33396475l));
		ohlcvList.add(new OHLCV(78.9,81.18,78.55,79.16,30769069l));
		ohlcvList.add(new OHLCV(77.0,80.15,75.16,79.51,61605465l));
		ohlcvList.add(new OHLCV(79.4,79.6,75.27,75.66,24971633l));
		ohlcvList.add(new OHLCV(75.5,76.5,72.7,73.39,38606186l));
		ohlcvList.add(new OHLCV(71.0,72.83,70.0,70.18,32634298l));
		ohlcvList.add(new OHLCV(71.5,73.47,70.39,72.88,34095762l));
		ohlcvList.add(new OHLCV(73.9,75.6,72.1,73.25,28969720l));
		ohlcvList.add(new OHLCV(75.0,75.48,74.01,74.81,16453514l));
		ohlcvList.add(new OHLCV(74.5,75.18,73.91,74.41,13403086l));
		ohlcvList.add(new OHLCV(75.0,77.0,74.33,76.33,15293544l));
		ohlcvList.add(new OHLCV(77.14,78.78,76.01,76.24,27328395l));
		ohlcvList.add(new OHLCV(76.6,76.89,73.8,74.31,15524482l));
		ohlcvList.add(new OHLCV(74.79,74.79,73.17,73.49,11531639l));
		ohlcvList.add(new OHLCV(73.34,73.75,71.0,71.34,14299599l));
		ohlcvList.add(new OHLCV(70.1,70.44,67.5,68.08,22699796l));
		ohlcvList.add(new OHLCV(68.16,68.79,64.6,65.16,31301577l));
		ohlcvList.add(new OHLCV(65.82,68.31,65.24,65.98,26430739l));
		ohlcvList.add(new OHLCV(66.71,68.47,66.29,66.79,20575646l));
		ohlcvList.add(new OHLCV(67.85,76.53,67.62,74.8,116622091l));
		ohlcvList.add(new OHLCV(75.99,80.8,73.66,74.33,85425148l));
		ohlcvList.add(new OHLCV(74.51,76.47,72.37,73.08,45904119l));
		ohlcvList.add(new OHLCV(74.2,75.46,73.72,74.85,22868898l));
		ohlcvList.add(new OHLCV(76.98,77.0,73.51,74.6,23581873l));
		ohlcvList.add(new OHLCV(74.6,74.99,71.56,71.84,22284387l));
		ohlcvList.add(new OHLCV(72.11,72.3,68.3,69.97,36396886l));
		ohlcvList.add(new OHLCV(68.91,68.97,67.16,67.6,27920171l));
		ohlcvList.add(new OHLCV(67.9,67.9,64.36,65.26,28376783l));
		ohlcvList.add(new OHLCV(65.5,65.6,63.3,64.62,37541843l));
		ohlcvList.add(new OHLCV(65.0,66.12,63.81,64.28,24482682l));
		ohlcvList.add(new OHLCV(64.56,64.8,60.15,60.87,35125401l));
		ohlcvList.add(new OHLCV(60.95,62.11,59.29,61.29,36043235l));
		ohlcvList.add(new OHLCV(60.6,61.65,58.8,60.27,25503226l));
		ohlcvList.add(new OHLCV(59.4,61.6,58.58,60.55,27181781l));
		ohlcvList.add(new OHLCV(60.24,62.7,59.81,61.71,26475985l));
		ohlcvList.add(new OHLCV(61.7,63.1,60.56,60.9,19439120l));

		onBalanceVolumeCalculatorService.calculate(ohlcvList);
	}


	private void testTimeFrameSR(){
		Stock stock = stockService.getStockByNseSymbol("AUBANK");
		Trend trend = trendService.isDownTrend(stock);
		candleStickHelperService.isBullishConfirmed(StockPriceUtil.buildStockPricePreviousWeek(stock, miscUtil.previousWeekFirstDay()), Boolean.FALSE);
		candleStickHelperService.isBullishConfirmed(StockPriceUtil.buildStockPricePreviousMonth(stock, miscUtil.previousMonthFirstDay()), Boolean.FALSE);
		System.out.println("******MONTHYL******");
		monthlySupportResistanceService.isBullish(stock);
		System.out.println("******WEEKLY******");
		weeklySupportResistanceService.isBullish(stock);
		System.out.println("******SCAN******");
		boolean result = timeframeSupportResistanceService.isBullish(stock, trend);

		System.out.println("******SCAN******" + result);
	}


	private void scanCandleStickPattern(){

		System.out.println("******* Scanning Bullish *******");
		List<Stock> stockList = stockService.getActiveStocks();

		for(Stock stock: stockList) {

			if(stock.getSeries()!=null && stock.getSeries().equalsIgnoreCase("EQ")){
				if (fundamentalResearchService.isMcapInRange(stock)) {
					//candleStickExecutorService.executeBullish(stock);
					if(researchLedgerFundamentalService.isResearchActive(stock)){
						System.out.println("FUNDAMENTAL " + stock.getNseSymbol());
					}
					priceActionService.breakOut(stock);
					swingActionService.breakOut(stock);
					volumeActionService.breakOut(stock);
				}
			}
		}

		System.out.println("******* Scanning Bearish From Master *******");

		for(Stock stock: stockList){
			if(stock.getSeries()!=null && stock.getSeries().equalsIgnoreCase("EQ")) {
					if (fundamentalResearchService.isMcapInRange(stock)) {
						//candleStickExecutorService.executeBearish(stock);
						if(researchLedgerTechnicalService.isActive(stock, ResearchIO.ResearchTrigger.BUY)){
							System.out.println("RESEARCH " + stock.getNseSymbol());
						}
						if(portfolioService.isPortfolioStock(stock)){
							System.out.println("PORTFOLIO " + stock.getNseSymbol());
						}
						priceActionService.breakDown(stock);
						movingAverageActionService.breakDown(stock);
					}
			}
		}
	}


	private void processBhavFromApi(){

		List<Stock> stockList = stockRepository.findByActivityCompleted(false);

		int countTotal = stockList.size();

		for(Stock stock : stockList){
			long startTime = System.currentTimeMillis();
			System.out.println("Starting activity for " + stock.getNseSymbol());

			try {

				List<OHLCV> ohlcvList = mcService.getMCOHLP(stock.getNseSymbol(), 5, 700);

				if(!ohlcvList.isEmpty()){
					long count = priceTemplate.delete(stock.getNseSymbol());
					miscUtil.delay(25);
					System.out.println("Deleted existing bhav " + count + " "+ stock.getNseSymbol());
				}

				List<com.example.storage.model.StockPrice> stockPriceList = new ArrayList<>();
				com.example.storage.model.StockPrice stockPrice = null;

				for(OHLCV ohlcv :  ohlcvList) {

				 StockPriceIO stockPriceIO = new StockPriceIO("NSE",
						stock.getCompanyName(),
						stock.getNseSymbol(),
						"EQ",
						ohlcv.getOpen(),
						ohlcv.getHigh(),
						ohlcv.getLow(),
						ohlcv.getClose(),
						ohlcv.getClose(),
						ohlcv.getOpen(),
						ohlcv.getVolume(),
						0.00,
						 LocalDate.now().toString(),
						1,
						stock.getIsinCode());

				 stockPriceIO.setBhavDate(ohlcv.getBhavDate());
				 stockPriceIO.setTimestamp(ohlcv.getBhavDate().atZone(ZoneOffset.UTC).toLocalDate());
				 stockPriceIO.setLastRecordToProcess(Boolean.TRUE);
				 System.out.println(stockPriceIO);

				 stockPrice = new com.example.storage.model.StockPrice(stockPriceIO.getNseSymbol(),stockPriceIO.getBhavDate(), stockPriceIO.getOpen(), stockPriceIO.getHigh(),
						 stockPriceIO.getLow(), stockPriceIO.getClose(),  stockPriceIO.getTottrdqty());

				//queueService.send(stockPriceIO, QueueConstants.MTQueue.UPDATE_PRICE_TXN_QUEUE);
					stockPriceList.add(stockPrice);

						}
				priceTemplate.create(stockPriceList);

				stock.setActivityCompleted(true);

				stockRepository.save(stock);
				--countTotal;
				long endTime = System.currentTimeMillis();

				System.out.println("Completed activity for " + stock.getNseSymbol() + " took " + (endTime - startTime) +"ms");
				System.out.println("Remaining " + countTotal);
				miscUtil.delay(25);
				//miscUtil.delay(miscUtil.getInterval());

			}catch(Exception e){
				System.out.println("An error occured while getting data " + stock.getNseSymbol());
			}


		}
	}

	private void syncTechnicals(){
		List<Stock> stockList = stockRepository.findByActivityCompleted(false);

		int countTotal = stockList.size();

		for(Stock stock : stockList) {
			long startTime = System.currentTimeMillis();
			System.out.println("Starting activity for " + stock.getNseSymbol());

			try {

				com.example.storage.model.StockTechnicals stockTechnicals = technicalsTemplate.getForDate(stock.getNseSymbol(), LocalDate.now());
				if(stockTechnicals!=null){
					com.example.storage.model.StockPrice stockPrice = priceTemplate.getForDate(stock.getNseSymbol(), LocalDate.now());

					StockPriceIO stockPriceIO = new StockPriceIO("NSE",
							stock.getCompanyName(),
							stock.getNseSymbol(),
							"EQ",
							stockPrice.getOpen(),
							stockPrice.getHigh(),
							stockPrice.getLow(),
							stockPrice.getClose(),
							stockPrice.getClose(),
							stockPrice.getOpen(),
							stockPrice.getVolume(),
							0.00,
							LocalDate.now().toString(),
							1,
							stock.getIsinCode());

					stockPriceIO.setBhavDate(Instant.from(LocalDate.now()).atZone(ZoneOffset.UTC).toInstant());
					stockPriceIO.setTimestamp(LocalDate.now());
					stockPriceIO.setLastRecordToProcess(Boolean.TRUE);
					updateTechnicalsService.updateTechnicalsTxn(stockTechnicals, stockPriceIO);
					System.out.println("Updated Technicals  " + stock.getNseSymbol());
				}else{
					System.out.println("No Technicals  " + stock.getNseSymbol());
				}

				stock.setActivityCompleted(true);

				stockRepository.save(stock);

				System.out.println("Remaioning  " + --countTotal);

			}catch(Exception e){
				System.out.println("An error occured while getting data " + stock.getNseSymbol());
			}
		}
	}


	private void testDownLoad() {
		String referrerURI = fileNameService.getNSEBhavReferrerURI(LocalDate.now().minusDays(3));
		
		System.out.println(referrerURI);

		String fileURI = fileNameService.getNSEBhavDownloadURI(LocalDate.now().minusDays(3));
		
		System.out.println(fileURI);
		String fileName = "D:/cm13JAN2020bhav.zip";
		System.out.println(fileName);
		
		try {
			downloadBhavService.downloadFile(referrerURI, fileURI, fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void printJson(Object stockTechnicals) {

		// pretty print
		ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

		String json;
		try {
			json = mapper.writeValueAsString(stockTechnicals);
			System.out.println(json);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void restTemplate() {
		
		/*String quote = restTemplate.getForObject("http://localhost:8081/public/api/stocks/active/nifty500", String.class);
		
	      System.out.println(quote);*/
		 
		
		List<StockPriceIO> stocks = restClientService.getNift500();
		
		stocks.forEach(System.out::println);
		
	}
}
