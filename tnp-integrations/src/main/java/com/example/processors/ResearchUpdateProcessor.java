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

// This processor will update research List
@Service
public class ResearchUpdateProcessor implements Processor{

	
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

		researchLedgerService.researchValueStocks();
		
		System.out.println("EmailResearchListProcessor END");
		
	}

}
