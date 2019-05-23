package com.example.service;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.BreakoutLedger;
import com.example.model.ledger.BreakoutLedger.BreakoutCategory;
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
			if(breakoutCategory == BreakoutLedger.BreakoutCategory.CROSS50) {
				breakoutLedger = new BreakoutLedger(stock, LocalDate.now(), BreakoutLedger.BreakoutType.POSITIVE, breakoutCategory, stock.getStockPrice().getCurrentPrice(), stock.getTechnicals().getSma50());
			}else if(breakoutCategory == BreakoutLedger.BreakoutCategory.CROSS100) {
				breakoutLedger = new BreakoutLedger(stock, LocalDate.now(), BreakoutLedger.BreakoutType.POSITIVE, breakoutCategory, stock.getStockPrice().getCurrentPrice(), stock.getTechnicals().getSma100());
			}else if(breakoutCategory == BreakoutLedger.BreakoutCategory.CROSS200) {
				breakoutLedger = new BreakoutLedger(stock, LocalDate.now(), BreakoutLedger.BreakoutType.POSITIVE, breakoutCategory, stock.getStockPrice().getCurrentPrice(), stock.getTechnicals().getSma200());
			}
			
			breakoutLedgerRepository.save(breakoutLedger);
		}
		
	}
	public void addNegative(Stock stock,BreakoutCategory breakoutCategory) {
		BreakoutLedger breakoutLedger =  breakoutLedgerRepository.findByStockIdAndBreakoutTypeAndBreakoutCategoryAndBreakoutDate(stock, BreakoutLedger.BreakoutType.NEGATIVE, breakoutCategory, LocalDate.now());
		if(breakoutLedger == null) {
			if(breakoutCategory == BreakoutLedger.BreakoutCategory.CROSS50) {
				breakoutLedger = new BreakoutLedger(stock, LocalDate.now(), BreakoutLedger.BreakoutType.NEGATIVE, breakoutCategory, stock.getStockPrice().getCurrentPrice(), stock.getTechnicals().getSma50());
			}else if(breakoutCategory == BreakoutLedger.BreakoutCategory.CROSS100) {
				breakoutLedger = new BreakoutLedger(stock, LocalDate.now(), BreakoutLedger.BreakoutType.NEGATIVE, breakoutCategory, stock.getStockPrice().getCurrentPrice(), stock.getTechnicals().getSma100());
			}else if(breakoutCategory == BreakoutLedger.BreakoutCategory.CROSS200) {
				breakoutLedger = new BreakoutLedger(stock, LocalDate.now(), BreakoutLedger.BreakoutType.NEGATIVE, breakoutCategory, stock.getStockPrice().getCurrentPrice(), stock.getTechnicals().getSma200());
			}
			
			breakoutLedgerRepository.save(breakoutLedger);
		}
	}
	
	public List<BreakoutLedger> getBreakouts(Stock stock){
		List<BreakoutLedger> breakoutList = breakoutLedgerRepository.findByStockIdOrderByBreakoutDateDesc(stock);
		
		return breakoutList;
	}
}
