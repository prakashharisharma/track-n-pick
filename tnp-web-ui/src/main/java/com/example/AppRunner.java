package com.example;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.external.factor.FactorRediff;
import com.example.model.ledger.ResearchLedgerTechnical;
import com.example.model.stocks.UserPortfolio;
import com.example.mq.constants.QueueConstants;
import com.example.repo.ledger.FundsLedgerRepository;
import com.example.repo.ledger.ResearchLedgerTechnicalRepository;
import com.example.repo.ledger.TradeLedgerRepository;
import com.example.repo.master.StockRepository;
import com.example.repo.stocks.StockTechnicalsRepository;
import com.example.service.*;
import com.example.service.calc.AverageDirectionalIndexCalculatorService;
import com.example.service.calc.ExponentialMovingAverageCalculatorService;
import com.example.service.calc.RelativeStrengthIndexCalculatorService;
import com.example.storage.model.*;
import com.example.ui.service.UiRenderUtil;
import com.example.util.io.model.MCResult;
import com.example.util.io.model.ResearchIO;
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
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class AppRunner implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppRunner.class);

	@Autowired
	private RestClientService restClientService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private FileNameService fileNameService;

	@Autowired
	private StockTechnicalsRepository stockTechnicalsRepository;

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
	private QueueService queueService;
	@Autowired
	private ResearchLedgerTechnicalService tecnicalLedger;
	
	@Autowired
	private TechnicalsResearchService technicalsResearchService;

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
	private RelativeStrengthIndexCalculatorService rsiService;

	@Autowired
	private ExponentialMovingAverageCalculatorService exponentialMovingAverageService;

	@Autowired
	private MovingAverageConvergenceDivergenceService movingAverageConvergenceDivergenceService;

	@Autowired
	private AverageDirectionalIndexCalculatorService averageDirectionalIndexService;

	@Autowired
	private StockRepository stockRepository;


	@Autowired
	private ResearchLedgerTechnicalRepository researchLedgerTechnicalRepository;

	@Autowired
	private ResearchLedgerTechnicalService researchLedgerTechnicalService;

	@Autowired
	private CandleStickExecutorService candleStickExecutorService;

	@Autowired
	private CandleStickService candleStickService;

	@Autowired
	private UpdateTechnicalsService updateTechnicalsService;

	@Override
	public void run(String... arg0) throws InterruptedException, IOException {

		log.info("Application started....");

		candleStickService.isBuyingWickPresent(1630.0, 1660.25, 1480.50, 1655.15, 100);
		candleStickService.isSellingWickPresent(1630.0, 1660.25, 1480.50, 1655.15, 100);

		candleStickService.isBuyingWickPresent(264.0, 273.80, 261.20, 262.20, 100);
		candleStickService.isSellingWickPresent(264.0, 273.80, 261.20, 262.20, 100);

		System.out.println("STARTED " + LocalDateTime.now());

		long start = System.currentTimeMillis();

		StockPriceIO stockPriceIO = new StockPriceIO();
		stockPriceIO.setNseSymbol("HAVELLS");
		stockPriceIO.setBhavDate(Instant.now());
		stockPriceIO.setOpen(123.0);
		stockPriceIO.setHigh(165.0);
		stockPriceIO.setLow(121.0);
		stockPriceIO.setClose(145.0);
		stockPriceIO.setYearHigh(999.0);
		stockPriceIO.setYearLow(99.0);

		//updateTechnicalsService.updateTechnicals(stockPriceIO);

		long end = System.currentTimeMillis();
		System.out.println("DONE " + (end-start));

		//this.scanCandleStickPattern();

		//List<Double> retracements =  formulaService.fibonacciRetracements(380, 489);

		//retracements.forEach(System.out::println);

		//retracements =  formulaService.fibonacciExtensions(380, 489);

		//retracements.forEach(System.out::println);

		//retracements =  formulaService.fibonacciExtensions(380, 489, 421);

		//retracements.forEach(System.out::println);

		//this.getMCOHLP("OLAELEC");
		//this.getMCOHLP("HAVELLS");
		//this.getMCOHLP("ARE%26M");
		//this.doActivity();

		List<StockPrice> stockPriceList =  priceTemplate.get("HAVELLS", 700);

		Collections.reverse(stockPriceList);

		for (StockPrice price : stockPriceList) {
			//System.out.println(price);
		}

		List<ResearchLedgerTechnical> researchLedgerTechnicals =  researchLedgerTechnicalRepository.getActiveResearch("ANUP", ResearchIO.ResearchTrigger.BUY);

		if(!researchLedgerTechnicals.isEmpty()){
			//System.out.println(researchLedgerTechnicals.get(researchLedgerTechnicals.size() -1 ));
		}

		/*
		System.out.println("Running Runner");
		List<Stock> stocks = stockService.getActiveStocks();

		System.out.println("Total stocks " + stocks.size());
		for(Stock stock : stocks){
			System.out.println("Factors updating "+ stock.getNseSymbol());
			StockFactor stockFactor = stock.getStockFactor();
			if(stock.getNseSymbol().equalsIgnoreCase("DIVISLAB")){
				continue;
			}

			if(stockFactor.getMarketCap() == 0.0 || stockFactor.getFaceValue() == 0.0) {
				try {
					stockService.updateFactor(stock);
				}catch(Exception e){
					LOGGER.error("An error occured {}", stock.getNseSymbol(), e );
				}
				System.out.println("Factors updated {}"+ stock.getNseSymbol());
			}else{
				System.out.println("Factors Already Up to date {}"+ stock.getNseSymbol());

			}

		}


		System.out.println("Completed Runner");
				*/

		/*
		  LOGGER.info("PREV50 " + technicalsTemplate.getPrevSessionSma50("ZEEL"));
		  LOGGER.info("PREV200 " + technicalsTemplate.getPrevSessionSma200("ZEEL"));
		  LOGGER.info("SMA50 " + storageService.getSMA("ZEEL", 50));
		  LOGGER.info("SMA200 " + storageService.getSMA("ZEEL", 200));
		  
		  LOGGER.info("LOw " + storageService.getyearLow("ZEEL"));
		  
		  LOGGER.info("LOW1 " + priceTemplate.getyearLow("ZEEL"));
		  
		  LOGGER.info("HIgh " + storageService.getyearHigh("ZEEL"));
		  
		  LOGGER.info("HIgh " + priceTemplate.getyearHigh("ZEEL"));
		  
		  LOGGER.info("RSI " + storageService.getRSI("ZEEL", 14));
		  
		  LOGGER.info("RSI1 " + technicalsTemplate.getCurrentRSI("ZEEL"));
		  
		  LOGGER.info("RSI2 " + technicalsTemplate.getCurrentSmoothedRSI("ZEEL"));
		  
		  LOGGER.info("PREV OBV " + technicalsTemplate.getOBV("ZEEL"));
		  
		  LOGGER.info("PREV OBV " + priceTemplate.getTotalTradedQuantity("ZEEL"));
		  
		  StockTechnicals prevStockTechnicals =
		  technicalsTemplate.getPrevTechnicals("ZEEL", 1);
		  System.out.println("BHAV_DATE : " + prevStockTechnicals.getBhavDate());
		  this.printJson(prevStockTechnicals);
		 */

		//List<Stock> sl = stockFactorService.stocksToUpdateFactor();
		
		//sl.forEach(System.out::println);
		
		//List<Stock> stocksList = stockService.activeStocks();

		
		//List<UserProfile> allActiveUsers = userService.subsribedCurrentUnderValueUsers();
		
		//allActiveUsers.forEach(System.out::println);
		
		//sectorService.updateSectorPEPB();
		
		//restTemplate();
		
		/*List<ResearchLedgerTechnical> buyResearchTechnicalLedgerList = tecnicalLedger.buyNotificationPending();
		
		buyResearchTechnicalLedgerList.forEach(System.out::println);
		*/
		// Buy Research Fundamental
		/*stocksList.forEach(stock -> {
			ResearchIO researchIO = new ResearchIO();

			researchIO.setNseSymbol(stock.getNseSymbol());
			researchIO.setResearchTrigger(ResearchTrigger.BUY);
			researchIO.setResearchType(ResearchType.FUNDAMENTAL);
			// this.processFundamental(researchIO);

			queueService.send(researchIO, QueueConstants.MTQueue.RESEARCH_QUEUE);
		});
*/
		// Buy Research Technical

		/*stocksList.forEach(stock -> {
			ResearchIO researchIO = new ResearchIO();
			researchIO.setNseSymbol(stock.getNseSymbol());
			researchIO.setResearchTrigger(ResearchTrigger.BUY);
			researchIO.setResearchType(ResearchType.TECHNICAL);
			// this.processTechnical(researchIO);

			queueService.send(researchIO, QueueConstants.MTQueue.RESEARCH_QUEUE);
		});*/

		/*
		 * double ema20 = formulaService.calculateRateOfChange(150, 100);
		 * 
		 * System.out.println(ema20);
		 */

		/*
		 * ema20 = formulaService.calculateEMA(22.15, 22.22, 50);
		 * 
		 * System.out.println(ema20);
		 * 
		 * ema20 = formulaService.calculateEMA(22.15, 22.22,100);
		 * 
		 * System.out.println(ema20);
		 * 
		 * ema20 = formulaService.calculateEMA(22.15, 22.22,200);
		 * 
		 * System.out.println(ema20);
		 */
		/*
		 * double K = formulaService.getEMAMultiplier(20); System.out.println(K);
		 * 
		 * K = formulaService.getEMAMultiplier(50); System.out.println(K);
		 */
		// StockTechnicals stockTechnicals =
		// technicalsTemplate.getPrevTechnicals("ZEEL");

		// System.out.println(stockTechnicals);

		// stockService.resetFactors();

		/*
		 * LOGGER.info("DAYS HIGH " + priceTemplate.getDaysHigh("TEST6", 14));
		 * 
		 * LOGGER.info("DAYS LOW " + priceTemplate.getDaysLow("TEST6", 14));
		 * 
		 * LOGGER.info("DATE HIGH " + priceTemplate.getHighFromDate("ZEEL",
		 * LocalDate.now().minusWeeks(52)));
		 * 
		 * LOGGER.info("DATE LOW " + priceTemplate.getLowFromDate("ZEEL",
		 * LocalDate.now().minusWeeks(52)));
		 * 
		 * LOGGER.info("AVG PRICe " + priceTemplate.getAveragePrice("ZEEL", 2));
		 * 
		 * HighLowResult result = priceTemplate.getHighLowByDate("ZEEL",
		 * LocalDate.now().minusWeeks(52));
		 * 
		 * System.out.println(result);
		 * 
		 * result = priceTemplate.getHighLowByDays("ZEEL", 2);
		 * 
		 * System.out.println(result);
		 * 
		 * double r = tradingSessionTemplate.getTotalGain("ZEEL", 14);
		 * 
		 * System.out.println(r);
		 * 
		 * r = priceTemplate.getTotalGain("ZEEL", 14);
		 * 
		 * System.out.println(r);
		 * 
		 * r = storageService.getAverageGain("ZEEL", 14);
		 * 
		 * System.out.println(r);
		 * 
		 * r = priceTemplate.getAverageGain("ZEEL", 14);
		 * 
		 * System.out.println(r);
		 * 
		 * 
		 * r = tradingSessionTemplate.getTotalLoss("ZEEL", 14);
		 * 
		 * System.out.println(r);
		 * 
		 * r = priceTemplate.getTotalLoss("ZEEL", 14);
		 * 
		 * System.out.println(r);
		 * 
		 * r = storageService.getAverageLoss("ZEEL", 14);
		 * 
		 * System.out.println(r);
		 * 
		 * r = priceTemplate.getAverageLoss("ZEEL", 14);
		 * 
		 * System.out.println(r);
		 */

		// LOGGER.info("AVG PRICe " +
		// technicalsTemplate.getAverageStochasticOscillatorK("ZEEL",2));

		// stockService.resetFactors();

		/*
		 * List<Stock> sm = stockService.getActiveStocks();
		 * 
		 * for (Stock s : sm) { stockService.updateFactor(s);
		 * 
		 * Thread.sleep(100);
		 * 
		 * }
		 */

		// sectorService.updateSectorPEPB();

		/*
		 * List<Stock> stkList = stockService.getActiveStocks();
		 * 
		 * stkList.forEach( s -> { stockService.updateFactor(s);
		 * 
		 * });
		 */

		
		 /* UserProfile user1 = userService.getUserById(1); 
		  UserProfile user2 = userService.getUserById(2);
		  
		  Stock stock = stockService.getStockByNseSymbol("RITES");
		  
		  portfolioService.addBonus(user1, stock, 4, 1);
		  portfolioService.addBonus(user2, stock, 4, 1);
		 */
		//testDownLoad();
		System.out.println("STARTED");

		//this.updateDi();

		//this.processBhavFromApi();

		//this.doActivity();

	}

	private void scanCandleStickPattern(){

		System.out.println("******* Scanning Bullish *******");
		List<Stock> stockList = stockService.getActiveStocks();


		for(Stock stock: stockList){
			System.out.println("******* Scanning ******* " + stock.getNseSymbol());
			candleStickExecutorService.executeBullish(stock);
		}

		System.out.println("******* Scanning Bearish From Research *******");
		List<ResearchLedgerTechnical> researchLedgerTechnicalList = researchLedgerTechnicalService.allActiveResearch();

		for(ResearchLedgerTechnical researchLedgerTechnical: researchLedgerTechnicalList){
			System.out.println("******* Scanning ******* " + researchLedgerTechnical.getStock().getNseSymbol());
			candleStickExecutorService.executeBearish(researchLedgerTechnical.getStock());
		}
		System.out.println("******* Scanning Bearish From Portfolio *******");
		List<UserPortfolio> portfolioList =  portfolioService.get();

		for(UserPortfolio userPortfolio: portfolioList){
			System.out.println("******* Scanning ******* " + userPortfolio.getStock().getNseSymbol());
			candleStickExecutorService.executeBearish(userPortfolio.getStock());
		}

	}

	private void updateDi(){
		List<Stock> stockList = stockService.activeStocks();

		stockList.forEach(stock -> {
			try{

				StockTechnicals stockTechnicals = technicalsTemplate.getPrevTechnicals(stock.getNseSymbol(), 2);

				System.out.println(stockTechnicals);

				if(stockTechnicals!=null) {
					AverageDirectionalIndex adx = stockTechnicals.getAdx();
					if(adx!=null){
						com.example.model.stocks.StockTechnicals stockTechnicalsTxn = stock.getTechnicals();

						if(stockTechnicalsTxn!=null){
							stockTechnicalsTxn.setPrevPlusDi(adx.getPlusDi());
							stockTechnicalsTxn.setPrevMinusDi(adx.getMinusDi());
							stockTechnicalsRepository.save(stockTechnicalsTxn);
							System.out.println("Updated DI " + stock.getNseSymbol());
						}
					}
				}

			}catch(Exception e){
				System.out.println("An error occured while updating DI " + stock.getNseSymbol());
			}
		});
	}

	private void processBhavFromApi(){

		List<Stock> stockList = stockRepository.findByActivityCompleted(false);

		stockList.forEach(stock -> {
			long startTime = System.currentTimeMillis();
			System.out.println("Starting activity for " + stock.getNseSymbol());

			try {

				OHLCV ohlcv = this.getMCOHLCV(stock.getNseSymbol());

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
						LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
						1,
						stock.getIsinCode());

				queueService.send(stockPriceIO, QueueConstants.MTQueue.UPDATE_PRICE_TXN_QUEUE);

				stock.setActivityCompleted(true);

				stockRepository.save(stock);

			}catch(Exception e){
				System.out.println("An error occured while getting data " + stock.getNseSymbol());
			}

			long endTime = System.currentTimeMillis();
			System.out.println("Completed activity for " + stock.getNseSymbol() + " took " + (endTime - startTime) +"ms");

			try {
				Thread.sleep(miscUtil.getInterval());
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

		});
	}

	private void doActivity(){
		List<String> symbolList = new ArrayList<>();
		symbolList.add("MUFIN");
		symbolList.add("ARE&M");
		symbolList.add("M&M");
		symbolList.add("J&KBANK");
		symbolList.add("GMRP&UI");
		symbolList.add("M&MFIN");
		// symbolList.add("PSUBANK");
		symbolList.add("UNIVPHOTO");
		symbolList.add("AGIIL");

		//List<Stock> stockList = stockRepository.findByActivityCompleted(false);

		//stockList.forEach(stock -> {
			symbolList.forEach(symbol -> {
				Stock stock = stockService.getStockByNseSymbol(symbol);

			long startTime = System.currentTimeMillis();


			try {

				System.out.println("Starting activity for " + stock.getNseSymbol());
				List<OHLCV> ohlcvList = this.getMCOHLP(stock.getNseSymbol());
				technicalsTemplate.upsert(this.construct(stock.getNseSymbol(), ohlcvList));


				stock.setActivityCompleted(true);

				stockRepository.save(stock);

			}catch(Exception e){
				System.out.println("An error occured while getting data " + stock.getNseSymbol());
			}

			long endTime = System.currentTimeMillis();
			System.out.println("Completed activity for " + stock.getNseSymbol() + " took " + (endTime - startTime) +"ms");

			try {
				Thread.sleep(miscUtil.getInterval());
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

		});



	}

	private StockTechnicals construct(String nseSymbol, List<OHLCV> ohlcvList){

		OHLCV currentOHLCV = ohlcvList.get(ohlcvList.size() - 1);

		Volume volume = new Volume();

		volume.setVolume(currentOHLCV.getVolume());

		SimpleMovingAverage sma = this.constructSMA(nseSymbol, currentOHLCV);

		ExponentialMovingAverage ema = this.constructEMA(ohlcvList);

		int resultIndex= ohlcvList.size()-1;

		AverageDirectionalIndex adx = averageDirectionalIndexService.calculate(ohlcvList).get(resultIndex);

		RelativeStrengthIndex rsi = rsiService.calculate(ohlcvList).get(resultIndex);

		MovingAverageConvergenceDivergence macd = movingAverageConvergenceDivergenceService.calculate(ohlcvList).get(resultIndex);

		StockTechnicals stockTechnicals = new StockTechnicals(nseSymbol, currentOHLCV.getBhavDate(), volume,  sma, ema, adx, rsi, macd);

		return  stockTechnicals;

	}

	private SimpleMovingAverage constructSMA(String nseSymbol, OHLCV current){

		double close = 0.00;

		double sma5 = priceTemplate.getAveragePrice(nseSymbol, close, 5);

		double sma10 = priceTemplate.getAveragePrice(nseSymbol, close, 10);

		double sma20 = priceTemplate.getAveragePrice(nseSymbol, close, 20);

		double sma50 = priceTemplate.getAveragePrice(nseSymbol, close, 50);

		double sma100 = priceTemplate.getAveragePrice(nseSymbol, close, 100);

		double sma200 = priceTemplate.getAveragePrice(nseSymbol,close, 200);

		return new SimpleMovingAverage(sma5, sma10, sma20, sma50, sma100, sma200);
	}

	private ExponentialMovingAverage constructEMA(List<OHLCV> ohlcvList){
		int resultIndex= ohlcvList.size()-1;
		double ema5 = exponentialMovingAverageService.calculate(ohlcvList, 5).get(resultIndex);
		double ema10 = exponentialMovingAverageService.calculate(ohlcvList, 10).get(resultIndex);
		double ema20 = exponentialMovingAverageService.calculate(ohlcvList, 20).get(resultIndex);
		double ema50 = exponentialMovingAverageService.calculate(ohlcvList, 50).get(resultIndex);
		double ema100 = exponentialMovingAverageService.calculate(ohlcvList, 100).get(resultIndex);
		double ema200 = exponentialMovingAverageService.calculate(ohlcvList, 200).get(resultIndex);
	return new ExponentialMovingAverage(ema5, ema10, ema20, ema50, ema100, ema200);
	}
	private OHLCV getMCOHLCV(String nseSymbol)
	{
		long startTime = System.currentTimeMillis();

		if(nseSymbol.contains("&")){
			nseSymbol = nseSymbol.replace("&", "%26");
		}

		final String uri;

		try {
			uri = "https://priceapi.moneycontrol.com/techCharts/indianMarket/stock/history?symbol="+ URLEncoder.encode(nseSymbol, StandardCharsets.UTF_8.toString())+"&resolution=1D&from=1727782150&to=1727782150&countback=1&currencyCode=INR";
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

		System.out.println("mc url: " + uri);

		RestTemplate restTemplate = new RestTemplate();

		MCResult ohlc = restTemplate.getForObject(uri, MCResult.class);

		long endTime = System.currentTimeMillis();

		System.out.println("Time took to get MC data for " + nseSymbol +" is "+ (endTime - startTime) +"ms");

		List<OHLCV> ohlcvList = this.map(ohlc);

		if(ohlcvList !=null && ohlcvList.size()!=0){
			return ohlcvList.get(ohlcvList.size()-1);
		}

		return new OHLCV();

	}


	private List<OHLCV> getMCOHLP(String nseSymbol)
	{
		long startTime = System.currentTimeMillis();

		/*
		if(nseSymbol.contains("&")){
			nseSymbol = nseSymbol.replace("&", "%26");
		}
		 */

		final String uri = "https://priceapi.moneycontrol.com/techCharts/indianMarket/stock/history?symbol="+URLEncoder.encode(nseSymbol, StandardCharsets.UTF_8)+"&resolution=1D&from=1643241600&to=1727913600&countback=700&currencyCode=INR";

		System.out.println("mc url: " + uri);

		RestTemplate restTemplate = new RestTemplate();

		MCResult ohlc = restTemplate.getForObject(uri, MCResult.class);

		long endTime = System.currentTimeMillis();

		System.out.println("Time took to get MC data for " + nseSymbol +" is "+ (endTime - startTime) +"ms");

		List<OHLCV> ohlcvList = this.map(ohlc);

		return ohlcvList;

		/*
		List<RelativeStrengthIndex> relativeStrengthIndexList = rsiService.calculate(ohlcvList);

		relativeStrengthIndexList.forEach(r -> {
			System.out.println("RSI " + r);
		});

		System.out.println("TEST Single");

		RelativeStrengthIndex prevRelativeStrengthIndex = new RelativeStrengthIndex(1.7699406364986499, 1.961918811746544, 0.902147747348938, 47.43);

		List<OHLCV> ohlcvParam = new ArrayList<>();
		ohlcvParam.add(ohlcvList.get(ohlcvList.size()-2));
		ohlcvParam.add(ohlcvList.get(ohlcvList.size()-1));

		prevRelativeStrengthIndex = rsiService.calculate(ohlcvParam, prevRelativeStrengthIndex);

		System.out.println("RSI " + prevRelativeStrengthIndex);

		double ema = this.calculate(ohlcvList, 5);
		System.out.println( "5 EMA: " + ema);
		ema = exponentialMovingAverageService.calculate(ohlcvList, 5).get(ohlcvList.size()-1);
		System.out.println( "5 EMAn: " + ema);

		ema = this.calculate(ohlcvList, 10);
		System.out.println( "10 EMA: " + ema);

		ema = this.calculate(ohlcvList, 12);
		System.out.println( "12 EMA: " + ema);

		ema = this.calculate(ohlcvList, 20);
		System.out.println( "20 EMA: " + ema);

		ema = this.calculate(ohlcvList, 26);
		System.out.println( "26 EMA: " + ema);

		List<MovingAverageConvergenceDivergence> movingAverageConvergenceDivergenceList =  movingAverageConvergenceDivergenceService.calculate(ohlcvList);

		movingAverageConvergenceDivergenceList.forEach(m -> {
			System.out.println(m);
		});

		List<AverageDirectionalIndex> averageDirectionalIndexList =  averageDirectionalIndexService.calculate(ohlcvList);

		averageDirectionalIndexList.forEach(m -> {
			System.out.println(m);
		});

		AverageDirectionalIndex prevAverageDirectionalIndex =  new AverageDirectionalIndex(7.843693101984988, 2.132895110207555, 2.0239300105933364, 27.192485510018077, 25.80327894370503, 2.6213539527788625, 23.74);

		AverageDirectionalIndex averageDirectionalIndex = averageDirectionalIndexService.calculate(ohlcvParam, prevAverageDirectionalIndex);

		System.out.println("New " + averageDirectionalIndex);

		StockPrice sp = new StockPrice();
		sp.setNseSymbol("TEST");
		sp.setBhavDate(LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC));
		sp.setClose(123.90);

		priceTemplate.upsert(sp);

		long count = priceTemplate.count("HAVELLS");

		System.out.println(count);
		*/
	}

	private List<OHLCV> map(MCResult ohlc){

		long startTime = System.currentTimeMillis();

		List<OHLCV> ohlcvList = new ArrayList<>();

		List<Long>  dates = ohlc.getT();

		List<Double> opens = ohlc.getO();

		List<Double> highs = ohlc.getH();

		List<Double> lows = ohlc.getL();

		List<Double> closes = ohlc.getC();

		List<Long> volumes = ohlc.getV();

		for(int i=0; i < dates.size(); i++){

			OHLCV ohlcv = new OHLCV(Instant.ofEpochSecond(dates.get(i)), opens.get(i), highs.get(i), lows.get(i), closes.get(i), volumes.get(i));

			ohlcvList.add(ohlcv);

			System.out.println(ohlcv);
		}
		long endTime = System.currentTimeMillis();

		System.out.println("Time took to map OHLCV " + (endTime - startTime) + "ms");

		return ohlcvList;
	}

	private Double calculate(List<OHLCV> ohlcvList, int days){
		long startTime = System.currentTimeMillis();

		List<Double> emaList = new ArrayList<>(ohlcvList.size());

		for(int i=0; i < ohlcvList.size(); i++){

			if(i < days-1){
				emaList.add(i, 0.00);
			}else if(i == days-1){
				double sma = this.calculateSimpleAverage(ohlcvList, days);
				//System.out.println(sma);
				//System.out.println(miscUtil.formatDouble(sma,"0000"));
				//emaList.add(i, miscUtil.formatDouble(sma,"0000"));
				emaList.add(i, sma);
			}else{

				//System.out.println("i " + i );
				//System.out.println(ohlcvList.get(i).getClose() );
				//System.out.println(emaList.get(i-1) );
				double ema=  formulaService.calculateEMA(ohlcvList.get(i).getClose(), emaList.get(i-1), days);
				//System.out.println(ema);
				//System.out.println(miscUtil.formatDouble(ema,"0000"));
				emaList.add(i, miscUtil.formatDouble(ema,"0000"));
				emaList.add(i, ema);
			}

		}

		long endTime = System.currentTimeMillis();

		System.out.println("Time took to calculate EMA " + (endTime - startTime) + "ms");
		return emaList.get(ohlcvList.size()-1);
	}

	private double calculateSimpleAverage(List<OHLCV> ohlcvList, int days){

		double sum = 0.00;

		for(int i =0; i < days; i++){
			sum = sum + ohlcvList.get(i).getClose();
		}

		return sum / days;
	}

/*
	private RelativeStrengthIndex calculate(MCResult ohlc){

		long startTime = System.currentTimeMillis();

		List<Long> times = ohlc.getT();

		List<Long> volumes = ohlc.getV();

		List<Double> opens = ohlc.getO();

		List<Double> highs = ohlc.getH();

		List<Double> lows = ohlc.getL();

		List<Double> closes = ohlc.getC();

		List<Double> upMoves = new ArrayList<>(times.size());

		List<Double> downMoves = new ArrayList<>(times.size());

		List<Double> avgUp = new ArrayList<>(times.size());

		List<Double> avgDown = new ArrayList<>(times.size());
		List<Double> rsList = new ArrayList<>(times.size());
		List<Double> rsiList = new ArrayList<>(times.size());

		upMoves.add(0, 0.00);
		downMoves.add(0, 0.00);
		//double avgUp = 0.00;
		for(int i=0; i < times.size(); i++){

			Instant instant = Instant.ofEpochSecond(times.get(i));
			LocalDateTime localDateTime =
					LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));

			System.out.println(localDateTime+","+opens.get(i)+","+highs.get(i)+","+lows.get(i)+","+closes.get(i));
			avgUp.add(i, 0.00);
			avgDown.add(i, 0.00);
			rsList.add(i, 0.00);
			rsiList.add(i, 0.00);
			if(i>0) {

				double upMove = (closes.get(i) - closes.get(i-1) ) > 0 ? (closes.get(i) - closes.get(i-1) ) : 0;
				double downMove = (closes.get(i-1) - closes.get(i) ) > 0 ? (closes.get(i-1) - closes.get(i) ) : 0;

				System.out.println("Upmove: " +upMove +" DownMove: "+downMove);
				upMoves.add(i, upMove);
				downMoves.add(i, downMove);
				if(i==14){
					double sumU=0.00;
					double sumD=0.00;
					for(int j=1; j<=14; j++){

						//System.out.println(upMoves.get(j));
						sumU = sumU + upMoves.get(j);
						sumD = sumD + downMoves.get(j);
					}
					avgUp.add(i, sumU/14);
					avgDown.add(i, sumD/14);
					double rs = avgUp.get(i) / avgDown.get(i);
					double rsi = formulaService.calculateRsi(rs);
					rsList.add(i, rs);
					rsiList.add(i, rsi);

				}else{
					//double avgUpc = ((avgUp.get(i-1) * 13 ) + upMoves.get(i)) / 14;
					double avgUpc = formulaService.calculateSmoothedMovingAverage(avgUp.get(i-1), upMoves.get(i), 14);
					//double avgDownc = ((avgDown.get(i-1) * 13 ) + downMoves.get(i)) / 14;
					double avgDownc = formulaService.calculateSmoothedMovingAverage(avgDown.get(i-1), downMoves.get(i), 14);

					avgUp.add(i, avgUpc);
					avgDown.add(i, avgDownc);

					double rs = avgUp.get(i) / avgDown.get(i);
					double rsi = formulaService.calculateRsi(rs);
					rsList.add(i, rs);
					rsiList.add(i, rsi);
				}
				System.out.println("AvgUp: " +avgUp.get(i) +" AvgUp: "+avgDown.get(i)+" rs: "+ rsList.get(i)+" rsi: "+miscUtil.formatDouble(rsiList.get(i)));

			}

		}

		long endTime = System.currentTimeMillis();

		System.out.println("Time took to calculate RSI " + (endTime - startTime)+"ms");
		int resultIndex= times.size() -1;

		return new RelativeStrengthIndex(upMoves.get(resultIndex), downMoves.get(resultIndex), avgUp.get(resultIndex), avgDown.get(resultIndex),rsList.get(resultIndex), rsiList.get(resultIndex) );
	}

	private RelativeStrengthIndex calculate2(List<OHLCV> ohlcvList){

		long startTime = System.currentTimeMillis();

		List<Double> upMoves = new ArrayList<>(ohlcvList.size());
		List<Double> downMoves = new ArrayList<>(ohlcvList.size());
		List<Double> avgUp = new ArrayList<>(ohlcvList.size());
		List<Double> avgDown = new ArrayList<>(ohlcvList.size());
		List<Double> rsList = new ArrayList<>(ohlcvList.size());
		List<Double> rsiList = new ArrayList<>(ohlcvList.size());

		upMoves.add(0, 0.00);
		downMoves.add(0, 0.00);

		for(int i=0; i < ohlcvList.size(); i++){
			LocalDateTime localDateTime =
					LocalDateTime.ofInstant(ohlcvList.get(i).getBhavDate(), ZoneId.of("UTC"));

			avgUp.add(i, 0.00);
			avgDown.add(i, 0.00);
			rsList.add(i, 0.00);
			rsiList.add(i, 0.00);

			if(i>0) {

				double upMove = (ohlcvList.get(i).getClose() - ohlcvList.get(i-1).getClose() ) > 0 ? (ohlcvList.get(i).getClose() - ohlcvList.get(i-1).getClose() ) : 0;
				double downMove = (ohlcvList.get(i-1).getClose() - ohlcvList.get(i).getClose() ) > 0 ? (ohlcvList.get(i-1).getClose() - ohlcvList.get(i).getClose() ) : 0;

				upMoves.add(i, upMove);
				downMoves.add(i, downMove);

				if(i==14){

					double sumU=0.00;
					double sumD=0.00;
					for(int j=1; j<=14; j++){

						sumU = sumU + upMoves.get(j);
						sumD = sumD + downMoves.get(j);
					}
					avgUp.add(i, sumU/14);
					avgDown.add(i, sumD/14);
					double rs = avgUp.get(i) / avgDown.get(i);
					double rsi = formulaService.calculateRsi(rs);
					rsList.add(i, rs);
					rsiList.add(i, rsi);

				}else{

					double avgUpc = formulaService.calculateSmoothedMovingAverage(avgUp.get(i-1), upMoves.get(i), 14);

					double avgDownc = formulaService.calculateSmoothedMovingAverage(avgDown.get(i-1), downMoves.get(i), 14);

					avgUp.add(i, avgUpc);
					avgDown.add(i, avgDownc);

					double rs = avgUp.get(i) / avgDown.get(i);
					double rsi = formulaService.calculateRsi(rs);
					rsList.add(i, rs);
					rsiList.add(i, rsi);
				}
				//System.out.println("AvgUp: " +avgUp.get(i) +" AvgUp: "+avgDown.get(i)+" rs: "+ rsList.get(i)+" rsi: "+miscUtil.formatDouble(rsiList.get(i)));

			}

		}

		long endTime = System.currentTimeMillis();

		System.out.println("Time took to calculate RSI " + (endTime - startTime)+"ms");
		int resultIndex= ohlcvList.size() -1;

		return new RelativeStrengthIndex(upMoves.get(resultIndex), downMoves.get(resultIndex), avgUp.get(resultIndex), avgDown.get(resultIndex),rsList.get(resultIndex), rsiList.get(resultIndex) );
	}
	*/

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
