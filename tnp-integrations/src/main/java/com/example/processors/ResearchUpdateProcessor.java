package com.example.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.service.ResearchLedgerService;

// This processor will update research List
@Service
public class ResearchUpdateProcessor implements Processor{
	
	@Autowired
	private ResearchLedgerService researchLedgerService;
	
	@Override
	public void process(Exchange arg0) throws Exception {
		System.out.println("EmailResearchListProcessor START");

		researchLedgerService.researchValueStocks();
		
		System.out.println("EmailResearchListProcessor END");
		
	}

}
