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
import com.example.model.master.Stock;

@Entity
@Table(name = "DIVIDEND_LEDGER")
public class DividendLedger {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "DIVIDEND_ID")
	long dividendId;
	
	@ManyToOne
	@JoinColumn(name = "userId")
	User userId;
	
	@ManyToOne
	@JoinColumn(name = "stockId")
	Stock stockId;
	
	@Column(name = "PER_SHARE_AMOUNT")
	double perShareAmount;
	
	@Column(name = "QUANTITY")
	long quantity;
	
	@Column(name = "TXN_DATE")
	LocalDate transactionDate;
}
