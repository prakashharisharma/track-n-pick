package com.example.service.async;



import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.jms.Session;

import com.example.service.impl.FundamentalResearchService;
import org.apache.activemq.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.example.model.ledger.ResearchLedgerFundamental;
import com.example.model.ledger.ResearchLedgerTechnical;
import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.model.um.UserProfile;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.service.PortfolioService;
import com.example.service.PrettyPrintService;
import com.example.service.ResearchLedgerFundamentalService;
import com.example.service.ResearchLedgerTechnicalService;
import com.example.service.UserService;
import com.example.util.MiscUtil;
import com.example.util.io.model.EmailIO;
import com.example.util.io.model.NotificationTriggerIO;

@Component
public class NotificationTriggerConsumer {
	@Autowired
	private ResearchLedgerFundamentalService fundamentalLedger;

	@Autowired
	private ResearchLedgerTechnicalService tecnicalLedger;

	@Autowired
	private UserService userService;

	@Autowired
	private PrettyPrintService prettyPrintService;

	@Autowired
	private MiscUtil miscUtil;

	@Autowired
	private QueueService queueService;

	@Autowired
	private PortfolioService portfolioService;
	
	@Autowired
	private FundamentalResearchService fundamentalResearchService;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NotificationTriggerConsumer.class);

	@JmsListener(destination = QueueConstants.MTQueue.NOTIFICATION_SEND_MAIL_TRIGGER)
	public void receiveMessage(@Payload NotificationTriggerIO notificationTriggerIO, @Headers MessageHeaders headers,
			Message message, Session session) throws InterruptedException {

		LOGGER.debug(QueueConstants.MTQueue.NOTIFICATION_SEND_MAIL_TRIGGER.toUpperCase() + " : " + notificationTriggerIO
				+ " : START");

		if (notificationTriggerIO.getTriggerType() == NotificationTriggerIO.TriggerType.RESEARCH) {
			//this.processResearchNotification();
		} else if (notificationTriggerIO.getTriggerType() == NotificationTriggerIO.TriggerType.PORTFOLIO) {
			this.processPortfolioNotification();
		} else if (notificationTriggerIO.getTriggerType() == NotificationTriggerIO.TriggerType.CURRENT_UNDERVALUE) {
			this.processResearchCurrentUndervalueNotification();
		}

		LOGGER.debug(QueueConstants.MTQueue.NOTIFICATION_SEND_MAIL_TRIGGER.toUpperCase() + " : " + notificationTriggerIO
				+ " : END");
	}

	
	
	private void processPortfolioNotification() throws InterruptedException {

		List<UserProfile> userList = userService.subsribedPortfolioUsers();

		for (UserProfile user : userList) {

			try {

				this.preparePortfolioReportAndSendMail(user);

			} catch (Exception e) {
				LOGGER.error("Error while preparing email " + user);
			}

			Thread.sleep(miscUtil.getInterval());

		}

	}
	 /*

	private void processResearchNotification() throws InterruptedException{

		List<ResearchLedgerFundamental> buyResearchLedgerList = fundamentalLedger.buyNotificationPending();

		List<ResearchLedgerFundamental> sellResearchLedgerList = fundamentalLedger.sellNotificationPending();

		List<ResearchLedgerTechnical> buyResearchTechnicalLedgerList = tecnicalLedger.buyNotificationPending();

		List<ResearchLedgerTechnical> sellResearchTechnicalLedgerList = tecnicalLedger.sellNotificationPending();

		List<UserProfile> allActiveUsers = userService.subsribedResearchUsers();

		String emailSubject = "Stocks Research Report - " + LocalDate.now() + "!";

		String formatedbuyList = null;
		System.out.println("1");

		List<Stock> buyFundamentals = new ArrayList<>();
		List<Stock> buyTechnicals = new ArrayList<>();
		boolean isBuy = false;
		boolean isSell = false;
		if (buyResearchLedgerList != null && !buyResearchLedgerList.isEmpty()) {

			buyFundamentals = buyResearchLedgerList.stream().map(rl -> rl.getStock()).collect(Collectors.toList());

			System.out.println("2");

			formatedbuyList = prettyPrintService.formatBuyListHTML(buyFundamentals, buyTechnicals);

			System.out.println("3");

			buyResearchLedgerList.forEach(srl -> {
				fundamentalLedger.updateResearchNotified(srl);
			});

			System.out.println("4");

			System.out.println("5");
			isBuy = true;
		}

		if (buyResearchTechnicalLedgerList != null && !buyResearchTechnicalLedgerList.isEmpty()) {


			System.out.println("2");
			buyTechnicals = buyResearchTechnicalLedgerList.stream().map(rl -> rl.getStock())
					.collect(Collectors.toList());

			System.out.println("3");


			System.out.println("4");


			System.out.println("5");
			isBuy = true;

		}

		formatedbuyList = prettyPrintService.formatBuyListHTML(buyFundamentals, buyTechnicals);

		String formatedsellList = null;

		List<Stock> sellFundamentals = new ArrayList<>();
		
		List<Stock> sellTechnicals = new ArrayList<>();
		
		if (sellResearchLedgerList != null && !sellResearchLedgerList.isEmpty()) {

			System.out.println("6");
			sellFundamentals = sellResearchLedgerList.stream()

					.map(rl -> rl.getStock()).collect(Collectors.toList());
			System.out.println("7");

			System.out.println("8");
	
			System.out.println("9");

			sellResearchLedgerList.forEach(srl -> {
				fundamentalLedger.updateResearchNotified(srl);
			});


			System.out.println("10");

			System.out.println("11");
			isSell = true;
		}

		if (sellResearchTechnicalLedgerList != null && !sellResearchTechnicalLedgerList.isEmpty()) {

			System.out.println("6");

			System.out.println("7");
			sellTechnicals = sellResearchTechnicalLedgerList.stream()

					.map(rl -> rl.getStock()).collect(Collectors.toList());
			System.out.println("8");

			System.out.println("9");



			System.out.println("10");

			System.out.println("11");
			isSell = true;
		}

		formatedsellList = prettyPrintService.formatSellListHTML(sellFundamentals, sellTechnicals);

		String disclaimer = prettyPrintService.getDisclaimer();

		String formatedResearchList = null;

		if (isBuy==true && isSell==true) {

			formatedResearchList = formatedbuyList + formatedsellList + disclaimer;
		} else if (isBuy==true) {

			formatedResearchList = formatedbuyList + disclaimer;
		} else if (isSell==true) {

			formatedResearchList = formatedsellList + disclaimer;
		}

		if (formatedResearchList != null) {
			for (UserProfile user : allActiveUsers) {
				this.prepareWatchListReportAndSendMail(user, formatedResearchList, emailSubject);

				Thread.sleep(miscUtil.getInterval());
			}
		} else {
			LOGGER.info("NO RESEARCH");
		}
	}*/

	private void processResearchCurrentUndervalueNotification() throws InterruptedException {

		List<ResearchLedgerFundamental> buyResearchLedgerList = fundamentalLedger.allActiveResearch();

		List<UserProfile> allActiveUsers = userService.subsribedCurrentUnderValueUsers();

		String emailSubject = "Current Undervalue Stocks - " + LocalDate.now() + "!";

		String formatedCurrentUnderValueList = null;
		System.out.println("1");

		List<Stock> currentReseachList = new ArrayList<>();
		List<Stock> currentUnderValue = new ArrayList<>();
		
		if (buyResearchLedgerList != null && !buyResearchLedgerList.isEmpty()) {

			currentReseachList = buyResearchLedgerList.stream().map(rl -> rl.getStock()).collect(Collectors.toList());

			currentReseachList.forEach( stk -> {
				if (fundamentalResearchService.isUndervalued(stk)) {
					currentUnderValue.add(stk);
				}
			});
			
			System.out.println("2");
			
			if (currentUnderValue != null && !currentUnderValue.isEmpty()) {
				
				formatedCurrentUnderValueList = prettyPrintService.formatCurrentUndervalueListHTML(currentUnderValue);
				
				String disclaimer = prettyPrintService.getDisclaimer();

				String formatedResearchList = null;
				
				formatedResearchList = formatedCurrentUnderValueList + disclaimer;
				
				if (formatedResearchList != null) {
					
					for (UserProfile user : allActiveUsers) {
						
						this.prepareWatchListReportAndSendMail(user, formatedResearchList, emailSubject);

						Thread.sleep(miscUtil.getInterval());
						
					}
				} 
			}

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

	private void prepareWatchListReportAndSendMail(UserProfile user, String formatedResearchList, String emailSubject) {

		LOGGER.info("Emailing START" + user.getFirstName() + " : " + user.getUserEmail() + ": " + emailSubject);

		String emailBody = prettyPrintService.formatEmailBody(user.getFirstName(), formatedResearchList);

		EmailIO emailIO = new EmailIO(user.getUserEmail(), emailSubject, emailBody);

		queueService.send(emailIO, QueueConstants.ExternalQueue.SEND_EMAIL_QUEUE);

		LOGGER.info("Emailing END" + user.getFirstName() + " : " + user.getUserEmail());

	}
}
