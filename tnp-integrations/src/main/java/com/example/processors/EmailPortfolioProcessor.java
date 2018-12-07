package com.example.processors;

import java.time.LocalDate;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.stocks.UserPortfolio;
import com.example.model.um.User;
import com.example.service.PortfolioService;
import com.example.service.UserService;
import com.example.util.EmailService;
import com.example.util.MiscUtil;
import com.example.util.PrettyPrintService;

@Service
public class EmailPortfolioProcessor implements Processor {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailPortfolioProcessor.class);
	
	@Autowired
	private PortfolioService portfolioService;

	@Autowired
	private UserService userService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private PrettyPrintService prettyPrintService;

	@Autowired
	private MiscUtil miscUtil;
	
	@Override
	public void process(Exchange arg0) throws Exception {

		System.out.println("EmailPortfolioResearchProcessor START");
		
		LOGGER.info("EmailPortfolioResearchProcessor START");

		List<User> userList = userService.activeUsers();

		for(User user: userList) {
			
			preparePortfolioReportAndSendMail(user);
			
			Thread.sleep(miscUtil.getInterval());

		}
		
		LOGGER.info("EmailPortfolioResearchProcessor END");

	}

	private void preparePortfolioReportAndSendMail(User user) throws Exception {
		
		LOGGER.info("prepareReportAndSendMail START" + user.getFirstName());
		
		List<UserPortfolio> portfolioList = portfolioService.userPortfolio(user);
		
		String formatedPortfolio = prettyPrintService.formatPortfolioHTML(portfolioList);
		
		List<UserPortfolio> bookProfitList = portfolioService.targetAchived(user);

		String formatedBookProfitList = prettyPrintService.formatBookProfitHTML(bookProfitList);

		List<UserPortfolio> considerAveragingList = portfolioService.considerAveraging(user);

		String formatedConsiderAveragingList = prettyPrintService.formatAveragingHTML(considerAveragingList);

		String emailSubject = "Portfolio Research Report - " + LocalDate.now() + "!";

		String emailBody = prettyPrintService.formatEmailBody(user.getFirstName(), formatedPortfolio,formatedBookProfitList,
				formatedConsiderAveragingList);

		emailService.sendEmail(user.getUserEmail(), emailBody, emailSubject);
		
		LOGGER.info("prepareReportAndSendMail END" + user.getFirstName());
	}

}
