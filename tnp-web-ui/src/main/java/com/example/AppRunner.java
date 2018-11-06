package com.example;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.model.um.User;
import com.example.service.PortfolioService;
import com.example.service.StockService;
import com.example.service.UserService;
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

	@Override
	public void run(String... arg0) throws InterruptedException {

		LOGGER.info("YEAR LOW STOCKS");

		User userP = userService.getUserById(1);
		
		User userR = userService.getUserById(2);
		
		LOGGER.info("PORTFOLOIO P");

		List<UserPortfolio> portfolio = portfolioService.userPortfolio(userP);

		portfolio.forEach(System.out::println);
	
		LOGGER.info("PORTFOLOIO R");
		
		portfolio = portfolioService.userPortfolio(userR);

		portfolio.forEach(System.out::println);
		
	}

}
