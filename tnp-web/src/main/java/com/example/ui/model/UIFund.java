package com.example.ui.model;

import java.time.LocalDate;

public class UIFund {

	double amount;
	
	String txnType;
	
	String txnDate;

	public UIFund() {
		super();
	}

	public UIFund(double amount, String txnType, LocalDate txnDate) {
		super();
		this.amount = amount;
		this.txnType = txnType;
		this.txnDate = txnDate.toString();
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getTxnDate() {
		return txnDate;
	}

	public void setTxnDate(String txnDate) {
		this.txnDate = txnDate;
	}

	@Override
	public String toString() {
		return "Fund [amount=" + amount + ", txnType=" + txnType + ", txnDate=" + txnDate + "]";
	}
	
	
}
