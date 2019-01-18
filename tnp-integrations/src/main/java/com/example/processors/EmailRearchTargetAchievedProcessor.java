package com.example.processors;

import java.time.LocalDate;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.ResearchLedger;
import com.example.model.um.UserProfile;
import com.example.service.ResearchLedgerService;
import com.example.service.UserService;
import com.example.util.EmailService;
import com.example.util.MiscUtil;
import com.example.util.PrettyPrintService;

@Service
public class EmailRearchTargetAchievedProcessor implements Processor{
	
	@Autowired
	private ResearchLedgerService researchLedgerService;
	
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
		System.out.println("CHECK RESEARCH LIST PROCESSOR START");
		
		List<ResearchLedger> researchListTargetAchived = researchLedgerService.updateDailyResearchListTargetAchived();
		
		if(!researchListTargetAchived.isEmpty()) {
			
			String formatedResearchList = prettyPrintService.formatResearchListTargetAchivedHTML(researchListTargetAchived);
			
			String emailSubject = "Stocks Research Report - " + LocalDate.now() + "!";
			
			List<UserProfile> allActiveUsers = userService.activeUsers();
			
			for(UserProfile mailToUser : allActiveUsers) {
				
				prepareResearchReportPerformanceAndSendMail(mailToUser, formatedResearchList, emailSubject);
				
				Thread.sleep(miscUtil.getInterval());
				
			}
			
		}else {
			System.out.println("NO STOCK IN TARGET ACHIEVED LIST");
		}
		System.out.println("CHECK RESEARCH LIST PROCESSOR END");
		
	}
	
	private void prepareResearchReportPerformanceAndSendMail(UserProfile user, String formatedWatchList, String emailSubject) throws Exception {
		
		System.out.println("Emailing START" + user.getFirstName() +" : " + user.getUserEmail());
		
		String emailBody = prettyPrintService.formatEmailBody(user.getFirstName(), formatedWatchList);
	
		emailService.sendEmail(user.getUserEmail(), emailBody, emailSubject);
		
		System.out.println("Emailing END" + user.getFirstName() +" : " + user.getUserEmail());
		
	}
}
