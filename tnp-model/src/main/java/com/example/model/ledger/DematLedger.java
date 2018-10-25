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

import com.example.model.master.Broker;
import com.example.model.um.User;

@Entity
@Table(name = "DEMAT_LEDGER")
public class DematLedger {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "DEMAT_LEDGER_ID")
	long dematLedgerId;
	
	@ManyToOne
	@JoinColumn(name = "userId")
	User userId;
	
	@ManyToOne
	@JoinColumn(name = "brokerId")
	Broker brokerId;
	
	@Column(name = "AMOUNT")
	double amount;
	
	@Column(name = "DETAILS")
	double details;
	
	@Column(name = "TXN_DATE")
	LocalDate transactionDate;
	
}
