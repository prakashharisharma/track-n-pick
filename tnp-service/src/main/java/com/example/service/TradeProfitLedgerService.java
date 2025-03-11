package com.example.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.TradeProfitLedger;
import com.example.model.master.Stock;
import com.example.model.um.UserProfile;
import com.example.repo.ledger.TradeProfitLedgerRepository;
import com.example.util.MiscUtil;

@Transactional
@Service
public class TradeProfitLedgerService {

	@Autowired
	private TradeProfitLedgerRepository tradeProfitLedgerRepository;
	
	@Autowired
	private MiscUtil miscUtil;
	
	public void addProfitEntry(UserProfile user, Stock stock, long quantity, double netProfit) {
		TradeProfitLedger tradeProfitLedger = new TradeProfitLedger(user, stock, quantity, netProfit);
		System.out.println("Adding Profit Entry");
		tradeProfitLedgerRepository.save(tradeProfitLedger);
		
	}
	
	public double currentYearProfit(UserProfile user) {
		
		Double totalProfit = tradeProfitLedgerRepository.getTotalProfitBetweenTwoDates(user, miscUtil.currentYearFirstDay(), miscUtil.currentDate());
		
		if(totalProfit == null){
			totalProfit = 0.00;
		}
		
		return totalProfit;
	}
	
	public double currentFinYearProfit(UserProfile user) {
		
		Double totalProfit = tradeProfitLedgerRepository.getTotalProfitBetweenTwoDates(user, miscUtil.currentFinYearFirstDay(), miscUtil.currentDate());
		
		if(totalProfit == null){
			totalProfit = 0.00;
		}
		
		
		return totalProfit;
	}

	public double allTimeProfit(UserProfile user) {

		Double totalProfit = tradeProfitLedgerRepository.getTotalProfit(user);

		if(totalProfit == null){
			totalProfit = 0.00;
		}


		return totalProfit;
	}
	
}
