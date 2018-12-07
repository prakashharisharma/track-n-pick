package com.example.processors;

import java.time.LocalDate;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.master.Stock;
import com.example.model.um.User;
import com.example.service.ResearchLedgerService;
import com.example.service.UserService;
import com.example.util.EmailService;
import com.example.util.MiscUtil;
import com.example.util.PrettyPrintService;

@Service
public class EmailResearchProcessor implements Processor {
	
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

		System.out.println("EmailResearchListProcessor START");

		List<Stock> watchList = researchLedgerService.researchNnotificationStocks();
		
		if(!watchList.isEmpty()) {
		
			String formatedWatchList = prettyPrintService.formatWatchListHTML(watchList);
	
			String emailSubject = "Stocks Research Report - " + LocalDate.now() + "!";
			
			List<User> allActiveUsers = userService.allUsers();
			
			for(User mailToUser : allActiveUsers) {
	
				prepareWatchListReportAndSendMail(mailToUser, formatedWatchList, emailSubject);
				
				Thread.sleep(miscUtil.getInterval());
				
			}
			
		}else {
			System.out.println("No Stock in Mailer List END");
		}
		
		System.out.println("EmailResearchListProcessor END");
		
	}

	private void prepareWatchListReportAndSendMail(User user, String formatedWatchList, String emailSubject) throws Exception {
		
		System.out.println("Emailing START" + user.getFirstName() +" : " + user.getUserEmail());
		
		String emailBody = prettyPrintService.formatEmailBody(user.getFirstName(), formatedWatchList);
	
		emailService.sendEmail(user.getUserEmail(), emailBody, emailSubject);
		
		System.out.println("Emailing END" + user.getFirstName() +" : " + user.getUserEmail());
		
	}

}
