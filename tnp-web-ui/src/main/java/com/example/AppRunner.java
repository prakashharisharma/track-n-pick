package com.example;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.model.ledger.ResearchLedger;
import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.model.um.User;
import com.example.repo.ResearchLedgerHistoryRepository;
import com.example.repo.ResearchLedgerRepository;
import com.example.service.PortfolioService;
import com.example.service.StockService;
import com.example.service.UserService;
import com.example.service.WatchListService;
import com.example.util.PrettyPrintService;
import com.example.util.Rules;

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
	private PrettyPrintService prettyPrintService;
	
	@Autowired
	private WatchListService watchListService;
	
	@Autowired
	private ResearchLedgerRepository rlp; 
	
	@Override
	public void run(String... arg0) throws InterruptedException {

		LOGGER.info("USERS");

		User userP = userService.getUserById(1);
		
		User userR = userService.getUserById(2);
		
		User userRK = userService.getUserById(3);

		User userRD = userService.getUserById(4);
		
		User userCA = userService.getUserById(5);
		
		System.out.println(userRK);
		
		System.out.println(userRD);
		
		System.out.println(userCA);
		
		/*Stock endind = stockService.getStockByNseSymbol("ENGINERSIN");
		
		Stock nmdc = stockService.getStockByNseSymbol("NMDC");
		
		Stock nbcc = stockService.getStockByNseSymbol("NBCC");
		
		Stock ntpc = stockService.getStockByNseSymbol("NTPC");
		
		Stock bhel = stockService.getStockByNseSymbol("BHEL");
		
		Stock ih = stockService.getStockByNseSymbol("INDHOTEL");
		
		portfolioService.addStock(userP, ntpc, 140.15, 20);*/
	
		
		LOGGER.info("PORTFOLOIO P");
		
		List<UserPortfolio> portfolio = portfolioService.userPortfolio(userP);

		String portFolioStr = prettyPrintService.printPortFolio(portfolio);
		
		System.out.println(portFolioStr);
	
		LOGGER.info("PORTFOLOIO R");
		
		portfolio = portfolioService.userPortfolio(userR);

		portFolioStr = prettyPrintService.printPortFolio(portfolio);
		
		System.out.println(portFolioStr);
		
		List<Stock> watchList = watchListService.userWatchList(userP);
		
		portFolioStr = prettyPrintService.printWatchList(watchList);
		
		LOGGER.info("WATCHLIST");
		
		System.out.println(portFolioStr);
		
		List<User> userList = userService.activeUsers();
		
		userList.forEach(System.out::println);
		
		List<ResearchLedger> rlpList = rlp.findAll();
		
		rlpList.forEach(System.out::println);
		
	}

}
