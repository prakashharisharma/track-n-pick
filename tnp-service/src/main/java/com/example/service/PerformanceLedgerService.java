package com.example.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.PerformanceLedger;
import com.example.model.um.UserProfile;
import com.example.repo.ledger.PerformanceLedgerRepository;
import com.example.util.MiscUtil;

@Transactional
@Service
public class PerformanceLedgerService {

	@Autowired
	private PerformanceLedgerRepository performanceLedgerRepository;

	@Autowired
	private PortfolioService portfolioService;

	@Autowired
	private FundsLedgerService fundsLedgerService;

	@Autowired
	private MiscUtil miscUtil;

	public void updateMonthlyPerformance(UserProfile user, double investmentValue, double portfolioValue) {

		PerformanceLedger pl = new PerformanceLedger(user, investmentValue, portfolioValue,
				miscUtil.currentMonthFirstDay());
		performanceLedgerRepository.save(pl);
	}

	public void updateMonthlyPerformance(UserProfile user) {

		double investmentValue = fundsLedgerService.currentFinYearInvestment(user);
		
		double portfolioValue = portfolioService.currentValue(user);

		PerformanceLedger pl = new PerformanceLedger(user, investmentValue, portfolioValue,miscUtil.currentMonthFirstDay());
		performanceLedgerRepository.save(pl);
	}
	
	public List<PerformanceLedger> yearlyPerformance(UserProfile user){
		
		return performanceLedgerRepository.findByUserIdAndPerformanceDateGreaterThanEqual(user, miscUtil.currentDatePrevYear());
	}

}
