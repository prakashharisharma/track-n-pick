package com.example.service;

import java.time.LocalDate;
import java.util.ArrayList;
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

	public CrossOverLedger addBullish(Stock stock,CrossOverLedger.CrossOverCategory crossOverCategory) {
		
		CrossOverLedger crossOverLedger = crossOverLedgerRepository.findByStockIdAndCrossOverTypeAndCrossOverCategoryAndStatus(stock, CrossOverLedger.CrossOverType.BULLISH,crossOverCategory,CrossOverLedger.Status.OPEN);
		
		if(crossOverLedger == null) {
			
			if(crossOverCategory == CrossOverLedger.CrossOverCategory.CROSS100) {
				crossOverLedger = new CrossOverLedger(stock, LocalDate.now(), CrossOverLedger.CrossOverType.BULLISH,crossOverCategory, stock.getTechnicals().getSma50(),stock.getTechnicals().getSma100(), CrossOverLedger.Status.OPEN );
			}else if (crossOverCategory == CrossOverLedger.CrossOverCategory.CROSS200) {
				crossOverLedger = new CrossOverLedger(stock, LocalDate.now(), CrossOverLedger.CrossOverType.BULLISH,crossOverCategory, stock.getTechnicals().getSma50(),stock.getTechnicals().getSma200(), CrossOverLedger.Status.OPEN );
			}else if (crossOverCategory == CrossOverLedger.CrossOverCategory.CROSS50) {
				crossOverLedger = new CrossOverLedger(stock, LocalDate.now(), CrossOverLedger.CrossOverType.BULLISH,crossOverCategory, stock.getTechnicals().getSma21(),stock.getTechnicals().getSma50(), CrossOverLedger.Status.OPEN );
			}
			
			crossOverLedger.setAvgVolume(stock.getTechnicals().getAvgVolume());
			crossOverLedger.setVolume(stock.getTechnicals().getVolume());
			
			crossOverLedger.setPrice(stock.getStockPrice().getCurrentPrice());
			
			crossOverLedger = crossOverLedgerRepository.save(crossOverLedger);
		}
		
		CrossOverLedger crossOverLedgerPrevBearish = crossOverLedgerRepository.findByStockIdAndCrossOverTypeAndCrossOverCategoryAndStatus(stock, CrossOverLedger.CrossOverType.BEARISH,crossOverCategory,CrossOverLedger.Status.OPEN);
		
		if(crossOverLedgerPrevBearish != null) {
			crossOverLedgerPrevBearish.setStatus(CrossOverLedger.Status.CLOSE);
			crossOverLedgerPrevBearish.setCloseDate(LocalDate.now());
			crossOverLedgerRepository.save(crossOverLedgerPrevBearish);
		}
		
		return crossOverLedger;
		
	}
	
	public CrossOverLedger addBearish(Stock stock,CrossOverLedger.CrossOverCategory crossOverCategory) {
		
		CrossOverLedger crossOverLedger = crossOverLedgerRepository.findByStockIdAndCrossOverTypeAndCrossOverCategoryAndStatus(stock, CrossOverLedger.CrossOverType.BEARISH,crossOverCategory,CrossOverLedger.Status.OPEN);
		
		if(crossOverLedger == null) {
			
			if(crossOverCategory == CrossOverLedger.CrossOverCategory.CROSS100) {
				crossOverLedger = new CrossOverLedger(stock, LocalDate.now(), CrossOverLedger.CrossOverType.BEARISH,crossOverCategory, stock.getTechnicals().getSma50(),stock.getTechnicals().getSma100(), CrossOverLedger.Status.OPEN );
			}else if (crossOverCategory == CrossOverLedger.CrossOverCategory.CROSS200) {
				crossOverLedger = new CrossOverLedger(stock, LocalDate.now(), CrossOverLedger.CrossOverType.BEARISH,crossOverCategory, stock.getTechnicals().getSma50(),stock.getTechnicals().getSma200(), CrossOverLedger.Status.OPEN );
			}else if (crossOverCategory == CrossOverLedger.CrossOverCategory.CROSS50) {
				crossOverLedger = new CrossOverLedger(stock, LocalDate.now(), CrossOverLedger.CrossOverType.BEARISH,crossOverCategory, stock.getTechnicals().getSma21(),stock.getTechnicals().getSma50(), CrossOverLedger.Status.OPEN );
			}
			
			crossOverLedger.setAvgVolume(1000);
			crossOverLedger.setVolume(1000);
			crossOverLedger.setPrice(stock.getStockPrice().getCurrentPrice());
			
			crossOverLedger.setAvgVolume(stock.getTechnicals().getAvgVolume());
			crossOverLedger.setVolume(stock.getTechnicals().getVolume());
			
			crossOverLedger = crossOverLedgerRepository.save(crossOverLedger);
		}
		
		CrossOverLedger crossOverLedgerPrevBullish = crossOverLedgerRepository.findByStockIdAndCrossOverTypeAndCrossOverCategoryAndStatus(stock, CrossOverLedger.CrossOverType.BULLISH,crossOverCategory,CrossOverLedger.Status.OPEN);
		
		if(crossOverLedgerPrevBullish != null) {
			crossOverLedgerPrevBullish.setStatus(CrossOverLedger.Status.CLOSE);
			crossOverLedgerPrevBullish.setCloseDate(LocalDate.now());
			crossOverLedgerRepository.save(crossOverLedgerPrevBullish);
		}
		return crossOverLedger;
	}
	
	public List<CrossOverLedger> getCrossOver(Stock stock){
		List<CrossOverLedger> crossOverList = crossOverLedgerRepository.findByStockIdAndStatusOrderByResearchDateDesc(stock, CrossOverLedger.Status.OPEN);
		
		return crossOverList;
	}
	
	public List<CrossOverLedger> getRecentCrossOver(){
		List<CrossOverLedger> recentCrossOverList = new ArrayList<>();
		
		recentCrossOverList.addAll(crossOverLedgerRepository.findTop5ByCrossOverTypeAndCrossOverCategoryAndStatusOrderByResearchDateDesc(CrossOverLedger.CrossOverType.BULLISH, CrossOverLedger.CrossOverCategory.CROSS200, CrossOverLedger.Status.OPEN));
		
		recentCrossOverList.addAll(crossOverLedgerRepository.findTop5ByCrossOverTypeAndCrossOverCategoryAndStatusOrderByResearchDateDesc(CrossOverLedger.CrossOverType.BEARISH, CrossOverLedger.CrossOverCategory.CROSS200, CrossOverLedger.Status.OPEN));
		
		return recentCrossOverList;
	}
	
}
