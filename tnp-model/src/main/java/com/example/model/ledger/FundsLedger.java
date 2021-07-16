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

import com.example.model.master.Broker;
import com.example.model.type.FundTransactionType;
import com.example.model.um.UserProfile;

@Entity
@Table(name = "FUNDS_LEDGER")
public class FundsLedger {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "FUND_LEDGER_ID")
	long fundLedgerId;
	
	@ManyToOne
	@JoinColumn(name = "userId")
	UserProfile userId;

	@ManyToOne
	@JoinColumn(name = "brokerId")
	Broker brokerId;
	
	@Column(name = "AMOUNT", columnDefinition="Decimal(10,2) default '0.00'")
	double amount;
	
	@Column(name = "TXN_DATE")
	LocalDate transactionDate;
	
	@Column(name = "TXN_TYPE")
	@Enumerated(EnumType.STRING)
	FundTransactionType  transactionType;

	public FundsLedger() {
		super();
		// TODO Auto-generated constructor stub
	}

	public FundsLedger(UserProfile userId, Broker brokerId, double amount, LocalDate transactionDate,
			FundTransactionType transactionType) {
		super();
		this.userId = userId;
		this.brokerId = brokerId;
		this.amount = amount;
		this.transactionDate = transactionDate;
		this.transactionType = transactionType;
	}

	public long getFundLedgerId() {
		return fundLedgerId;
	}

	public void setFundLedgerId(long fundLedgerId) {
		this.fundLedgerId = fundLedgerId;
	}

	public UserProfile getUserId() {
		return userId;
	}

	public void setUserId(UserProfile userId) {
		this.userId = userId;
	}

	public Broker getBrokerId() {
		return brokerId;
	}

	public void setBrokerId(Broker brokerId) {
		this.brokerId = brokerId;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(LocalDate transactionDate) {
		this.transactionDate = transactionDate;
	}

	public FundTransactionType getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(FundTransactionType transactionType) {
		this.transactionType = transactionType;
	}

}
