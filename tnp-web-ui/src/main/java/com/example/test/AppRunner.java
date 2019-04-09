package com.example.test;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.external.bhav.service.DownloadBhavService;
import com.example.external.dylh.service.DylhService;
import com.example.model.master.Stock;
import com.example.model.um.UserProfile;
import com.example.repo.master.HolidayCalendarRepository;
import com.example.repo.stocks.PortfolioRepository;
import com.example.service.CalendarService;
import com.example.service.DividendLedgerService;
import com.example.service.ExpenseService;
import com.example.service.FundsLedgerService;
import com.example.service.PortfolioService;
import com.example.service.SectorService;
import com.example.service.StockService;
import com.example.service.TechnicalsResearchService;
import com.example.service.UserService;
import com.example.service.WatchListService;
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

	@Override
	public void run(String... arg0) throws InterruptedException, IOException {


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
		
		//sectorService.updateSectorPEPB();
		
		/*List<Stock> stkList = stockService.getActiveStocks();
		
		stkList.forEach( s -> {
			stockService.updateFactor(s);
			
		});*/

/*		UserProfile user1 = userService.getUserById(1);
		UserProfile user2 = userService.getUserById(2);
		
		Stock stock = stockService.getStockByNseSymbol("NTPC");
		
		portfolioService.addBonus(user1, stock, 5, 1);
		portfolioService.addBonus(user2, stock, 5, 1);*/
		
	}


}
