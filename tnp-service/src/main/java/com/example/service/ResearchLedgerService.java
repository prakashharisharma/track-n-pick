package com.example.service;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.ResearchLedger;
import com.example.model.master.Stock;
import com.example.repo.ledger.ResearchLedgerRepository;
import com.example.util.MiscUtil;
import com.example.util.io.model.ResearchIO.ResearchTrigger;
import com.example.util.io.model.ResearchIO.ResearchType;

@Transactional
@Service
public class ResearchLedgerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResearchLedgerService.class);

	@Autowired
	private ResearchLedgerRepository researchLedgerRepository;

	@Autowired
	private MiscUtil miscUtil;

	public void addResearch(Stock stock, ResearchType researchType) {

		ResearchLedger researchLedger = researchLedgerRepository.findByStockAndResearchType(stock, researchType);

		if (researchLedger == null) {
			researchLedger = new ResearchLedger();
			researchLedger.setStock(stock);
			researchLedger.setEntryhDate(miscUtil.currentDate());
			researchLedger.setEntryPrice(stock.getStockPrice().getCurrentPrice());
			researchLedger.setResearchType(researchType);
			researchLedger.setResearchStatus(ResearchTrigger.BUY);
			researchLedgerRepository.save(researchLedger);
		} else {
			LOGGER.debug(stock.getNseSymbol() + " is already in Ledger for " + researchType);
		}

	}

	public void updateResearch(Stock stock, ResearchType researchType) {

		ResearchLedger researchLedger = researchLedgerRepository.findByStockAndResearchType(stock, researchType);
		if (researchLedger != null) {

			researchLedger.setExitDate(miscUtil.currentDate());
			researchLedger.setExitPrice(stock.getStockPrice().getCurrentPrice());
			researchLedger.setResearchStatus(ResearchTrigger.SELL);

		} else {
			researchLedger = new ResearchLedger();
			researchLedger.setStock(stock);
			researchLedger.setExitDate(miscUtil.currentDate());
			researchLedger.setExitPrice(stock.getStockPrice().getCurrentPrice());
			researchLedger.setResearchType(researchType);
			researchLedger.setResearchStatus(ResearchTrigger.SELL);

		}
		researchLedgerRepository.save(researchLedger);
	}

	public boolean isResearchStorageNotified(Stock stock, ResearchType researchType, ResearchTrigger researchTrigger) {
		boolean isResearchStorageNotified = true;
		ResearchLedger researchLedger = researchLedgerRepository.findByStockAndResearchTypeAndResearchStatusAndNotifiedStorage(stock, researchType, researchTrigger, false);
		
		if(researchLedger != null) {
			isResearchStorageNotified = false;
		}
		
		return isResearchStorageNotified;
	}

	
	public void updateResearchNotifiedBuy(Stock stock, ResearchType researchType) {
		ResearchLedger researchLedger = researchLedgerRepository.findByStockAndResearchType(stock, researchType);
		if (researchLedger != null) {

			researchLedger.setNotifiedBuy(true);

		}
		researchLedgerRepository.save(researchLedger);
	}

	public void updateResearchNotifiedSell(Stock stock, ResearchType researchType) {

		ResearchLedger researchLedger = researchLedgerRepository.findByStockAndResearchType(stock, researchType);
		if (researchLedger != null) {

			researchLedger.setNotifiedSell(true);

		}
		researchLedgerRepository.save(researchLedger);

	}
	
	public void updateResearchNotifiedStorage(Stock stock, ResearchType researchType) {

		ResearchLedger researchLedger = researchLedgerRepository.findByStockAndResearchTypeAndNotifiedStorage(stock, researchType, false);
		if (researchLedger != null) {

			researchLedger.setNotifiedStorage(true);
			researchLedgerRepository.save(researchLedger);
		}
		

	}

	public List<ResearchLedger> allActiveResearch() {
		return researchLedgerRepository.findByResearchStatus(ResearchTrigger.BUY);
	}

	public List<ResearchLedger> allNotificationPending() {
		return researchLedgerRepository.findByNotifiedBuyOrNotifiedSell(false, false);
	}

	public List<ResearchLedger> buyNotificationPending() {
		return researchLedgerRepository.findByResearchStatusAndNotifiedBuy(ResearchTrigger.BUY, false);
	}

	public List<ResearchLedger> sellNotificationPending() {
		return researchLedgerRepository.findByResearchStatusAndNotifiedSell(ResearchTrigger.SELL, false);
	}

	public List<ResearchLedger> researchStocksFundamentals() {

		return researchLedgerRepository.findByResearchTypeAndResearchStatus(ResearchType.FUNDAMENTAL,
				ResearchTrigger.BUY);
	}

	public List<ResearchLedger> researchStocksTechnicalss() {

		return researchLedgerRepository.findByResearchTypeAndResearchStatus(ResearchType.TECHNICAL, ResearchTrigger.BUY);
	}

}
