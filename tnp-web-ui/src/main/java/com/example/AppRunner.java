package com.example;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.example.external.factor.FactorRediff;
import com.example.model.ledger.FundsLedger;
import com.example.model.ledger.TradeLedger;
import com.example.model.master.Sector;
import com.example.model.stocks.StockFactor;
import com.example.model.stocks.UserPortfolio;
import com.example.model.type.FundTransactionType;
import com.example.repo.ledger.FundsLedgerRepository;
import com.example.repo.ledger.TradeLedgerRepository;
import com.example.storage.model.StockPrice;
import com.example.storage.model.StockTechnicals;
import com.example.ui.model.UIRenderStock;
import com.example.ui.service.UiRenderUtil;
import com.example.util.io.model.StockIO;
import org.decampo.xirr.Transaction;
import org.decampo.xirr.Xirr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.external.bhav.service.DownloadBhavService;
import com.example.external.dylh.service.DylhService;
import com.example.integration.service.RestClientService;
import com.example.model.master.Stock;
import com.example.model.um.UserProfile;
import com.example.mq.producer.QueueService;
import com.example.repo.master.HolidayCalendarRepository;
import com.example.repo.stocks.PortfolioRepository;
import com.example.service.CalendarService;
import com.example.service.DividendLedgerService;
import com.example.service.ExpenseService;
import com.example.service.FileNameService;
import com.example.service.FundsLedgerService;
import com.example.service.PortfolioService;
import com.example.service.ResearchLedgerTechnicalService;
import com.example.service.SectorService;
import com.example.service.StockFactorService;
import com.example.service.StockService;
import com.example.service.TechnicalsResearchService;
import com.example.service.UserService;
import com.example.service.WatchListService;
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

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.YEARS;

@Component
public class AppRunner implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppRunner.class);

	@Autowired
	private RestClientService restClientService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private FileNameService fileNameService;
	
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

	@Override
	public void run(String... arg0) throws InterruptedException, IOException {

		Stock stock = stockService.getStockByNseSymbol("APLLTD");

		StockFactor prevStockFactor = stock.getStockFactor();

		if (prevStockFactor != null) {
			System.out.println(" PREV " + prevStockFactor.getQuarterEnded());
		}

		StockFactor newStockFactor = stockService.updateFactor(stock);

		if (newStockFactor != null) {
			System.out.println(" NEW " + newStockFactor.getQuarterEnded());
		}

		UserProfile userProfile = new UserProfile();
		userProfile.setUserId(1);

		List<TradeLedger> tradeLedgers = tradeLedgerRepository.getCashFlows(userProfile, 195l);

		tradeLedgers.forEach( tl -> {
			System.out.println(" Date "+  tl.getTransactionDate() +  "Amount " + tl.getPrice());
				}
		);


		StockTechnicals stockPrice = technicalsTemplate.getForDate("HAVELLS",LocalDate.now().minusDays(1));

		if(stockPrice!=null){
			System.out.println(stockPrice.getBhavDate() + " : " + stockPrice.getTrend().getMovingAverage().getSimple().getAvg5());
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
