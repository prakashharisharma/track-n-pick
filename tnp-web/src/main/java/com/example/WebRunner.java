package com.example;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.data.common.type.Timeframe;
import com.example.data.common.type.Trend;
import com.example.dto.OHLCV;
import com.example.dto.TradeSetup;
import com.example.transactional.model.research.ResearchTechnical;
import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.model.stocks.StockTechnicals;
import com.example.transactional.model.um.Trade;
import com.example.processor.BhavProcessor;
import com.example.external.factor.FactorRediff;
import com.example.external.ta.service.McService;
import com.example.transactional.model.um.User;
import com.example.transactional.repo.ledger.FundsLedgerRepository;
import com.example.transactional.repo.master.StockRepository;
import com.example.transactional.repo.stocks.StockPriceRepositoryOld;
import com.example.transactional.repo.stocks.StockTechnicalsRepositoryOld;
import com.example.service.*;
import com.example.service.calc.*;
import com.example.service.impl.FundamentalResearchService;
import com.example.storage.model.assembler.StockPriceOHLCVAssembler;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.transactional.model.master.Stock;
import com.example.transactional.repo.master.HolidayCalendarRepository;
import com.example.storage.repo.PriceTemplate;
import com.example.storage.repo.TechnicalsTemplate;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import com.example.dto.io.StockPriceIO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


@Component
@Slf4j
public class WebRunner implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(WebRunner.class);

	@Autowired
	private BhavcopyService bhavcopyService;

	@Autowired
	private DailySupportResistanceService dailySupportResistanceService;

	@Autowired
	private OHLCVAggregatorService ohlcvAggregatorService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private ResearchLedgerFundamentalService researchLedgerFundamentalService;


	@Autowired
	private StockTechnicalsRepositoryOld stockTechnicalsRepository;

	@Autowired
	private StockPriceRepositoryOld stockPricesRepository;

	@Autowired
	private OhlcvService ohlcvService;


	@Autowired
	private StockService stockService;

	@Autowired
	private SectorService sectorService;



	@Autowired
	private FundsLedgerService fundsLedgerService;
	@Autowired
	private HolidayCalendarRepository holidayCalendarRepository;
	@Autowired
	private CalendarService calendarService;
	@Autowired
	private MiscUtil miscUtil;
	@Autowired
	private BhavProcessor bhavProcessor;

	@Autowired
	private UpdatePriceService updatePriceService;


	@Autowired
	private TechnicalsTemplate technicalsTemplate;

	@Autowired
	private PriceTemplate priceTemplate;


	@Autowired
	private FormulaService formulaService;

	@Autowired
	private StockPriceOHLCVAssembler stockPriceOHLCVAssembler;


	@Autowired
	private MovingAverageActionService movingAverageActionService;


	@Autowired
	private StockFactorService stockFactorService;

	@Autowired
	private FactorRediff factorRediff;

	@Autowired
	private FundsLedgerRepository fundsLedgerRepository;



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
	private QuarterlySupportResistanceService quarterlySupportResistanceService;

	@Autowired
	private MonthlySupportResistanceService monthlySupportResistanceService;

	@Autowired
	private WeeklySupportResistanceService weeklySupportResistanceService;

	@Autowired
	private StockRepository stockRepository;

	@Autowired
	private StockPriceServiceOld stockPriceServiceOld;

	@Autowired
	private StockPriceService<StockPrice> stockPriceService;

	@Autowired
	private StockTechnicalsService<StockTechnicals> stockTechnicalsService;

	@Autowired
	private BreakoutService breakoutService;

	@Autowired
	private ResearchTechnicalService<ResearchTechnical> researchTechnicalService;

	@Autowired
	private CandleStickConfirmationService candleStickHelperService;
	@Autowired
	private SwingActionService swingActionService;

	@Autowired
	private PriceActionService priceActionService;

	@Autowired
	private FundamentalResearchService fundamentalResearchService;
	@Autowired
	private CandleStickService candleStickService;

	@Autowired
	private UpdateTechnicalsService updateTechnicalsService;

	@Autowired
	private  PositionService positionService;

	@Autowired
	private TrendService trendService;

	@Autowired
	private YearlySupportResistanceService yearlySupportResistanceService;

	@Autowired
	private TimeframeSupportResistanceService timeframeSupportResistanceService;

	@Override
	public void run(String... arg0) throws InterruptedException, IOException {

		log.info("Application started....");

		//bhavProcessor.processAndResearchTechnicals();

		/*
		Stock stock = stockService.getStockByNseSymbol("360ONE");
		StockTechnicals stockTechnicals = updateTechnicalsService.build(Timeframe.MONTHLY, stock, LocalDate.of(2024,9,30));

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		ObjectWriter objectWriter = objectMapper.writer().withDefaultPrettyPrinter();
		System.out.println(objectWriter.writeValueAsString(stockTechnicals));
		 */

		//this.testCandleStick();
		//this.updateYearHighLow();
		//this.testObv();
		//this.testTimeFrameSR();
		//this.testTrend();
		//this.scanCandleStickPattern();
		//this.allocatePositions();

		//this.testScore();

		//this.updatePriceHistory();
		//this.updateTechnicals();
		//this.processBhavFromApi();
		//this.processPriceUpdate();
		//this.processTechnicalsUpdate();
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
		/*
		List<ResearchTechnical> researchTechnicalList = researchTechnicalService.getAll(Trade.Type.BUY);

		for(ResearchTechnical researchTechnical : researchTechnicalList){

			double score = researchLedgerTechnicalService.calculateScore(researchTechnical);

			System.out.println(" SYMBOL " + researchTechnical.getStock().getNseSymbol() + " SCORE " + score);
		}
		 */
	}


	/**
	 * Position Size = (Total trading fund * Risk%)/SL%
	 */
	private void allocatePositions(){


		double capital = 750000.0;
		double riskFactor = 1.0;

		List<User> portfolioList =  new ArrayList<>();

		User user = new User();
		//User user = userService.getUserByUsername("phsdhan");

		List<ResearchTechnical> researchTechnicalList = researchTechnicalService.getAll(Trade.Type.BUY);

		for(ResearchTechnical researchTechnical : researchTechnicalList){


			double allottedAmount = ((capital * riskFactor) / researchTechnical.getRisk());

			long positionSize = (long) (allottedAmount / researchTechnical.getResearchPrice());

			double reward = formulaService.calculateChangePercentage(researchTechnical.getResearchPrice(), researchTechnical.getTarget());


			double risk = formulaService.calculateFraction(capital, riskFactor);
			double stopLoss = (researchTechnical.getResearchPrice()  - researchTechnical.getStopLoss());
			positionSize = (long) (risk / stopLoss);
			positionService.calculate(user, researchTechnical);

		}

	}

	private void  testTrend(){

		List<Stock> stockList = stockService.getActiveStocks();
		/*
		List<Stock> stockList = new ArrayList<>();
		Stock stock = stockService.getStockByNseSymbol("ACC");
		stockList.add(stock);
		stock = stockService.getStockByNseSymbol("AUBANK");
		stockList.add(stock);
		stock = stockService.getStockByNseSymbol("AWL");
		stockList.add(stock);
		stock = stockService.getStockByNseSymbol("BALRAMCHIN");
		stockList.add(stock);
		stock = stockService.getStockByNseSymbol("MAXHEALTH");
		stockList.add(stock);
		stock = stockService.getStockByNseSymbol("JUBLFOOD");
		stockList.add(stock);
		 */

		stockList.forEach(stk ->{
			Trend trend = trendService.detect(stk, Timeframe.DAILY);
			if(trend.getDirection() != Trend.Direction.INVALID) {
				System.out.println("DAILY -> " + stk.getNseSymbol() + " Direction: " + trend.getDirection() + " Strength: " + trend.getStrength() + " Momentum: " + trend.getMomentum());
			}

			trend = trendService.detect(stk, Timeframe.WEEKLY);
			if(trend.getDirection() != Trend.Direction.INVALID) {
				System.out.println("WEEKLY -> " + stk.getNseSymbol() + " Direction: " + trend.getDirection() + " Strength: " + trend.getStrength() + " Momentum: " + trend.getMomentum());
			}

			trend = trendService.detect(stk, Timeframe.MONTHLY);
			if(trend.getDirection() != Trend.Direction.INVALID) {
				System.out.println("MONTHLY -> " + stk.getNseSymbol() + " Direction: " + trend.getDirection() + " Strength: " + trend.getStrength() + " Momentum: " + trend.getMomentum());
			}
		});
	}




	private void testCandleStick(){
		System.out.println("******* Testing CandleSticks *******");


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
					System.out.println("******* DAILY :" + stock.getNseSymbol()  +" *******");
					TradeSetup tradeSetup = priceActionService.breakOut(stock, Timeframe.DAILY);
					StockPrice stockPrice = stockPriceService.get(stock, Timeframe.DAILY);
					StockTechnicals stockTechnicals = stockTechnicalsService.get(stock, Timeframe.DAILY);


					tradeSetup = swingActionService.breakOut(stock, Timeframe.DAILY);


					if(calendarService.isLastTradingSessionOfWeek(miscUtil.currentDate())) {
						System.out.println("******* WEEKLY :" + stock.getNseSymbol() + " *******");
						 stockPrice = stockPriceService.get(stock, Timeframe.WEEKLY);
						 stockTechnicals = stockTechnicalsService.get(stock, Timeframe.WEEKLY);
						tradeSetup = priceActionService.breakOut(stock, Timeframe.WEEKLY);

						tradeSetup = swingActionService.breakOut(stock, Timeframe.WEEKLY);

					}
					if(calendarService.isLastTradingSessionOfMonth(miscUtil.currentDate())) {
						System.out.println("******* MONTHLY :" + stock.getNseSymbol() + " *******");
						priceActionService.breakOut(stock, Timeframe.MONTHLY);
						swingActionService.breakOut(stock, Timeframe.MONTHLY);

					}

				}
			}
		}

		System.out.println("******* Scanning Bearish From Master *******");

		for(Stock stock: stockList){
			if(stock.getSeries()!=null && stock.getSeries().equalsIgnoreCase("EQ")) {
					if (fundamentalResearchService.isMcapInRange(stock)) {
						/*
						if(researchLedgerTechnicalService.isActive(stock, ResearchIO.ResearchTrigger.BUY)){
							System.out.println("RESEARCH " + stock.getNseSymbol());
						}*/

						if(calendarService.isLastTradingSessionOfMonth(miscUtil.currentDate())) {
							System.out.println("******* MONTHLY :" + stock.getNseSymbol() + " *******");
							priceActionService.breakDown(stock, Timeframe.MONTHLY);
						}
						if(calendarService.isLastTradingSessionOfWeek(miscUtil.currentDate())) {
							System.out.println("******* WEEKLY :" + stock.getNseSymbol() + " *******");
							priceActionService.breakDown(stock, Timeframe.WEEKLY);
						}
						System.out.println("******* DAILY :" + stock.getNseSymbol()  +" *******");
						priceActionService.breakDown(stock, Timeframe.DAILY);
						//movingAverageActionService.breakDown(stock, Timeframe.DAILY);
					}
			}
		}
	}

	private void testLastTradingSession(){
		LocalDate tradingDate = LocalDate.of(2025,03,13);
		boolean special = calendarService.isLastTradingSessionOfQuarter(tradingDate);

		System.out.println(tradingDate +" is " + special);

		tradingDate = LocalDate.of(2025,01,31);
		special = calendarService.isLastTradingSessionOfQuarter(tradingDate);

		System.out.println(tradingDate +" is " + special);

		tradingDate = LocalDate.of(2025,02,28);
		special = calendarService.isLastTradingSessionOfQuarter(tradingDate);

		System.out.println(tradingDate +" is " + special);

		tradingDate = LocalDate.of(2025,03,31);
		special = calendarService.isLastTradingSessionOfQuarter(tradingDate);

		System.out.println(tradingDate +" is " + special);

		tradingDate = LocalDate.of(2025,03,28);
		special = calendarService.isLastTradingSessionOfQuarter(tradingDate);

		System.out.println(tradingDate +" is " + special);

		tradingDate = LocalDate.of(2025,06,30);
		special = calendarService.isLastTradingSessionOfQuarter(tradingDate);

		System.out.println(tradingDate +" is " + special);
	}

	private void updatePriceHistory(){

		List<Stock> stockList = stockRepository.findByActivityCompleted(false);

		int countTotal = stockList.size();

		for(Stock stock : stockList) {
			try {
			long startTime = System.currentTimeMillis();
			System.out.println("Starting activity for " + stock.getNseSymbol());

			System.out.println("Printing Daily Data");
			List<OHLCV> ohlcvListDaily = ohlcvService.fetch(stock.getNseSymbol(), LocalDate.of(2022, 01, 01), LocalDate.now());
			List<OHLCV> ohlcvList = ohlcvListDaily.subList(Math.max(ohlcvListDaily.size() - 5, 0), ohlcvListDaily.size());
			System.out.println("ohlcvListDaily Size " + ohlcvListDaily.size());
			System.out.println("ohlcvList Size " + ohlcvList.size());

			ohlcvList.forEach(ohlcv -> {
				System.out.println(ohlcv);
				StockPrice stockPrice = stockPriceService.createOrUpdate(stock, Timeframe.DAILY, ohlcv.getOpen(), ohlcv.getHigh(), ohlcv.getLow(), ohlcv.getClose(), LocalDate.ofInstant(ohlcv.getBhavDate(), ZoneOffset.UTC));
				System.out.println(stockPrice);
			});

			System.out.println("Printing Weekly Data");
			List<OHLCV> ohlcvListWeekly = ohlcvService.fetch(Timeframe.WEEKLY,stock.getNseSymbol(), LocalDate.of(2022, 01, 01), LocalDate.now());
			ohlcvList = ohlcvListWeekly.subList(Math.max(ohlcvListWeekly.size() - 5, 0), ohlcvListWeekly.size());
			System.out.println("ohlcvListWeekly Size " + ohlcvListWeekly.size());
			System.out.println("ohlcvList Size " + ohlcvList.size());

			ohlcvList.forEach(ohlcv -> {
				System.out.println(ohlcv);
				StockPrice stockPrice = stockPriceService.createOrUpdate(stock, Timeframe.WEEKLY, ohlcv.getOpen(), ohlcv.getHigh(), ohlcv.getLow(), ohlcv.getClose(), LocalDate.ofInstant(ohlcv.getBhavDate(), ZoneOffset.UTC));
				System.out.println(stockPrice);
			});

			System.out.println("Printing Monthly Data");
			List<OHLCV> ohlcvListMonthly = ohlcvService.fetch(Timeframe.MONTHLY,stock.getNseSymbol(), LocalDate.of(2022, 01, 01), LocalDate.now());
			ohlcvList = ohlcvListMonthly.subList(Math.max(ohlcvListMonthly.size() - 5, 0), ohlcvListMonthly.size());
			System.out.println("ohlcvListMonthly Size " + ohlcvListMonthly.size());
			System.out.println("ohlcvList Size " + ohlcvList.size());

			ohlcvList.forEach(ohlcv -> {
				System.out.println(ohlcv);
				StockPrice stockPrice = stockPriceService.createOrUpdate(stock, Timeframe.MONTHLY, ohlcv.getOpen(), ohlcv.getHigh(), ohlcv.getLow(), ohlcv.getClose(), LocalDate.ofInstant(ohlcv.getBhavDate(), ZoneOffset.UTC));
				System.out.println(stockPrice);
			});

			System.out.println("Printing Quarterly Data");
			List<OHLCV> ohlcvListQuarterly = ohlcvService.fetch(Timeframe.QUARTERLY,stock.getNseSymbol(), LocalDate.of(2022, 01, 01), LocalDate.now());
			ohlcvList = ohlcvListQuarterly.subList(Math.max(ohlcvListQuarterly.size() - 5, 0), ohlcvListQuarterly.size());
			System.out.println("ohlcvListQuarterly Size " + ohlcvListQuarterly.size());
			System.out.println("ohlcvList Size " + ohlcvList.size());


			ohlcvList.forEach(ohlcv -> {
				System.out.println(ohlcv);
				StockPrice stockPrice = stockPriceService.createOrUpdate(stock, Timeframe.QUARTERLY, ohlcv.getOpen(), ohlcv.getHigh(), ohlcv.getLow(), ohlcv.getClose(), LocalDate.ofInstant(ohlcv.getBhavDate(), ZoneOffset.UTC));
				System.out.println(stockPrice);
			});

			System.out.println("Printing Yearly Data");
			List<OHLCV> ohlcvListYearly = ohlcvService.fetch(Timeframe.YEARLY,stock.getNseSymbol(), LocalDate.of(2022, 01, 01), LocalDate.now());
			ohlcvList = ohlcvListYearly.subList(Math.max(ohlcvListYearly.size() - 5, 0), ohlcvListYearly.size());
			System.out.println("ohlcvListYearly Size " + ohlcvListYearly.size());
			System.out.println("ohlcvList Size " + ohlcvList.size());

			ohlcvList.forEach(ohlcv -> {
				System.out.println(ohlcv);
				StockPrice stockPrice = stockPriceService.createOrUpdate(stock, Timeframe.YEARLY, ohlcv.getOpen(), ohlcv.getHigh(), ohlcv.getLow(), ohlcv.getClose(), LocalDate.ofInstant(ohlcv.getBhavDate(), ZoneOffset.UTC));
				System.out.println(stockPrice);
			});

			stock.setActivityCompleted(true);

			stockRepository.save(stock);
			--countTotal;
			long endTime = System.currentTimeMillis();

			System.out.println("Completed activity for " + stock.getNseSymbol() + " took " + (endTime - startTime) + "ms");
			System.out.println("Remaining " + countTotal);

				miscUtil.delay(25);
			}catch (Exception e){
				System.out.println("An Error occurred while updating price");
			}
		}
	}

	private void updateTechnicals(){

		List<Stock> stockList = stockRepository.findByActivityCompleted(false);

		int countTotal = stockList.size();

		for(Stock stock : stockList) {
			try {
				long startTime = System.currentTimeMillis();
				System.out.println("Starting activity for " + stock.getNseSymbol());

				updateTechnicalsService.updateTechnicals(Timeframe.DAILY, stock, LocalDate.of(2025,03,11));
				updateTechnicalsService.updateTechnicals(Timeframe.DAILY, stock, LocalDate.of(2025,03,12));
				updateTechnicalsService.updateTechnicals(Timeframe.DAILY, stock, LocalDate.of(2025,03,13));

				updateTechnicalsService.updateTechnicals(Timeframe.WEEKLY, stock, LocalDate.of(2025,02,28));
				updateTechnicalsService.updateTechnicals(Timeframe.WEEKLY, stock, LocalDate.of(2025,03,07));
				updateTechnicalsService.updateTechnicals(Timeframe.WEEKLY, stock, LocalDate.of(2025,03,13));


				updateTechnicalsService.updateTechnicals(Timeframe.MONTHLY, stock, LocalDate.of(2024,12,31));
				updateTechnicalsService.updateTechnicals(Timeframe.MONTHLY, stock, LocalDate.of(2025,01,31));
				updateTechnicalsService.updateTechnicals(Timeframe.MONTHLY, stock, LocalDate.of(2025,02,28));

				stock.setActivityCompleted(true);

				stockRepository.save(stock);
				--countTotal;
				long endTime = System.currentTimeMillis();

				System.out.println("Completed activity for " + stock.getNseSymbol() + " took " + (endTime - startTime) + "ms");
				System.out.println("Remaining " + countTotal);

				miscUtil.delay(25);
			}catch (Exception e){
				System.out.println("An Error occurred while updating price");
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
				List<OHLCV> ohlcvList = mcService.getMCOHLP(stock.getNseSymbol(), 25, 6137);

				if (ohlcvList!=null && !ohlcvList.isEmpty()) {
					System.out.println("Deleting existing bhav " + stock.getNseSymbol());
					long count = priceTemplate.delete(stock.getNseSymbol());
					miscUtil.delay(25);
					System.out.println("Deleted existing bhav " + count + " " + stock.getNseSymbol());
				}

				List<com.example.storage.model.StockPrice> stockPriceList = new ArrayList<>();
				com.example.storage.model.StockPrice stockPrice = null;

				for(OHLCV ohlcv : ohlcvList){
					if (ohlcv != null && ohlcv.getOpen() != 0.0 && ohlcv.getClose() != 0.0) {

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
								ohlcv.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yy")),
								1,
								stock.getIsinCode());
						//System.out.println("Debug1 " + stock.getNseSymbol());
						stockPriceIO.setBhavDate(ohlcv.getBhavDate());
						//System.out.println("Debug2 " + stock.getNseSymbol());
						stockPriceIO.setTimestamp(ohlcv.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());
						//System.out.println("Debug3 " + stock.getNseSymbol());
						stockPrice = new com.example.storage.model.StockPrice(stockPriceIO.getNseSymbol(),stockPriceIO.getBhavDate(), stockPriceIO.getOpen(), stockPriceIO.getHigh(),
								stockPriceIO.getLow(), stockPriceIO.getClose(),  stockPriceIO.getTottrdqty());
						//System.out.println("Debug4 " + stock.getNseSymbol());
						stockPriceList.add(stockPrice);

					}
				}
				System.out.println("Debug5 " + stock.getNseSymbol());
				priceTemplate.create(stockPriceList);
				System.out.println("Debug6 " + stock.getNseSymbol());
				stock.setActivityCompleted(true);
				System.out.println("Debug7 " + stock.getNseSymbol());
				stockRepository.save(stock);
				System.out.println("Debug8 " + stock.getNseSymbol());
				--countTotal;
				long endTime = System.currentTimeMillis();

				System.out.println("Completed activity for " + stock.getNseSymbol() + " took " + (endTime - startTime) + "ms");
				System.out.println("Remaining " + countTotal);
				miscUtil.delay();
			}catch(Exception e){
				System.out.println("An error occured while getting data " + stock.getNseSymbol());
			}

		}
	}

	public void processPriceUpdate() {
		List<Stock> stockList = stockRepository.findByActivityCompleted(false);
		int threadCount = Runtime.getRuntime().availableProcessors(); // Use CPU cores
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

		AtomicInteger countTotal = new AtomicInteger(stockList.size());

		for (Stock stock : stockList) {
			executorService.submit(() -> {
				try {
					processYearlyPriceUpdate(stock);
					processQuarterlyPriceUpdate(stock);
					processMonthlyPriceUpdate(stock);
					processWeeklyPriceUpdate(stock);
					processDailyPriceUpdate(stock);

					stock.setActivityCompleted(true);
					stockRepository.save(stock);

					System.out.println("Remaining: " + countTotal.decrementAndGet());
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}

		executorService.shutdown(); // No new tasks will be accepted

	}

	private void processYearlyPriceUpdate(Stock stock){

			long startTime = System.currentTimeMillis();
			System.out.println("Starting yearly price update for " + stock.getNseSymbol());

			try {
				LocalDate initialDate = LocalDate.of(2000, 07, 14);

				LocalDate from = miscUtil.yearFirstDay(initialDate);
				LocalDate to = miscUtil.yearLastDay(from);

				System.out.println("yearly from: " + from + " to: "+to);
				List<com.example.storage.model.StockPrice> stockPriceList = new ArrayList<>();
				com.example.storage.model.StockPrice stockPrice = null;
				do{
					System.out.println("yearly from: " + from + " to: "+to);

				OHLCV ohlcv = yearlySupportResistanceService.supportAndResistance(stock.getNseSymbol(), from, to);

				if (ohlcv != null && ohlcv.getOpen() != 0.0 && ohlcv.getClose() != 0.0) {
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
							ohlcv.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yy")),
							1,
							stock.getIsinCode());

					stockPriceIO.setBhavDate(ohlcv.getBhavDate());

					stockPriceIO.setTimestamp(ohlcv.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());
					stockPriceIO.setTimeFrame(Timeframe.YEARLY);

					stockPrice = new com.example.storage.model.StockPrice(stockPriceIO.getNseSymbol(),stockPriceIO.getBhavDate(), stockPriceIO.getOpen(), stockPriceIO.getHigh(),
							stockPriceIO.getLow(), stockPriceIO.getClose(),  stockPriceIO.getTottrdqty());

					updatePriceService.updatePrice(Timeframe.YEARLY, stock, stockPrice);

					stockPriceList.add(stockPrice);

				}

					from = to.plusDays(1);
					to = miscUtil.yearLastDay(from);

			}while(to.isBefore(LocalDate.now()));

				priceTemplate.create(Timeframe.YEARLY, stockPriceList);

				long endTime = System.currentTimeMillis();

				System.out.println("Completed activity for " + stock.getNseSymbol() + " took " + (endTime - startTime) + "ms");

				miscUtil.delay(25);
			}catch(Exception e){
				System.out.println("An error occured while getting data " + stock.getNseSymbol());
			}
	}

	private void processQuarterlyPriceUpdate(Stock stock){

			long startTime = System.currentTimeMillis();
			System.out.println("Starting quarterly activity for " + stock.getNseSymbol());

			try {

				LocalDate initialDate = LocalDate.of(2000, 07, 14);

				LocalDate from = miscUtil.quarterFirstDay(initialDate);
				LocalDate to = miscUtil.quarterLastDay(from);

				List<com.example.storage.model.StockPrice> stockPriceList = new ArrayList<>();
				com.example.storage.model.StockPrice stockPrice = null;
				do{

					System.out.println("quarterly from: " + from + " to: "+to);

					OHLCV ohlcv = quarterlySupportResistanceService.supportAndResistance(stock.getNseSymbol(), from, to);

					if (ohlcv != null && ohlcv.getOpen() != 0.0 && ohlcv.getClose() != 0.0) {
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
								ohlcv.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yy")),
								1,
								stock.getIsinCode());

						stockPriceIO.setBhavDate(ohlcv.getBhavDate());

						stockPriceIO.setTimestamp(ohlcv.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());
						stockPriceIO.setTimeFrame(Timeframe.QUARTERLY);

						stockPrice = new com.example.storage.model.StockPrice(stockPriceIO.getNseSymbol(),stockPriceIO.getBhavDate(), stockPriceIO.getOpen(), stockPriceIO.getHigh(),
								stockPriceIO.getLow(), stockPriceIO.getClose(),  stockPriceIO.getTottrdqty());

						updatePriceService.updatePrice(Timeframe.QUARTERLY, stock, stockPrice);
						stockPriceList.add(stockPrice);

					}

					from = to.plusDays(1);
					to = miscUtil.quarterLastDay(from);

				}while(to.isBefore(LocalDate.now()));

				priceTemplate.create(Timeframe.QUARTERLY, stockPriceList);

				long endTime = System.currentTimeMillis();

				System.out.println("Completed activity for " + stock.getNseSymbol() + " took " + (endTime - startTime) + "ms");

				miscUtil.delay(25);
			}catch(Exception e){
				System.out.println("An error occured while getting data " + stock.getNseSymbol());
			}
	}

	private void processMonthlyPriceUpdate(Stock stock){

			long startTime = System.currentTimeMillis();
			System.out.println("Starting monthly activity for " + stock.getNseSymbol());

			try {

				LocalDate initialDate = LocalDate.of(2000, 07, 14);

				LocalDate from = initialDate.with(TemporalAdjusters.firstDayOfMonth());
				LocalDate to = from.with(TemporalAdjusters.lastDayOfMonth());

				List<com.example.storage.model.StockPrice> stockPriceList = new ArrayList<>();
				com.example.storage.model.StockPrice stockPrice = null;
				do{
					System.out.println("monthly from: " + from + " to: "+to);

					OHLCV ohlcv = monthlySupportResistanceService.supportAndResistance(stock.getNseSymbol(), from, to);

					if (ohlcv != null && ohlcv.getOpen() != 0.0 && ohlcv.getClose() != 0.0) {
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
								ohlcv.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yy")),
								1,
								stock.getIsinCode());

						stockPriceIO.setBhavDate(ohlcv.getBhavDate());

						stockPriceIO.setTimestamp(ohlcv.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());
						stockPriceIO.setTimeFrame(Timeframe.MONTHLY);

						stockPrice = new com.example.storage.model.StockPrice(stockPriceIO.getNseSymbol(),stockPriceIO.getBhavDate(), stockPriceIO.getOpen(), stockPriceIO.getHigh(),
								stockPriceIO.getLow(), stockPriceIO.getClose(),  stockPriceIO.getTottrdqty());

						updatePriceService.updatePrice(Timeframe.MONTHLY, stock, stockPrice);
						stockPriceList.add(stockPrice);

					}

					from = to.plusDays(1);
					to = from.with(TemporalAdjusters.lastDayOfMonth());

				}while(to.isBefore(LocalDate.now()));

				priceTemplate.create(Timeframe.MONTHLY,stockPriceList);

				long endTime = System.currentTimeMillis();

				System.out.println("Completed activity for " + stock.getNseSymbol() + " took " + (endTime - startTime) + "ms");
				//System.out.println("Remaining " + countTotal);
				miscUtil.delay(25);
			}catch(Exception e){
				System.out.println("An error occured while getting data " + stock.getNseSymbol());
			}

	}

	private void processWeeklyPriceUpdate(Stock stock){

			long startTime = System.currentTimeMillis();
			System.out.println("Starting weekly activity for " + stock.getNseSymbol());

			try {

				LocalDate initialDate = LocalDate.of(2000, 07, 14);


				LocalDate from = initialDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
				LocalDate to = from.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

				List<com.example.storage.model.StockPrice> stockPriceList = new ArrayList<>();
				com.example.storage.model.StockPrice stockPrice = null;
				do{

					System.out.println("weekly from: " + from + " to: "+to);

					OHLCV ohlcv = weeklySupportResistanceService.supportAndResistance(stock.getNseSymbol(), from, to);

					if (ohlcv != null && ohlcv.getOpen() != 0.0 && ohlcv.getClose() != 0.0) {
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
								ohlcv.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yy")),
								1,
								stock.getIsinCode());

						stockPriceIO.setBhavDate(ohlcv.getBhavDate());

						stockPriceIO.setTimestamp(ohlcv.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());
						stockPriceIO.setTimeFrame(Timeframe.WEEKLY);

						stockPrice = new com.example.storage.model.StockPrice(stockPriceIO.getNseSymbol(),stockPriceIO.getBhavDate(), stockPriceIO.getOpen(), stockPriceIO.getHigh(),
								stockPriceIO.getLow(), stockPriceIO.getClose(),  stockPriceIO.getTottrdqty());

						updatePriceService.updatePrice(Timeframe.WEEKLY, stock, stockPrice);
						stockPriceList.add(stockPrice);

					}

					from = to.plusDays(1);
					to = from.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

				}while(to.isBefore(LocalDate.now()));

				priceTemplate.create(Timeframe.WEEKLY, stockPriceList);

				long endTime = System.currentTimeMillis();

				System.out.println("Completed activity for " + stock.getNseSymbol() + " took " + (endTime - startTime) + "ms");

				miscUtil.delay(25);
			}catch(Exception e){
				System.out.println("An error occured while getting data " + stock.getNseSymbol());
			}

	}

	private void processDailyPriceUpdate(Stock stock){

		long startTime = System.currentTimeMillis();
		System.out.println("Starting daily activity for " + stock.getNseSymbol());

		try {

			LocalDate initialDate = LocalDate.of(2025, 03, 17);

			LocalDate from = initialDate;
			LocalDate to = initialDate;

			List<com.example.storage.model.StockPrice> stockPriceList = new ArrayList<>();
			com.example.storage.model.StockPrice stockPrice = null;
			do{

				System.out.println("daily from: " + from + " to: "+to);

				OHLCV ohlcv = dailySupportResistanceService.supportAndResistance(stock.getNseSymbol(), from, to);

				if (ohlcv != null && ohlcv.getOpen() != 0.0 && ohlcv.getClose() != 0.0) {
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
							ohlcv.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yy")),
							1,
							stock.getIsinCode());

					stockPriceIO.setBhavDate(ohlcv.getBhavDate());

					stockPriceIO.setTimestamp(ohlcv.getBhavDate().atOffset(ZoneOffset.UTC).toLocalDate());
					stockPriceIO.setTimeFrame(Timeframe.DAILY);

					stockPrice = new com.example.storage.model.StockPrice(stockPriceIO.getNseSymbol(),stockPriceIO.getBhavDate(), stockPriceIO.getOpen(), stockPriceIO.getHigh(),
							stockPriceIO.getLow(), stockPriceIO.getClose(),  stockPriceIO.getTottrdqty());

					updatePriceService.updatePrice(Timeframe.DAILY, stock, stockPrice);
					stockPriceList.add(stockPrice);

				}

				from = to.plusDays(1);
				to = from;

			}while(to.isBefore(LocalDate.now()));

			long endTime = System.currentTimeMillis();

			System.out.println("Completed activity for " + stock.getNseSymbol() + " took " + (endTime - startTime) + "ms");

			miscUtil.delay(25);
		}catch(Exception e){
			System.out.println("An error occured while getting data " + stock.getNseSymbol());
		}

	}

	public void processTechnicalsUpdate() {
		List<Stock> stockList = stockRepository.findByActivityCompleted(false);
		int threadCount = Runtime.getRuntime().availableProcessors(); // Use CPU cores
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

		AtomicInteger countTotal = new AtomicInteger(stockList.size());

		for (Stock stock : stockList) {
			executorService.submit(() -> {
				try {

					processMonthlyTechnicalsUpdate(stock);
					processWeeklyTechnicalsUpdate(stock);
					processDailyTechnicalsUpdate(stock);

					stock.setActivityCompleted(true);
					stockRepository.save(stock);

					System.out.println("Remaining: " + countTotal.decrementAndGet());
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}

		executorService.shutdown(); // No new tasks will be accepted

	}

	private void processMonthlyTechnicalsUpdate(Stock stock){

		long startTime = System.currentTimeMillis();
		System.out.println("Starting monthly activity for " + stock.getNseSymbol());

		try {

			LocalDate initialDate = LocalDate.of(2024, 9, 01);

			LocalDate from = initialDate.with(TemporalAdjusters.firstDayOfMonth());
			LocalDate to = from.with(TemporalAdjusters.lastDayOfMonth());

			com.example.storage.model.StockTechnicals stockTechnicals = null;
			do{
				System.out.println("monthly from: " + from + " to: "+to);


				stockTechnicals = updateTechnicalsService.build(Timeframe.MONTHLY, stock, to);

				updateTechnicalsService.updateTechnicals(Timeframe.MONTHLY, stock, stockTechnicals);

				from = to.plusDays(1);
				to = from.with(TemporalAdjusters.lastDayOfMonth());

			}while(to.isBefore(LocalDate.now()));

			long endTime = System.currentTimeMillis();

			System.out.println("Completed activity for " + stock.getNseSymbol() + " took " + (endTime - startTime) + "ms");
			//System.out.println("Remaining " + countTotal);
			miscUtil.delay(25);
		}catch(Exception e){
			System.out.println("An error occured while getting data " + stock.getNseSymbol());
		}

	}

	private void processWeeklyTechnicalsUpdate(Stock stock){

		long startTime = System.currentTimeMillis();
		System.out.println("Starting weekly activity for " + stock.getNseSymbol());

		try {

			LocalDate initialDate = LocalDate.of(2025, 02, 07);


			LocalDate from = initialDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
			LocalDate to = from.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

			com.example.storage.model.StockTechnicals stockTechnicals = null;
			do{

				System.out.println("weekly from: " + from + " to: "+to);

				stockTechnicals = updateTechnicalsService.build(Timeframe.WEEKLY, stock, to);

				updateTechnicalsService.updateTechnicals(Timeframe.WEEKLY, stock, stockTechnicals);

				from = to.plusDays(1);
				to = from.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

			}while(to.isBefore(LocalDate.now()));

			long endTime = System.currentTimeMillis();

			System.out.println("Completed activity for " + stock.getNseSymbol() + " took " + (endTime - startTime) + "ms");

			miscUtil.delay(25);
		}catch(Exception e){
			System.out.println("An error occured while getting data " + stock.getNseSymbol());
		}

	}

	private void processDailyTechnicalsUpdate(Stock stock){

		long startTime = System.currentTimeMillis();
		System.out.println("Starting daily activity for " + stock.getNseSymbol());

		try {

			LocalDate initialDate = LocalDate.of(2025, 03, 13);

			LocalDate from = initialDate;
			LocalDate to = initialDate;

			List<com.example.storage.model.StockPrice> stockPriceList = new ArrayList<>();
			com.example.storage.model.StockTechnicals stockTechnicals = null;
			do{

				System.out.println("daily from: " + from + " to: "+to);

				stockTechnicals = updateTechnicalsService.build(Timeframe.DAILY, stock, to);

				updateTechnicalsService.updateTechnicals(Timeframe.DAILY, stock, stockTechnicals);

				from = to.plusDays(1);
				to = from;

			}while(to.isBefore(LocalDate.now()));

			long endTime = System.currentTimeMillis();

			System.out.println("Completed activity for " + stock.getNseSymbol() + " took " + (endTime - startTime) + "ms");

			miscUtil.delay(25);
		}catch(Exception e){
			System.out.println("An error occured while getting data " + stock.getNseSymbol());
		}

	}

	private void syncTechnicals(){}




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
	

}
