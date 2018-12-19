package com.example.service;

import java.time.LocalDate;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.FundsLedger;
import com.example.model.master.Broker;
import com.example.model.type.FundTransactionType;
import com.example.model.um.User;
import com.example.repo.ledger.FundsLedgerRepository;
import com.example.repo.um.UserBrokerageRepository;

@Transactional
@Service
public class FundsLedgerService {

	@Autowired
	private FundsLedgerRepository fundsLedgerRepository;

	@Autowired
	private UserBrokerageRepository userBrokerageRepository;

	public void addFund(User user, double amount, LocalDate transactionDate) {

		Broker broker = userBrokerageRepository.findByBrokerageIdUserAndActive(user, true).getBrokerageId().getBroker();

		FundsLedger fundLedger = new FundsLedger(user, broker, amount, transactionDate, FundTransactionType.ADD);
		fundsLedgerRepository.save(fundLedger);
	}

	public void withdrawFund(User user, double amount, LocalDate transactionDate) {
		Broker broker = userBrokerageRepository.findByBrokerageIdUserAndActive(user, true).getBrokerageId().getBroker();

		FundsLedger fundLedger = new FundsLedger(user, broker, amount, transactionDate, FundTransactionType.WITHDRAW);
		fundsLedgerRepository.save(fundLedger);
	}

}
