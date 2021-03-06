package com.example.service;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.ResearchLedgerTechnical;
import com.example.model.ledger.CrossOverLedger;
import com.example.model.master.Stock;

import com.example.repo.ledger.ResearchLedgerTechnicalRepository;
import com.example.util.io.model.ResearchIO.ResearchTrigger;

@Transactional
@Service
public class ResearchLedgerTechnicalService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResearchLedgerTechnicalService.class);

	@Autowired
	private ResearchLedgerTechnicalRepository researchLedgerRepository;

	public void addResearch(Stock stock,CrossOverLedger entryCrossOver) {

		ResearchLedgerTechnical researchLedger = researchLedgerRepository.findByStockAndResearchStatus(stock, ResearchTrigger.BUY);

		if (researchLedger == null) {
			
			researchLedger = new ResearchLedgerTechnical();
			
			researchLedger.setStock(stock);
			
			researchLedger.setResearchStatus(ResearchTrigger.BUY);
			
			researchLedger.setNotified(false);
			
			//researchLedger.setNotifiedStorage(false);
			
			researchLedger.setEntryCrossOver(entryCrossOver);
			
			researchLedgerRepository.save(researchLedger);
		} else {
			LOGGER.debug(stock.getNseSymbol() + " is already in Ledger for " + stock.getNseSymbol());
		}

	}

	public void updateResearch(Stock stock, CrossOverLedger exitCrossOver) {

		ResearchLedgerTechnical researchLedger = researchLedgerRepository.findByStockAndResearchStatus(stock, ResearchTrigger.BUY);
		
		if (researchLedger != null) {

			
			researchLedger.setResearchStatus(ResearchTrigger.SELL);
			
			researchLedger.setNotified(false);
			//researchLedger.setNotifiedStorage(false);
			
			researchLedger.setExitCrossOver(exitCrossOver);
			
			researchLedgerRepository.save(researchLedger);	
		}
		
		
	}

	/*public boolean isResearchStorageNotified(Stock stock, ResearchTrigger researchTrigger) {
		boolean isResearchStorageNotified = true;
		ResearchLedgerTechnical researchLedger = researchLedgerRepository.findByStockAndResearchStatusAndNotifiedStorage(stock, researchTrigger, false);
		
		if(researchLedger != null) {
			
			isResearchStorageNotified = false;
		}
		
		return isResearchStorageNotified;
	}*/


	public void updateResearchNotified(ResearchLedgerTechnical researchLedger) {
		
		ResearchLedgerTechnical researchLedger1 = researchLedgerRepository.findBySrlId(researchLedger.getSrlId());
		
		if (researchLedger1 != null) {

			researchLedger1.setNotified(true);

			researchLedgerRepository.save(researchLedger1);
		}
		
	}
	
	
	public void updateResearchNotifiedStorage(ResearchLedgerTechnical researchLedger) {

		ResearchLedgerTechnical researchLedger1 = researchLedgerRepository.findBySrlId(researchLedger.getSrlId());
		
		if (researchLedger1 != null) {

			//researchLedger1.setNotifiedStorage(true);
			
			researchLedgerRepository.save(researchLedger1);
		}
		

	}

	public List<ResearchLedgerTechnical> allActiveResearch() {
		return researchLedgerRepository.findByResearchStatus(ResearchTrigger.BUY);
	}

	public List<ResearchLedgerTechnical> buyNotificationPending() {
		return researchLedgerRepository.findByResearchStatusAndNotified(ResearchTrigger.BUY, false);
	}

	public List<ResearchLedgerTechnical> sellNotificationPending() {
		return researchLedgerRepository.findByResearchStatusAndNotified(ResearchTrigger.SELL, false);
	}

	public List<ResearchLedgerTechnical> researchStocksTechnicals() {

		return researchLedgerRepository.findByResearchStatus(ResearchTrigger.BUY);
	}

}
