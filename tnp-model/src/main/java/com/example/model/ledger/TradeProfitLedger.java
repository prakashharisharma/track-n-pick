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
@Table(name = "TRADE_PROFIT_LEDGER")
public class TradeProfitLedger{
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "TRADE_PROFIT_LEDGER_ID")
	long tradeProfitLedgerId;
	
	@ManyToOne
	@JoinColumn(name = "userId")
	User userId;
	
	@ManyToOne
	@JoinColumn(name = "stockId")
	Stock stockId;
	
	@Column(name = "NET_PROFIT")
	double netProfit;
	
	@Column(name = "TXN_DATE")
	LocalDate transactionDate;
	
}
