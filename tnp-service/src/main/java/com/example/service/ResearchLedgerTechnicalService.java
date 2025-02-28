package com.example.service;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import com.example.dto.TradeSetup;
import com.example.util.FormulaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.ResearchLedgerTechnical;
import com.example.model.master.Stock;

import com.example.repo.ledger.ResearchLedgerTechnicalRepository;
import com.example.util.io.model.ResearchIO.ResearchTrigger;

@Transactional
@Service
public class ResearchLedgerTechnicalService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResearchLedgerTechnicalService.class);


	@Autowired
	private ResearchLedgerTechnicalRepository researchLedgerRepository;

	@Autowired
	private FormulaService formulaService;

	@Autowired
	private CalendarService calendarService;


	public void addResearch(Stock stock, TradeSetup tradeSetup) {

		ResearchLedgerTechnical researchLedger = researchLedgerRepository.findByStockAndResearchStatus(stock, ResearchTrigger.BUY);

		if (researchLedger == null) {

			researchLedger = new ResearchLedgerTechnical();

			researchLedger.setStock(stock);
			researchLedger.setResearchStatus(ResearchTrigger.BUY);
			researchLedger.setStrategy(tradeSetup.getStrategy());
			researchLedger.setSubStrategy(tradeSetup.getSubStrategy());
			//researchLedger.setNotified(false);


			researchLedger.setResearchPrice(tradeSetup.getEntryPrice());
			researchLedger.setResearchDate(stock.getStockPrice().getBhavDate());
			researchLedger.setStopLoss(tradeSetup.getStopLossPrice());
			researchLedger.setTarget(tradeSetup.getTargetPrice());
			researchLedger.setRisk(tradeSetup.getRisk());
			researchLedger.setCorrection(tradeSetup.getCorrection());

			//researchLedger.setResearchScore(score);
			researchLedger.setVolume(stock.getTechnicals().getVolume());
			researchLedger.setVolumeAvg5(stock.getTechnicals().getVolumeAvg5());
			researchLedger.setVolumeAvg20(stock.getTechnicals().getVolumeAvg20());

			researchLedger.setNextTradingDate(calendarService.nextTradingDate(stock.getStockPrice().getBhavDate()));
			researchLedger.setCreatedAt(LocalDate.now());
			researchLedger.setModifiedAt(LocalDate.now());
			researchLedgerRepository.save(researchLedger);
		} else {
			LOGGER.debug(stock.getNseSymbol() + " is already in Ledger for " + stock.getNseSymbol());
		}

	}

	public void update(ResearchLedgerTechnical researchLedgerTechnical){
		researchLedgerTechnical.setModifiedAt(LocalDate.now());
		researchLedgerRepository.save(researchLedgerTechnical);
	}

	public void updateResearch(Stock stock,  double score) {

		ResearchLedgerTechnical researchLedger = researchLedgerRepository.findByStockAndResearchStatus(stock, ResearchTrigger.BUY);
		
		if (researchLedger != null) {

			researchLedger.setResearchStatus(ResearchTrigger.SELL);
			
			//researchLedger.setNotified(false);
			researchLedger.setExitDate(stock.getStockPrice().getBhavDate());
			researchLedger.setExitPrice(stock.getStockPrice().getLow());
			//researchLedger.setExitRule(rule);
			//researchLedger.setExitScore(score);
			
			researchLedgerRepository.save(researchLedger);	
		}
		
		
	}

	
	
	public void updateResearchNotifiedStorage(ResearchLedgerTechnical researchLedger) {

		ResearchLedgerTechnical researchLedger1 = researchLedgerRepository.findBySrlId(researchLedger.getSrlId());
		
		if (researchLedger1 != null) {
			
			researchLedgerRepository.save(researchLedger1);
		}
		

	}

	public List<ResearchLedgerTechnical> allActiveResearch() {
		return researchLedgerRepository.findByResearchStatus(ResearchTrigger.BUY);
	}

	//public List<ResearchLedgerTechnical> buyNotificationPending() {
	//	return researchLedgerRepository.findByResearchStatusAndNotified(ResearchTrigger.BUY, false);
	//}

	//public List<ResearchLedgerTechnical> sellNotificationPending() {
	//	return researchLedgerRepository.findByResearchStatusAndNotified(ResearchTrigger.SELL, false);
	//}

	public List<ResearchLedgerTechnical> researchStocksTechnicals() {

		return researchLedgerRepository.findByResearchStatus(ResearchTrigger.BUY);
	}

	public ResearchLedgerTechnical getActiveResearch(Stock stock){

		List<ResearchLedgerTechnical> researchLedgerTechnicals = researchLedgerRepository.getActiveResearch(stock.getNseSymbol(), ResearchTrigger.BUY);

		if(researchLedgerTechnicals!=null && !researchLedgerTechnicals.isEmpty()){
			return researchLedgerTechnicals.get(researchLedgerTechnicals.size()-1);
		}

		return null;
	}

	public boolean isActive(Stock stock, ResearchTrigger researchStatus){

		List<ResearchLedgerTechnical> researchLedgerTechnicals = researchLedgerRepository.getActiveResearch(stock.getNseSymbol(), researchStatus);

		if(researchLedgerTechnicals!=null && !researchLedgerTechnicals.isEmpty()){

			ResearchLedgerTechnical researchLedgerTechnical = researchLedgerTechnicals.get(researchLedgerTechnicals.size() -1);

			if(researchStatus == researchLedgerTechnical.getResearchStatus()){
				if(researchLedgerTechnical.isConfirmed()) {
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}

	public boolean isResearchActive(Stock stock){
		return this.isActive(stock, ResearchTrigger.BUY);
	}

	public void updateStopLoss(ResearchLedgerTechnical researchLedgerTechnical, double stopLoss){
		researchLedgerTechnical.setStopLoss(stopLoss);
		researchLedgerRepository.save(researchLedgerTechnical);
	}


}
