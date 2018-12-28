package com.example.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.PerformanceLedger;
import com.example.model.um.User;
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

	public void updateMonthlyPerformance(User user, double investmentValue, double portfolioValue) {

		PerformanceLedger pl = new PerformanceLedger(user, investmentValue, portfolioValue,
				miscUtil.currentMonthFirstDay());
		performanceLedgerRepository.save(pl);
	}

	public void updateMonthlyPerformance(User user) {

		double investmentValue = fundsLedgerService.currentFinYearInvestment(user);
		
		double portfolioValue = portfolioService.currentValue(user);

		PerformanceLedger pl = new PerformanceLedger(user, investmentValue, portfolioValue,miscUtil.currentMonthFirstDay());
		performanceLedgerRepository.save(pl);
	}

}
