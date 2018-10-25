package com.example.model.ledger;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.example.model.um.User;

@Entity
@Table(name = "FEE_TAX_LEDGER")
public class FeeAndTaxLedger {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "FEE_TAX_LEDGER_ID")
	long feeTaxLedgerId;
	
	@ManyToOne
	@JoinColumn(name = "userId")
	User userId;
	
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
