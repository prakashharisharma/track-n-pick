package com.example;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.external.bhav.service.DownloadBhavService;
import com.example.external.dylh.service.DylhService;
import com.example.repo.master.HolidayCalendarRepository;
import com.example.repo.stocks.PortfolioRepository;
import com.example.service.CalendarService;
import com.example.service.DividendLedgerService;
import com.example.service.ExpenseService;
import com.example.service.FundsLedgerService;
import com.example.service.PortfolioService;
import com.example.service.StockService;
import com.example.service.TechnicalsResearchService;
import com.example.service.TempFileNameService;
import com.example.service.UserService;
import com.example.service.WatchListService;
import com.example.storage.model.TradingSession;
import com.example.storage.repo.PriceTemplate;
import com.example.storage.repo.TechnicalsTemplate;
import com.example.storage.repo.TradingSessionTemplate;
import com.example.storage.service.StorageService;
import com.example.util.MiscUtil;

@Component
public class AppRunner implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppRunner.class);

	@Autowired
	private UserService userService;

	@Autowired
	private PortfolioService portfolioService;

	@Autowired
	private DylhService dylhService;

	@Autowired
	private StockService stockService;

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
	private TempFileNameService tempFileNameService;

	@Autowired
	private TechnicalsResearchService technicalsService;

	@Autowired
	private TechnicalsTemplate technicalsTemplate;

	@Autowired
	private PriceTemplate priceTemplate;

	@Override
	public void run(String... arg0) throws InterruptedException, IOException {

		// LOGGER.info("USERS" + technicalsTemplate.getPrevTotalGain("TEST2"));

		LOGGER.info("AVG" + technicalsTemplate.getPriorDaysSma50Average("TEST", 3));
		LOGGER.info("AVG" + technicalsTemplate.getrsiCountAbove(60.00, "TEST", 50));
		LOGGER.info("AVG1 " + technicalsTemplate.getPrevSessionSma50("JAGRAN"));
		LOGGER.info("AVG2 " + technicalsTemplate.getPrevSessionSma200("JAGRAN"));

		LOGGER.info("LOw " + storageService.getyearLow("ZEEL"));

		LOGGER.info("LOW1 " + priceTemplate.getyearLow("ZEEL"));

		LOGGER.info("HIgh " + storageService.getyearHigh("ZEEL"));

		LOGGER.info("HIgh " + priceTemplate.getyearHigh("ZEEL"));
		/*
		 * priceTemplate.getYearLowStocks();
		 * 
		 * System.out.println("HIGH");
		 * 
		 * priceTemplate.getYearHighStocks();
		 */
		/*
		 * dylhService.yearHighStocks().forEach(stk -> {
		 * 
		 * System.out.println(stk.getNseSymbol());
		 * 
		 * });
		 */
		// setUpStoragePrice();

		// setupTradinfSession();

		LOGGER.info("RSI " + storageService.getRSI("ZEEL", 14));

		LOGGER.info("RSI1 " + technicalsTemplate.getCurrentRSI("ZEEL"));

		LOGGER.info("RSI2 " + technicalsTemplate.getCurrentSmoothedRSI("ZEEL"));

		/*LOGGER.info("NIFTY50 ");
		
		stockService.getNifty50ActiveStocks().forEach(System.out::println);
		
		LOGGER.info("NIFTY200 ");
		
		stockService.getOnlyNifty200ActiveStocks().forEach(System.out::println);
		*/
		//LOGGER.info("NIFTY500 ");
		//stockService.getNifty500ActiveStocks().forEach(System.out::println);
		
		/*
		 * LOGGER.info("LOW " + storageService.getyearLow("ZEEL"));
		 * 
		 * LOGGER.info("HIGH " + storageService.getyearHigh("ZEEL"));
		 * 
		 * LOGGER.info("SMA200 " + storageService.getSMA("ZEEL", 200));
		 * 
		 * LOGGER.info("SMA50 " + storageService.getSMA("ZEEL", 50));
		 * 
		 * 
		 * LOGGER.info("CURRENT " + storageService.getCurrentPrice("ZEEL"));
		 * 
		 * LOGGER.info("CURRENT " + storageService.getCurrentPrice("ZEEL"));
		 */

		//technicalsService.technicalsResearch();
		//
		// stockService.updateNifty50PriceAndFactor();

		// stockService.updateNifty50Technicals();

		/*
		 * LocalDate date = LocalDate.of(2019, Month.FEBRUARY,1);
		 * 
		 * storageTemplate.createTS(new
		 * TradingSession(date.atStartOfDay().toInstant(ZoneOffset.UTC)));
		 * 
		 * downloadBhavService.downloadFile(tempFileNameService.getNSEBhavReferrerURI(
		 * date),tempFileNameService.getNSEBhavDownloadURI(date),
		 * tempFileNameService.getNSEBhavFileName(date));
		 */
	}

	public void setUpStoragePrice() throws IOException, InterruptedException {

		LocalDate dt = LocalDate.of(2018, Month.MAY, 2);

		List<LocalDate> dateList = new ArrayList<>();
		dateList.clear();

		for (int i = 0; i < 20; i++) {

			dt = calendarService.previousWorkingDay(dt);

			dateList.add(dt);
			System.out.println(dt);
		}

		for (LocalDate date : dateList) {
			downloadBhavService.downloadFile(tempFileNameService.getNSEBhavReferrerURI(date),
					tempFileNameService.getNSEBhavDownloadURI(date), tempFileNameService.getNSEBhavFileName(date));

			long delay = miscUtil.getInterval();
			System.out.println(delay);
			Thread.sleep(delay);
		}
		System.out.println(dt);
	}

	public void setupTradinfSession() {
		LocalDate dt = LocalDate.now();

		List<LocalDate> dateList = new ArrayList<>();
		dateList.clear();

		for (int i = 0; i < 200; i++) {

			dt = calendarService.previousWorkingDay(dt);

			dateList.add(dt);
			System.out.println(dt);

			storageTemplate.createTS(new TradingSession(dt.atStartOfDay().toInstant(ZoneOffset.UTC)));

		}

	}

}
