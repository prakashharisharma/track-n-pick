package com.example.service;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.BreakoutLedger;
import com.example.model.ledger.BreakoutLedger.BreakoutCategory;
import com.example.model.ledger.BreakoutLedger.BreakoutType;
import com.example.model.master.Stock;
import com.example.repo.ledger.BreakoutLedgerRepository;

@Transactional
@Service
public class BreakoutLedgerService {
	@Autowired
	private BreakoutLedgerRepository breakoutLedgerRepository;
	
	public void addPositive(Stock stock,BreakoutCategory breakoutCategory) {
		
		BreakoutLedger breakoutLedger =  breakoutLedgerRepository.findByStockIdAndBreakoutTypeAndBreakoutCategoryAndBreakoutDate(stock, BreakoutLedger.BreakoutType.POSITIVE, breakoutCategory, LocalDate.now());
		
		if(breakoutLedger == null) {

			breakoutLedger = new BreakoutLedger(stock, stock.getStockPrice().getBhavDate(), BreakoutLedger.BreakoutType.POSITIVE, breakoutCategory);

			breakoutLedgerRepository.save(breakoutLedger);
		}
		
	}
	public void addNegative(Stock stock,BreakoutCategory breakoutCategory) {

		BreakoutLedger breakoutLedger =  breakoutLedgerRepository.findByStockIdAndBreakoutTypeAndBreakoutCategoryAndBreakoutDate(stock, BreakoutLedger.BreakoutType.NEGATIVE, breakoutCategory, LocalDate.now());

		if(breakoutLedger == null) {

				breakoutLedger = new BreakoutLedger(stock, LocalDate.now(), BreakoutLedger.BreakoutType.NEGATIVE, breakoutCategory);

			breakoutLedgerRepository.save(breakoutLedger);
		}
	}
	
	public List<BreakoutLedger> getBreakouts(Stock stock){

		List<BreakoutLedger> breakoutList = breakoutLedgerRepository.findByStockIdOrderByBreakoutDateDesc(stock);
		
		return breakoutList;
	}
	
	public boolean isBreakout(Stock stock,BreakoutType breakoutType,BreakoutCategory breakoutCategory){
		
		boolean isBreakout = false;
		
		BreakoutLedger breakoutLedger =  breakoutLedgerRepository.findByStockIdAndBreakoutTypeAndBreakoutCategoryAndBreakoutDate(stock, breakoutType, breakoutCategory, LocalDate.now());
		
		if (breakoutLedger != null) {
			isBreakout = true;
		}
		
		return isBreakout;
	}
	
}
