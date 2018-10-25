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
import com.example.model.type.FundTransactionType;

@Entity
@Table(name = "FUNDS_LEDGER")
public class FundsLedger {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "DIVIDEND_ID")
	long dividendId;
	
	@ManyToOne
	@JoinColumn(name = "userId")
	User userId;

	@Column(name = "AMOUNT")
	double amount;
	
	@Column(name = "TXN_DATE")
	LocalDate transactionDate;
	
	@Column(name = "TXN_TYPE")
	@Enumerated(EnumType.STRING)
	FundTransactionType  transactionType;
}
