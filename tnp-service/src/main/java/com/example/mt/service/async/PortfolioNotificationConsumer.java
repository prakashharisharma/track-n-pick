package com.example.mt.service.async;

import java.time.LocalDate;
import java.util.List;

import javax.jms.Session;

import org.apache.activemq.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.example.model.stocks.UserPortfolio;
import com.example.model.um.UserProfile;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.service.PortfolioService;
import com.example.service.PrettyPrintService;
import com.example.service.UserService;
import com.example.util.MiscUtil;
import com.example.util.io.model.EmailIO;

//@Component
@Deprecated
public class PortfolioNotificationConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioNotificationConsumer.class);

	@Autowired
	private PortfolioService portfolioService;

	@Autowired
	private UserService userService;

	@Autowired
	private PrettyPrintService prettyPrintService;

	@Autowired
	private MiscUtil miscUtil;

	
	@Autowired
	private QueueService queueService;
	
	@JmsListener(destination = QueueConstants.MTQueue.NOTIFICATION_SEND_MAIL_TRIGGER)
	public void receiveMessage(@Payload String triggerEmail, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {

		List<UserProfile> userList = userService.activeUsers();

		for (UserProfile user : userList) {

			try {
				
				this.preparePortfolioReportAndSendMail(user);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Thread.sleep(miscUtil.getInterval());

		}

	}

	private void preparePortfolioReportAndSendMail(UserProfile user) throws Exception {

		LOGGER.info("prepareReportAndSendMail START" + user.getFirstName());

		List<UserPortfolio> portfolioList = portfolioService.userPortfolio(user);

		String formatedPortfolio = prettyPrintService.formatPortfolioHTML(portfolioList);

		List<UserPortfolio> bookProfitList = portfolioService.overValuedStocks(user);

		String formatedBookProfitList = prettyPrintService.formatOvervaluedStocksHTML(bookProfitList);

		List<UserPortfolio> considerAveragingList = portfolioService.underValuedStocks(user);

		String formatedConsiderAveragingList = prettyPrintService.formatUndervaluedStocksHTML(considerAveragingList);

		String emailSubject = "Portfolio Research Report - " + LocalDate.now() + "!";

		String emailBody = prettyPrintService.formatEmailBody(user.getFirstName(), formatedPortfolio,
				formatedBookProfitList, formatedConsiderAveragingList);

		EmailIO emailIO = new EmailIO(user.getUserEmail(), emailSubject, emailBody);
		
		queueService.send(emailIO, QueueConstants.ExternalQueue.SEND_EMAIL_QUEUE);
		
		LOGGER.info("prepareReportAndSendMail END" + user.getFirstName());
	}
}
