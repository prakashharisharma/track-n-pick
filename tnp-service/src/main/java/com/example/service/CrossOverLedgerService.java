package com.example.service;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.transactional.model.ledger.CrossOverLedger;
import com.example.transactional.model.master.Stock;
import com.example.transactional.repo.ledger.CrossOverLedgerRepository;

@Transactional
@Service
public class CrossOverLedgerService {
	@Autowired
	private CrossOverLedgerRepository crossOverLedgerRepository;

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
