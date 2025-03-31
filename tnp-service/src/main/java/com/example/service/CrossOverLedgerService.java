package com.example.service;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import com.example.data.transactional.entities.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.data.transactional.entities.CrossOverLedger;

import com.example.data.transactional.repo.CrossOverLedgerRepository;

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
