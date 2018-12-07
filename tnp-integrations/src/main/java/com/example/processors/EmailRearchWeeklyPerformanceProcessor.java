package com.example.processors;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.ResearchLedger;
import com.example.service.ResearchLedgerService;

@Service
public class EmailRearchWeeklyPerformanceProcessor implements Processor {

	@Autowired
	private ResearchLedgerService researchLedgerService;
	
	@Override
	public void process(Exchange arg0) throws Exception {
		System.out.println("RESEARCH LIST CURRENT PERFORMACE START");
		
		List<ResearchLedger> researchList = researchLedgerService.activeResearchStocks();
		
		if(!researchList.isEmpty()) {
			researchList.stream().forEach((rl) -> {
				System.out.println("SYMBOL :" +rl.getStock().getNseSymbol());
				System.out.println("RESEARCH DATE :" +rl.getResearchDate());
				double rp = rl.getResearchPrice();
				
				System.out.println("RESEARCH PRICE :" +rl.getResearchPrice());
				
				double cp = rl.getStock().getStockPrice().getCurrentPrice();
				
				double diff = cp-rp;
				
				double per = (diff * 100)/cp;
				
				System.out.println("CURRENT :" + cp);
				
				System.out.println("PER :" + per);
				
				
				
				
			});
		}
		
		System.out.println("RESEARCH LIST CURRENT PERFORMACE END");

	}

}
