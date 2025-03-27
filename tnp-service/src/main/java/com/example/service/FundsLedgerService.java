package com.example.service;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import com.example.model.um.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.FundsLedger;
import com.example.model.master.Broker;
import com.example.model.type.FundTransactionType;
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
	private MiscUtil miscUtil;


	public void addFund(User user, double amount, LocalDate transactionDate) {

		Broker broker = userBrokerageRepository.findByBrokerageIdUserAndActive(user, true).getBrokerageId().getBroker();

		FundsLedger fundLedger = new FundsLedger(user, broker, amount, transactionDate, FundTransactionType.ADD);
		fundsLedgerRepository.save(fundLedger);
	}

	public void withdrawFund(User user, double amount, LocalDate transactionDate) {
		Broker broker = userBrokerageRepository.findByBrokerageIdUserAndActive(user, true).getBrokerageId().getBroker();

		FundsLedger fundLedger = new FundsLedger(user, broker, -1 * amount, transactionDate, FundTransactionType.WITHDRAW);
		fundsLedgerRepository.save(fundLedger);
	}

	public List<FundsLedger> recentHistory(User user) {
		return fundsLedgerRepository.findByUserId(user);
	}



	public  double allTimeInvestment(User user){
		Double totalInvestment = fundsLedgerRepository.getTotalFund(user);

		if (totalInvestment == null) {
			totalInvestment = 0.00;
		}

		return totalInvestment;
	}


}
