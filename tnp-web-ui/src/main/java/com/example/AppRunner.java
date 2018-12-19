package com.example;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.model.ledger.ResearchLedger;
import com.example.model.master.Stock;
import com.example.model.master.TaxMaster;
import com.example.model.stocks.UserPortfolio;
import com.example.model.um.User;
import com.example.repo.ledger.ResearchLedgerHistoryRepository;
import com.example.repo.ledger.ResearchLedgerRepository;
import com.example.repo.master.TaxMasterRepository;
import com.example.service.BrokerageService;
import com.example.service.DividendLedgerService;
import com.example.service.FundsLedgerService;
import com.example.service.PortfolioService;
import com.example.service.StockService;
import com.example.service.TaxMasterService;
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
	private DividendLedgerService dividendLedgerService;
	
	@Autowired
	private FundsLedgerService fundsLedgerService;
	
	@Override
	public void run(String... arg0) throws InterruptedException {

		LOGGER.info("USERS");

		User userP = userService.getUserById(1);
		
		User userR = userService.getUserById(2);

	//	fundsLedgerService.addFund(userR, 15000, LocalDate.now());
		//fundsLedgerService.withdrawFund(userR, 10000, LocalDate.now());
		
		/*Stock ioc = stockService.getStockByNseSymbol("IOC");
		
		dividendLedgerService.addDividend(userP, ioc, 6.75, LocalDate.now().plusDays(2), LocalDate.now().plusDays(6), LocalDate.now().plusDays(7));
		
		dividendLedgerService.addDividend(userR, ioc, 6.75, LocalDate.now().plusDays(2), LocalDate.now().plusDays(6), LocalDate.now().plusDays(7));
		*/
		/*Stock endind = stockService.getStockByNseSymbol("ENGINERSIN");
		
		Stock nmdc = stockService.getStockByNseSymbol("NMDC");
		
		Stock nbcc = stockService.getStockByNseSymbol("NBCC");
		
		Stock ntpc = stockService.getStockByNseSymbol("NTPC");
		
		Stock bhel = stockService.getStockByNseSymbol("BHEL");
		
		Stock ih = stockService.getStockByNseSymbol("INDHOTEL");
		
		portfolioService.addStock(userP, ntpc, 140.15, 20);*/
	
		/*
		LOGGER.info("PORTFOLOIO P");
		
		List<UserPortfolio> portfolio = portfolioService.userPortfolio(userP);

		String portFolioStr = prettyPrintService.printPortFolio(portfolio);
		
		System.out.println(portFolioStr);
	
		LOGGER.info("PORTFOLOIO R");
		
		portfolio = portfolioService.userPortfolio(userR);

		portFolioStr = prettyPrintService.printPortFolio(portfolio);
		
		System.out.println(portFolioStr);*/

		
	}

}
