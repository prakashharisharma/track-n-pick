package com.example.processors;

import java.time.LocalDate;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.model.um.User;
import com.example.service.PortfolioService;
import com.example.service.UserService;
import com.example.service.WatchListService;
import com.example.util.EmailService;
import com.example.util.PrettyPrintService;

@Service
public class EmailWatchListProcessor implements Processor {

	@Autowired
	private WatchListService watchListService;

	@Autowired
	private PortfolioService portfolioService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private PrettyPrintService prettyPrintService;
	
	@Override
	public void process(Exchange arg0) throws Exception {

		System.out.println("EmailWatchListProcessor START");
		
		User user = userService.getUserById(1);

		List<Stock> watchList = watchListService.userWatchList(user);
		
		String formatedWatchList = prettyPrintService.formatWatchListHTML(watchList);
		
/*		List<Stock> considerAveragingList = portfolioService.considerAveraging(user);
		
		String formatedConsiderAveragingList = prettyPrintService.formatAveragingHTML(considerAveragingList);

		List<UserPortfolio> bookProfitList = portfolioService.targetAchived(user);
		
		String formatedBookProfitList = prettyPrintService.formatBookProfitHTML(bookProfitList);*/

		String emailSubject = "Stocks Research Report - " + LocalDate.now() + "!";

		String emailBody = prettyPrintService.formatEmailBody(user.getFirstName(), formatedWatchList);
		
		emailService.sendEmail(user.getUserEmail(), emailBody, emailSubject);

		System.out.println("EmailWatchListProcessor END");
		
	}



}
