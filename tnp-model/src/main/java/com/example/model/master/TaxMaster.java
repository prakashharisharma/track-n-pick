package com.example.model.master;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TAX_MASTER")
public class TaxMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "TAX_MASTER_ID")
	long taxMasterId;
	
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
