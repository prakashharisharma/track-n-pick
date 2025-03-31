package com.example.service;

import javax.transaction.Transactional;

import com.example.data.transactional.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.data.transactional.entities.PerformanceLedger;
import com.example.data.transactional.repo.PerformanceLedgerRepository;
import com.example.util.MiscUtil;

@Transactional
@Service
public class PerformanceLedgerService {

	@Autowired
	private PerformanceLedgerRepository performanceLedgerRepository;

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

		double investmentValue = fundsLedgerService.allTimeInvestment(user);
		
		//double portfolioValue = portfolioServiceOld.currentValue(user);
		double portfolioValue = 0.0;

		PerformanceLedger pl = new PerformanceLedger(user, investmentValue, portfolioValue,miscUtil.currentMonthFirstDay());
		performanceLedgerRepository.save(pl);
	}


}
