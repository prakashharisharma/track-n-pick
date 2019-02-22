package com.example.mt.service.async;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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

import com.example.model.ledger.ResearchLedger;
import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;
import com.example.model.um.UserProfile;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.service.PortfolioService;
import com.example.service.PrettyPrintService;
import com.example.service.ResearchLedgerService;
import com.example.service.UserService;
import com.example.util.MiscUtil;
import com.example.util.io.model.EmailIO;
import com.example.util.io.model.NotificationTriggerIO;
import com.example.util.io.model.ResearchType;

@Component
public class NotificationTriggerConsumer {
	@Autowired
	private ResearchLedgerService researchLedgerService;

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
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NotificationTriggerConsumer.class);
	
	@JmsListener(destination = QueueConstants.MTQueue.NOTIFICATION_SEND_MAIL_TRIGGER)
	public void receiveMessage(@Payload NotificationTriggerIO notificationTriggerIO, @Headers MessageHeaders headers,
			Message message, Session session) throws InterruptedException {

		System.out.println("NOTIFICATION " + notificationTriggerIO);

		
		if(notificationTriggerIO.getTriggerType() == NotificationTriggerIO.TriggerType.RESEARCH ) {
			this.processResearchNotification();
		}else if(notificationTriggerIO.getTriggerType() == NotificationTriggerIO.TriggerType.PORTFOLIO) {
			this.processPortfolioNotification();
		}
		

	}

	private void processPortfolioNotification() throws InterruptedException {

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
	
	private void processResearchNotification() throws InterruptedException {
		List<ResearchLedger> buyResearchLedgerList = researchLedgerService.buyNotificationPending();

		List<ResearchLedger> sellResearchLedgerList = researchLedgerService.sellNotificationPending();

		List<UserProfile> allActiveUsers = userService.activeUsers();

		String emailSubject = "Stocks Research Report - " + LocalDate.now() + "!";

		String formatedbuyList = null;

		System.out.println("TRIGGEr 1");
		
		if (buyResearchLedgerList != null && !buyResearchLedgerList.isEmpty()) {

			List<Stock> buyFundamentals = buyResearchLedgerList.stream()
					.filter(researchLedger -> researchLedger.getResearchType() == ResearchType.FUNDAMENTAL)
					.map(rl -> rl.getStock()).collect(Collectors.toList());


			List<Stock> buyTechnicals = buyResearchLedgerList.stream()
					.filter(researchLedger -> researchLedger.getResearchType() == ResearchType.TECHNICAL)
					.map(rl -> rl.getStock()).collect(Collectors.toList());

			formatedbuyList = prettyPrintService.formatBuyListHTML(buyFundamentals, buyTechnicals);
			
			System.out.println("TRIGGEr 11");
			buyFundamentals.forEach(stock -> {
				researchLedgerService.updateResearchNotifiedBuy(stock, ResearchType.FUNDAMENTAL);
			});
			System.out.println("TRIGGEr 12");
			buyTechnicals.forEach(stock -> {
				researchLedgerService.updateResearchNotifiedBuy(stock, ResearchType.TECHNICAL);
			});

			

		}

		String formatedsellList = null;
		System.out.println("TRIGGEr 2");
		if (sellResearchLedgerList!=null && !sellResearchLedgerList.isEmpty()) {
			
			
			List<Stock> sellFundamentals = sellResearchLedgerList.stream()
					.filter(researchLedger -> researchLedger.getResearchType() == ResearchType.FUNDAMENTAL)
					.map(rl -> rl.getStock()).collect(Collectors.toList());

			List<Stock> sellTechnicals = sellResearchLedgerList.stream()
					.filter(researchLedger -> researchLedger.getResearchType() == ResearchType.TECHNICAL)
					.map(rl -> rl.getStock()).collect(Collectors.toList());

			formatedsellList = prettyPrintService.formatSellListHTML(sellFundamentals, sellTechnicals);
			
			System.out.println("TRIGGEr 21");
			sellFundamentals.forEach(stock -> {
				researchLedgerService.updateResearchNotifiedSell(stock, ResearchType.FUNDAMENTAL);
			});
			System.out.println("TRIGGEr 22");
			sellTechnicals.forEach(stock -> {
				researchLedgerService.updateResearchNotifiedSell(stock, ResearchType.TECHNICAL);
			});

			

		}

		String disclaimer= prettyPrintService.getDisclaimer();
		System.out.println("TRIGGEr 3");
		String formatedResearchList = null;
		System.out.println("TRIGGEr 4");
		
		if (!buyResearchLedgerList.isEmpty() && !sellResearchLedgerList.isEmpty()) {
			System.out.println("TRIGGEr 41");
			formatedResearchList = formatedbuyList + formatedsellList+disclaimer;
		} else if (!buyResearchLedgerList.isEmpty()) {
			System.out.println("TRIGGEr 42");
			formatedResearchList = formatedbuyList +disclaimer;
		} else if (!sellResearchLedgerList.isEmpty()) {
			System.out.println("TRIGGEr 43");
			formatedResearchList = formatedsellList +disclaimer;
		}

		if (formatedResearchList !=null ) {
			System.out.println("TRIGGEr 5");
			for (UserProfile user : allActiveUsers) {
				System.out.println("TRIGGEr 6" + user);
				this.prepareWatchListReportAndSendMail(user, formatedResearchList, emailSubject);

				Thread.sleep(miscUtil.getInterval());
			}
		}else {
			System.out.println("NO RESEARCH");
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

		System.out.println("Emailing START" + user.getFirstName() + " : " + user.getUserEmail() + ": " + emailSubject);

		String emailBody = prettyPrintService.formatEmailBody(user.getFirstName(), formatedResearchList);

		EmailIO emailIO = new EmailIO(user.getUserEmail(), emailSubject, emailBody);

		queueService.send(emailIO, QueueConstants.ExternalQueue.SEND_EMAIL_QUEUE);

		System.out.println("Emailing END" + user.getFirstName() + " : " + user.getUserEmail());

	}
}
