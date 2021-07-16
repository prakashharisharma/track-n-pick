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

import com.example.model.um.UserProfile;
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
	UserProfile userId;
	
	@ManyToOne
	@JoinColumn(name = "stockId")
	Stock stockId;
	
	@Column(name = "QUANTITY")
	long quantity;
	
	@Column(name = "NET_PROFIT", columnDefinition="Decimal(10,2) default '0.00'")
	double netProfit;
	
	@Column(name = "TXN_DATE")
	LocalDate transactionDate;

	public TradeProfitLedger() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TradeProfitLedger(UserProfile userId, Stock stockId, long quantity, double netProfit) {
		super();
		this.userId = userId;
		this.stockId = stockId;
		this.quantity = quantity;
		this.netProfit = netProfit;
		this.transactionDate = LocalDate.now();
	}

	public long getTradeProfitLedgerId() {
		return tradeProfitLedgerId;
	}

	public void setTradeProfitLedgerId(long tradeProfitLedgerId) {
		this.tradeProfitLedgerId = tradeProfitLedgerId;
	}

	public UserProfile getUserId() {
		return userId;
	}

	public void setUserId(UserProfile userId) {
		this.userId = userId;
	}

	public Stock getStockId() {
		return stockId;
	}

	public void setStockId(Stock stockId) {
		this.stockId = stockId;
	}

	public long getQuantity() {
		return quantity;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	public double getNetProfit() {
		return netProfit;
	}

	public void setNetProfit(double netProfit) {
		this.netProfit = netProfit;
	}

	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(LocalDate transactionDate) {
		this.transactionDate = transactionDate;
	}
	
	
}
