package com.example.service;

import java.time.LocalDate;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.model.ledger.TradeLedger;
import com.example.model.master.Stock;
import com.example.model.type.Exchange;
import com.example.model.type.StockTransactionType;
import com.example.model.um.UserProfile;
import com.example.repo.ledger.TradeLedgerRepository;
import com.example.util.MiscUtil;

@Transactional
@Service
public class TradeLedgerService {

	@Autowired
	private TradeLedgerRepository tradeLedgerRepository;

	@Autowired
	private TaxMasterService taxMasterService;

	@Autowired
	private BrokerageService brokerageService;

	@Autowired
	private MiscUtil miscUtil;

	public void executeBuy(UserProfile user, Stock stock, double price, long quantity) {

		TradeLedger tradeLedger = new TradeLedger(user, stock, price, quantity, StockTransactionType.BUY, Exchange.NSE,
				LocalDate.now());

		this.calculateCharges(tradeLedger, user, price, quantity);

		tradeLedgerRepository.save(tradeLedger);

	}
	
	public void executeBonus(UserProfile user, Stock stock,long quantity) {
		TradeLedger tradeLedger = new TradeLedger(user, stock, quantity, StockTransactionType.BONUS, Exchange.NSE,
				LocalDate.now());
		
		tradeLedgerRepository.save(tradeLedger);

	}
	public void executeSplit(UserProfile user, Stock stock,long quantity) {
		TradeLedger tradeLedger = new TradeLedger(user, stock, quantity, StockTransactionType.SPLIT, Exchange.NSE,
				LocalDate.now());
		
		tradeLedgerRepository.save(tradeLedger);
	}
	
	public void executeSell(UserProfile user, Stock stock, double price, long quantity) {

		TradeLedger tradeLedger = new TradeLedger(user, stock, price, quantity, StockTransactionType.SELL, Exchange.NSE,
				LocalDate.now());

		this.calculateCharges(tradeLedger, user, price, quantity);
		System.out.println("Executing Sell");
		tradeLedgerRepository.save(tradeLedger);

	}

	private void calculateCharges(TradeLedger tradeLedger, UserProfile user, double price, long quantity) {

		//double brokergaeTotal = (brokerageService.getBrokerage(user).getDeliveryCharge() * quantity) / 100;

		double brokergaeTotal = ( 20.0 + 3.60) ;

		tradeLedger.setBrokerage(brokergaeTotal);

		double nseTxnCharges = (taxMasterService.getTaxMaster().getNseTransactionCharge() * price * quantity) / 100;

		tradeLedger.setNseTransactionCharge(nseTxnCharges);

		double sebiTurnoverFee = (taxMasterService.getTaxMaster().getSebiTurnoverFee() * price * quantity) / 100;

		tradeLedger.setSebiTurnoverFee(sebiTurnoverFee);

		double securityTxnTax = (taxMasterService.getTaxMaster().getSecurityTxnTax() * price * quantity) / 100;

		tradeLedger.setSecurityTxnTax(securityTxnTax);

		double stampDuty = (taxMasterService.getTaxMaster().getStampDuty() * price * quantity) / 100;

		tradeLedger.setStampDuty(stampDuty);

		double gst = (taxMasterService.getTaxMaster().getGst()
				* (brokergaeTotal + nseTxnCharges + sebiTurnoverFee + stampDuty)) / 100;
		tradeLedger.setGst(gst);
		double totaL = brokergaeTotal + nseTxnCharges + sebiTurnoverFee + securityTxnTax + stampDuty + gst;


		tradeLedger.setTotalCharges(totaL);

	}

	public double getFYNetTaxPaid(UserProfile user) {

		Double fyNetTaxPaid = tradeLedgerRepository.getNetTaxPaidBetweenTwoDates(user,
				miscUtil.currentFinYearFirstDay(), miscUtil.currentDate());
		if (fyNetTaxPaid == null) {
			fyNetTaxPaid = 0.00;
		}
		return fyNetTaxPaid;
	}

	public double getFYBrokeragePaid(UserProfile user) {

		Double fyBrokeragePaid = tradeLedgerRepository.getBrokeragePaidBetweenTwoDates(user,
				miscUtil.currentFinYearFirstDay(), miscUtil.currentDate());
		if (fyBrokeragePaid == null) {
			fyBrokeragePaid = 0.00;
		}
		return fyBrokeragePaid;
	}

	public List<TradeLedger> getCashFlows(UserProfile user, Long stockId){

		return tradeLedgerRepository.getCashFlows(user, stockId);
	}
}
