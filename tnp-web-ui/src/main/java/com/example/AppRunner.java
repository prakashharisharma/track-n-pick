package com.example;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.bhav.service.DownloadBhavService;
import com.example.model.ledger.ExpenseLedger;
import com.example.model.ledger.ResearchLedger;
import com.example.model.master.HolidayCalendar;
import com.example.model.master.Stock;
import com.example.model.master.TaxMaster;
import com.example.model.stocks.UserPortfolio;
import com.example.model.type.SectorWiseValue;
import com.example.model.um.UserProfile;
import com.example.repo.ledger.ResearchLedgerHistoryRepository;
import com.example.repo.ledger.ResearchLedgerRepository;
import com.example.repo.master.HolidayCalendarRepository;
import com.example.repo.master.TaxMasterRepository;
import com.example.repo.stocks.PortfolioRepository;
import com.example.service.BrokerageService;
import com.example.service.CalendarService;
import com.example.service.DividendLedgerService;
import com.example.service.ExpenseService;
import com.example.service.FileNameService;
import com.example.service.FundsLedgerService;
import com.example.service.PortfolioService;
import com.example.service.StockService;
import com.example.service.TaxMasterService;
import com.example.service.TempFileNameService;
import com.example.service.UserService;
import com.example.service.WatchListService;
import com.example.storage.model.StockPrice;
import com.example.storage.model.TradingSession;
import com.example.storage.repo.StorageTemplate;
import com.example.storage.service.StorageService;
import com.example.ta.service.TechnicalRatioService;
import com.example.ta.service.TechnicalRatioServiceImpl;
import com.example.util.MiscUtil;
import com.example.util.PrettyPrintService;
import com.example.util.rules.RulesFundamental;

@Component
public class AppRunner implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppRunner.class);

	@Autowired
	private UserService userService;

	@Autowired
	private PortfolioService portfolioService;

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
	private TechnicalRatioService technicalRatioService;

	@Autowired
	private WatchListService watchListService;

	@Autowired
	private StorageService storageService;

	@Autowired
	private StorageTemplate storageTemplate;

	@Autowired
	private DownloadBhavService downloadBhavService;

	@Autowired
	private TempFileNameService tempFileNameService;

	@Override
	public void run(String... arg0) throws InterruptedException, IOException {

		LOGGER.info("USERS");

		//setUpStoragePrice();

		
		LOGGER.info("RSI " + storageService.getRSI("BDL", 14));
		
		// stockService.updateNifty50PriceAndFactor();

		// LocalDate dt1= LocalDate.of(2018, Month.DECEMBER, 03);
		// System.out.println(dt1);
		/*
		 * dt1=dt1.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
		 * 
		 * System.out.println(dt1);
		 */

		// dt = calendarService.previousWorkingDay(dt1);
		// System.out.println(dt);
		/*
		 * LocalDate dt = LocalDate.now();
		 * 
		 * for (int i = 0; i < 200; i++) {
		 * 
		 * dt = calendarService.previousWorkingDay(dt); System.out.println(dt);
		 * 
		 * }
		 */

	}

	public void setUpStoragePrice() throws IOException, InterruptedException {

		LocalDate dt = LocalDate.of(2018, Month.DECEMBER, 31);

		List<LocalDate> dateList = new ArrayList<>();
		dateList.clear();

		for (int i = 0; i < 14; i++) {

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

}
