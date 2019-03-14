package com.example.service;

import java.time.LocalDate;

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

	public void add(Stock stock) {
		
		CrossOverLedger crossOverLedger = crossOverLedgerRepository.findByStockIdAndCrossOverType(stock, CrossOverLedger.CrossOverType.BULLISH);
		
		if(crossOverLedger == null) {
			crossOverLedger = new CrossOverLedger(stock, LocalDate.now(), CrossOverLedger.CrossOverType.BULLISH); 
			crossOverLedgerRepository.save(crossOverLedger);
		}
	}
	
	public void remove(Stock stock) {
		CrossOverLedger crossOverLedger = crossOverLedgerRepository.findByStockIdAndCrossOverType(stock, CrossOverLedger.CrossOverType.BULLISH);
		if(crossOverLedger != null) {
			crossOverLedgerRepository.save(crossOverLedger);
		}
	}
}
