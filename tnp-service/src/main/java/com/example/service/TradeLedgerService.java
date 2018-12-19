package com.example.service;

import java.time.LocalDate;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.TradeLedger;
import com.example.model.master.Stock;
import com.example.model.type.Exchange;
import com.example.model.type.StockTransactionType;
import com.example.model.um.User;
import com.example.repo.ledger.TradeLedgerRepository;

@Transactional
@Service
public class TradeLedgerService {

	@Autowired
	private TradeLedgerRepository tradeLedgerRepository;

	@Autowired
	private TaxMasterService taxMasterService;

	@Autowired
	private BrokerageService brokerageService;

	public void executeBuy(User user, Stock stock, double price, long quantity) {

		TradeLedger tradeLedger = new TradeLedger(user, stock, price, quantity, StockTransactionType.BUY, Exchange.NSE,
				LocalDate.now());

		this.calculateCharges(tradeLedger, user, price, quantity);
		
		tradeLedgerRepository.save(tradeLedger);

	}

	public void executeSell(User user, Stock stock, double price, long quantity) {

		TradeLedger tradeLedger = new TradeLedger(user, stock, price, quantity, StockTransactionType.SELL, Exchange.NSE,
				LocalDate.now());

		this.calculateCharges(tradeLedger, user, price, quantity);
		
		tradeLedgerRepository.save(tradeLedger);

	}

	private void calculateCharges(TradeLedger tradeLedger, User user, double price, long quantity) {
		
		double brokergaeTotal = (brokerageService.getBrokerage(user).getDeliveryCharge() * quantity) / 100;

		tradeLedger.setBrokerage(brokergaeTotal);

		double nseTxnCharges = (taxMasterService.getTaxMaster().getNseTransactionCharge() * price * quantity) / 100;

		tradeLedger.setNseTransactionCharge(nseTxnCharges);

		double sebiTurnoverFee = (taxMasterService.getTaxMaster().getSebiTurnoverFee() * price * quantity) / 100;

		tradeLedger.setSebiTurnoverFee(sebiTurnoverFee);

		double securityTxnTax = (taxMasterService.getTaxMaster().getSecurityTxnTax() * price * quantity) / 100;

		tradeLedger.setSecurityTxnTax(securityTxnTax);

		double stampDuty = (taxMasterService.getTaxMaster().getStampDuty() * price * quantity) / 100;

		tradeLedger.setStampDuty(stampDuty);

		double gst = (taxMasterService.getTaxMaster().getStampDuty()* (brokergaeTotal + nseTxnCharges + sebiTurnoverFee + stampDuty)) / 100;

		tradeLedger.setGst(gst);
		
	}
}
