package com.example.service;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.FundsLedger;
import com.example.model.master.Broker;
import com.example.model.type.FundTransactionType;
import com.example.model.um.UserProfile;
import com.example.repo.ledger.FundsLedgerRepository;
import com.example.repo.um.UserBrokerageRepository;
import com.example.util.MiscUtil;

@Transactional
@Service
public class FundsLedgerService {

	@Autowired
	private FundsLedgerRepository fundsLedgerRepository;

	@Autowired
	private UserBrokerageRepository userBrokerageRepository;

	@Autowired
	private PortfolioService portfolioService;

	@Autowired
	private MiscUtil miscUtil;

	public void addFYROFund(UserProfile user) {

		Broker broker = userBrokerageRepository.findByBrokerageIdUserAndActive(user, true).getBrokerageId().getBroker();

		double currentValue = portfolioService.currentValue(user);

		FundsLedger fundLedger = new FundsLedger(user, broker, currentValue, miscUtil.currentFinYearFirstDay(),
				FundTransactionType.FYRO);
		fundsLedgerRepository.save(fundLedger);
	}

	public void addCYROFund(UserProfile user) {

		Broker broker = userBrokerageRepository.findByBrokerageIdUserAndActive(user, true).getBrokerageId().getBroker();
		
		double currentValue = 0.00;
		
		if(broker != null) {
			currentValue = portfolioService.currentValue(user);
		}
		FundsLedger fundLedger = new FundsLedger(user, broker, currentValue, miscUtil.currentYearFirstDay(),
				FundTransactionType.CYRO);
		fundsLedgerRepository.save(fundLedger);
	}

	public void addFund(UserProfile user, double amount, LocalDate transactionDate) {

		Broker broker = userBrokerageRepository.findByBrokerageIdUserAndActive(user, true).getBrokerageId().getBroker();

		FundsLedger fundLedger = new FundsLedger(user, broker, amount, transactionDate, FundTransactionType.ADD);
		fundsLedgerRepository.save(fundLedger);
	}

	public void withdrawFund(UserProfile user, double amount, LocalDate transactionDate) {
		Broker broker = userBrokerageRepository.findByBrokerageIdUserAndActive(user, true).getBrokerageId().getBroker();

		FundsLedger fundLedger = new FundsLedger(user, broker, -1 * amount, transactionDate, FundTransactionType.WITHDRAW);
		fundsLedgerRepository.save(fundLedger);
	}

	public List<FundsLedger> recentHistory(UserProfile user) {
		return fundsLedgerRepository.findByUserId(user);
	}

	public double currentYearInvestment(UserProfile user) {

		Double totalInvestment = fundsLedgerRepository.getTotalCYFundBetweenTwoDates(user,
				miscUtil.currentYearFirstDay(), miscUtil.currentDate());

		if (totalInvestment == null) {
			totalInvestment = 0.00;
		}

		return totalInvestment;
	}

	public  double allTimeInvestment(UserProfile user){
		Double totalInvestment = fundsLedgerRepository.getTotalFund(user);

		if (totalInvestment == null) {
			totalInvestment = 0.00;
		}

		return totalInvestment;
	}

	public double currentFinYearInvestment(UserProfile user) {

		Double totalInvestment = fundsLedgerRepository.getTotalFYFundBetweenTwoDates(user,
				miscUtil.currentFinYearFirstDay(), miscUtil.currentDate());

		if (totalInvestment == null) {
			totalInvestment = 0.00;
		}

		return totalInvestment;
	}

}
