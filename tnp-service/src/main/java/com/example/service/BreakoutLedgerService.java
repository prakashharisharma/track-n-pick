package com.example.service;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import com.example.transactional.model.stocks.StockPrice;
import com.example.transactional.service.StockPriceService;
import com.example.util.io.model.type.Timeframe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.transactional.model.ledger.BreakoutLedger;
import com.example.transactional.model.ledger.BreakoutLedger.BreakoutCategory;
import com.example.transactional.model.ledger.BreakoutLedger.BreakoutType;
import com.example.transactional.model.master.Stock;
import com.example.transactional.repo.ledger.BreakoutLedgerRepository;

@Transactional
@Service
public class BreakoutLedgerService {
	@Autowired
	private BreakoutLedgerRepository breakoutLedgerRepository;

	@Autowired
	private StockPriceService<StockPrice> stockPriceService;
	
	public void addPositive(Stock stock, Timeframe timeframe, BreakoutCategory breakoutCategory) {

		LocalDate bhavDate =  stockPriceService.get(stock, timeframe).getSessionDate();

		BreakoutLedger breakoutLedger =  breakoutLedgerRepository.findByStockIdAndBreakoutTypeAndBreakoutCategoryAndBreakoutDate(stock, BreakoutLedger.BreakoutType.POSITIVE, breakoutCategory, bhavDate);
		
		if(breakoutLedger == null) {

			breakoutLedger = new BreakoutLedger(stock, bhavDate, BreakoutLedger.BreakoutType.POSITIVE, breakoutCategory);

			breakoutLedgerRepository.save(breakoutLedger);
		}
		
	}
	public void addNegative(Stock stock, Timeframe timeframe, BreakoutCategory breakoutCategory) {

		LocalDate bhavDate = stockPriceService.get(stock, timeframe).getSessionDate();

		BreakoutLedger breakoutLedger =  breakoutLedgerRepository.findByStockIdAndBreakoutTypeAndBreakoutCategoryAndBreakoutDate(stock, BreakoutLedger.BreakoutType.NEGATIVE, breakoutCategory, bhavDate);

		if(breakoutLedger == null) {

				breakoutLedger = new BreakoutLedger(stock, bhavDate, BreakoutLedger.BreakoutType.NEGATIVE, breakoutCategory);

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

	public BreakoutLedger get(Stock stock,BreakoutType breakoutType,BreakoutCategory breakoutCategory){
		return   breakoutLedgerRepository.findByStockIdAndBreakoutTypeAndBreakoutCategory(stock, breakoutType, breakoutCategory);
	}

	public void update(BreakoutLedger breakoutLedger){
		breakoutLedgerRepository.save(breakoutLedger);
	}
	
}
