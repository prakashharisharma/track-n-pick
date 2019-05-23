package com.example.service;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.CrossOverLedger;
import com.example.model.master.Stock;
import com.example.repo.ledger.CrossOverLedgerRepository;

@Transactional
@Service
public class CrossOverLedgerService {
	@Autowired
	private CrossOverLedgerRepository crossOverLedgerRepository;

	public void addBullish(Stock stock,CrossOverLedger.CrossOverCategory crossOverCategory) {
		
		CrossOverLedger crossOverLedger = crossOverLedgerRepository.findByStockIdAndCrossOverTypeAndCrossOverCategoryAndStatus(stock, CrossOverLedger.CrossOverType.BULLISH,crossOverCategory,CrossOverLedger.Status.OPEN);
		
		if(crossOverLedger == null) {
			
			if(crossOverCategory == CrossOverLedger.CrossOverCategory.CROSS100) {
				crossOverLedger = new CrossOverLedger(stock, LocalDate.now(), CrossOverLedger.CrossOverType.BULLISH,crossOverCategory, stock.getTechnicals().getSma50(),stock.getTechnicals().getSma100(), CrossOverLedger.Status.OPEN );
			}else if (crossOverCategory == CrossOverLedger.CrossOverCategory.CROSS200) {
				crossOverLedger = new CrossOverLedger(stock, LocalDate.now(), CrossOverLedger.CrossOverType.BULLISH,crossOverCategory, stock.getTechnicals().getSma50(),stock.getTechnicals().getSma200(), CrossOverLedger.Status.OPEN );
			}else if (crossOverCategory == CrossOverLedger.CrossOverCategory.CROSS50) {
				crossOverLedger = new CrossOverLedger(stock, LocalDate.now(), CrossOverLedger.CrossOverType.BULLISH,crossOverCategory, stock.getTechnicals().getSma21(),stock.getTechnicals().getSma50(), CrossOverLedger.Status.OPEN );
			}
			
			crossOverLedgerRepository.save(crossOverLedger);
		}
		
		CrossOverLedger crossOverLedgerPrevBearish = crossOverLedgerRepository.findByStockIdAndCrossOverTypeAndCrossOverCategoryAndStatus(stock, CrossOverLedger.CrossOverType.BEARISH,crossOverCategory,CrossOverLedger.Status.OPEN);
		
		if(crossOverLedgerPrevBearish != null) {
			crossOverLedgerPrevBearish.setStatus(CrossOverLedger.Status.CLOSE);
			crossOverLedgerPrevBearish.setCloseDate(LocalDate.now());
			crossOverLedgerRepository.save(crossOverLedgerPrevBearish);
		}
		
	}
	
	public void addBearish(Stock stock,CrossOverLedger.CrossOverCategory crossOverCategory) {
		
		CrossOverLedger crossOverLedger = crossOverLedgerRepository.findByStockIdAndCrossOverTypeAndCrossOverCategoryAndStatus(stock, CrossOverLedger.CrossOverType.BEARISH,crossOverCategory,CrossOverLedger.Status.OPEN);
		
		if(crossOverLedger == null) {
			
			if(crossOverCategory == CrossOverLedger.CrossOverCategory.CROSS100) {
				crossOverLedger = new CrossOverLedger(stock, LocalDate.now(), CrossOverLedger.CrossOverType.BEARISH,crossOverCategory, stock.getTechnicals().getSma50(),stock.getTechnicals().getSma100(), CrossOverLedger.Status.OPEN );
			}else if (crossOverCategory == CrossOverLedger.CrossOverCategory.CROSS200) {
				crossOverLedger = new CrossOverLedger(stock, LocalDate.now(), CrossOverLedger.CrossOverType.BEARISH,crossOverCategory, stock.getTechnicals().getSma50(),stock.getTechnicals().getSma200(), CrossOverLedger.Status.OPEN );
			}else if (crossOverCategory == CrossOverLedger.CrossOverCategory.CROSS50) {
				crossOverLedger = new CrossOverLedger(stock, LocalDate.now(), CrossOverLedger.CrossOverType.BEARISH,crossOverCategory, stock.getTechnicals().getSma21(),stock.getTechnicals().getSma50(), CrossOverLedger.Status.OPEN );
			}
			
			crossOverLedgerRepository.save(crossOverLedger);
		}
		
		CrossOverLedger crossOverLedgerPrevBullish = crossOverLedgerRepository.findByStockIdAndCrossOverTypeAndCrossOverCategoryAndStatus(stock, CrossOverLedger.CrossOverType.BULLISH,crossOverCategory,CrossOverLedger.Status.OPEN);
		
		if(crossOverLedgerPrevBullish != null) {
			crossOverLedgerPrevBullish.setStatus(CrossOverLedger.Status.CLOSE);
			crossOverLedgerPrevBullish.setCloseDate(LocalDate.now());
			crossOverLedgerRepository.save(crossOverLedgerPrevBullish);
		}
		
	}
	
	public List<CrossOverLedger> getCrossOver(Stock stock){
		List<CrossOverLedger> crossOverList = crossOverLedgerRepository.findByStockIdAndStatusOrderByResearchDateDesc(stock, CrossOverLedger.Status.OPEN);
		
		return crossOverList;
	}
}
