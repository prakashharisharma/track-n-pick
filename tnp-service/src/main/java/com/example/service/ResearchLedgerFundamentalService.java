package com.example.service;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.ResearchLedgerFundamental;
import com.example.model.ledger.ValuationLedger;
import com.example.model.master.Stock;
import com.example.repo.ledger.ResearchLedgerFundamentalRepository;
import com.example.util.io.model.ResearchIO.ResearchTrigger;

@Transactional
@Service
public class ResearchLedgerFundamentalService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResearchLedgerFundamentalService.class);

	@Autowired
	private ResearchLedgerFundamentalRepository researchLedgerRepository;

	public void addResearch(Stock stock,ValuationLedger entryValuation) {

		ResearchLedgerFundamental researchLedger = researchLedgerRepository.findByStockAndResearchStatus(stock, ResearchTrigger.BUY);

		if (researchLedger == null) {
			researchLedger = new ResearchLedgerFundamental();
			researchLedger.setStock(stock);

			researchLedger.setResearchStatus(ResearchTrigger.BUY);
			
			researchLedger.setNotified(false);
			researchLedger.setNotifiedStorage(false);
			
			researchLedger.setEntryValuation(entryValuation);
			
			researchLedgerRepository.save(researchLedger);
			
		} else {
			
			//researchLedger.setEntryValuation(entryValuation);
			
			LOGGER.debug(stock.getNseSymbol() + " is already in Ledger for " + stock.getNseSymbol());
		}

	}

	public void updateResearch(Stock stock, ValuationLedger exitValuation) {

		ResearchLedgerFundamental researchLedger = researchLedgerRepository.findByStockAndResearchStatus(stock, ResearchTrigger.BUY);
		
		if (researchLedger != null) {

			researchLedger.setResearchStatus(ResearchTrigger.SELL);
			
			researchLedger.setNotified(false);
			researchLedger.setNotifiedStorage(false);
			researchLedger.setExitValuation(exitValuation);
			
			researchLedgerRepository.save(researchLedger);	
		}
		
		
	}

	public boolean isResearchStorageNotified(Stock stock, ResearchTrigger researchTrigger) {
		boolean isResearchStorageNotified = true;
		ResearchLedgerFundamental researchLedger = researchLedgerRepository.findByStockAndResearchStatusAndNotifiedStorage(stock, researchTrigger, false);
		
		if(researchLedger != null) {
			isResearchStorageNotified = false;
		}
		
		return isResearchStorageNotified;
	}


	public void updateResearchNotified(ResearchLedgerFundamental researchLedger) {
		
		ResearchLedgerFundamental researchLedger1 = researchLedgerRepository.findBySrlId(researchLedger.getSrlId());
		
		if (researchLedger1 != null) {

			researchLedger1.setNotified(true);

			researchLedgerRepository.save(researchLedger1);
		}
		
	}
	
	
	public void updateResearchNotifiedStorage(ResearchLedgerFundamental researchLedger) {

		ResearchLedgerFundamental researchLedger1 = researchLedgerRepository.findBySrlId(researchLedger.getSrlId());
		
		if (researchLedger1 != null) {

			researchLedger1.setNotifiedStorage(true);
			
			researchLedgerRepository.save(researchLedger1);
		}
		

	}

	public List<ResearchLedgerFundamental> allActiveResearch() {
		return researchLedgerRepository.findByResearchStatus(ResearchTrigger.BUY);
	}

	public List<ResearchLedgerFundamental> buyNotificationPending() {
		return researchLedgerRepository.findByResearchStatusAndNotified(ResearchTrigger.BUY, false);
	}

	public List<ResearchLedgerFundamental> sellNotificationPending() {
		return researchLedgerRepository.findByResearchStatusAndNotified(ResearchTrigger.SELL, false);
	}

	public List<ResearchLedgerFundamental> researchStocksFundamentals() {

		return researchLedgerRepository.findByResearchStatus(ResearchTrigger.BUY);
	}
	public boolean isResearchActive(Stock stock) {
		boolean isResearchActive = false;
		
		ResearchLedgerFundamental researchLedgerFundamental = researchLedgerRepository.findByStockAndResearchStatus(stock, ResearchTrigger.BUY);
		
		if(researchLedgerFundamental != null) {
			isResearchActive = true;
		}
		
		return isResearchActive;
	}

}
