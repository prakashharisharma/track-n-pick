package com.example.processors;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.ResearchLedger;
import com.example.model.master.Stock;
import com.example.model.um.User;
import com.example.service.ResearchLedgerService;
import com.example.service.UserService;
import com.example.service.WatchListService;
import com.example.util.EmailService;
import com.example.util.MiscUtil;
import com.example.util.PrettyPrintService;

@Service
public class EmailWatchListProcessor implements Processor {

	@Autowired
	private WatchListService watchListService;
	
	@Autowired
	private UserService userService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private PrettyPrintService prettyPrintService;
	
	@Autowired
	private MiscUtil miscUtil;
	
	@Autowired
	private ResearchLedgerService researchLedgerService;
	
	@Override
	public void process(Exchange arg0) throws Exception {

		System.out.println("EmailWatchListProcessor START");
		
		User user = userService.getUserById(1);

		List<Stock> watchList = watchListService.userNotificationWatchList(user);
		
		watchList.stream().forEach(s ->
		
		{
			
			if(!researchLedgerService.isActive(s)) {
				researchLedgerService.addStock(s);
			}
		}
		
		);
		
		
		List<Stock> filteredWatchList = watchList.stream().filter(s -> researchLedgerService.includeInMail(s)).collect(Collectors.toList());
		
		if(!filteredWatchList.isEmpty()) {
		
			String formatedWatchList = prettyPrintService.formatWatchListHTML(filteredWatchList);
	
			String emailSubject = "Stocks Research Report - " + LocalDate.now() + "!";
			
			List<User> allActiveUsers = userService.allUsers();
			
			for(User mailToUser : allActiveUsers) {
	
				prepareWatchListReportAndSendMail(mailToUser, formatedWatchList, emailSubject);
				
				Thread.sleep(miscUtil.getInterval());
				
			}
		}else {
			System.out.println("No Stock in Mailer List END");
		}
		
		System.out.println("EmailWatchListProcessor END");
		
	}

	private void prepareWatchListReportAndSendMail(User user, String formatedWatchList, String emailSubject) throws Exception {
		
		System.out.println("Emailing START" + user.getFirstName() +" : " + user.getUserEmail());
		
		String emailBody = prettyPrintService.formatEmailBody(user.getFirstName(), formatedWatchList);
	
		emailService.sendEmail(user.getUserEmail(), emailBody, emailSubject);
		
		System.out.println("Emailing END" + user.getFirstName() +" : " + user.getUserEmail());
		
	}

}
