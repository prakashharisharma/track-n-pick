package com.example.model.ledger;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.example.model.um.User;
import com.example.model.master.Stock;
import com.example.model.type.Exchange;
import com.example.model.type.StockTransactionType;

@Entity
@Table(name = "TRADE_LEDGER")
public class TradeLedger {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "TRADE_ID")
	long tradeId;
	
	@ManyToOne
	@JoinColumn(name = "userId")
	User userId;
	
	@ManyToOne
	@JoinColumn(name = "stockId")
	Stock stockId;
	
	@Column(name = "PRICE")
	double price;
	
	@Column(name = "QUANTITY")
	long quantity;
	
	@Column(name = "TXN_TYPE")
	@Enumerated(EnumType.STRING)
	StockTransactionType  transactionType;
	
	@Column(name = "EXCHANGE")
	@Enumerated(EnumType.STRING)
	Exchange exchange;
	
	@Column(name = "TXN_DATE")
	LocalDate transactionDate;

	@Column(name = "BROKERAGE")
	double brokerage;
	
	@Column(name = "SECURITY_TXN_TAX")
	double securityTxnTax;
	
	@Column(name = "STAMP_DUTY")
	double stampDuty;
	
	@Column(name = "NSE_TXN_CHARGE")
	double nseTransactionCharge;
	
	@Column(name = "BSE_TXN_CHARGE")
	double bseTransactionCharge;
	
	@Column(name = "SEBI_TURNOVER_FEE")
	double sebiTurnoverFee;
	
	@Column(name = "GST")
	double gst;
	
}
