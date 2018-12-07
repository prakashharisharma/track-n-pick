package com.example.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.ResearchLedger;
import com.example.model.ledger.ResearchLedgerHistory;
import com.example.repo.ResearchLedgerHistoryRepository;

@Transactional
@Service
public class ResearchLedgerHistoryService {


	@Autowired
	private ResearchLedgerHistoryRepository researchLedgerHistoryRepository;
	
	public void addStock(ResearchLedger researchLedger) {
		
		ResearchLedgerHistory rlh = new ResearchLedgerHistory();
		
		rlh.setNseSymbol(researchLedger.getStock().getNseSymbol());
		rlh.setResearchDate(researchLedger.getResearchDate());
		rlh.setResearchPrice(researchLedger.getResearchPrice());
		rlh.setTargetDate(researchLedger.getTargetDate());
		rlh.setTargetPrice(researchLedger.getTargetPrice());
		
		researchLedgerHistoryRepository.save(rlh);
		
	}
}
