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

import com.example.model.um.UserProfile;
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
	UserProfile userId;
	
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

	@Column(name = "BROKERAGE", columnDefinition="Decimal(10,5) default '0.00'")
	double brokerage;
	
	@Column(name = "SECURITY_TXN_TAX", columnDefinition="Decimal(10,5) default '0.00'")
	double securityTxnTax;
	
	@Column(name = "STAMP_DUTY", columnDefinition="Decimal(10,5) default '0.00'")
	double stampDuty;
	
	@Column(name = "NSE_TXN_CHARGE", columnDefinition="Decimal(10,5) default '0.00'")
	double nseTransactionCharge;
	
	@Column(name = "BSE_TXN_CHARGE", columnDefinition="Decimal(10,5) default '0.00'")
	double bseTransactionCharge;
	
	@Column(name = "SEBI_TURNOVER_FEE", columnDefinition="Decimal(10,5) default '0.00'")
	double sebiTurnoverFee;
	
	@Column(name = "GST", columnDefinition="Decimal(10,5) default '0.00'")
	double gst;

	@Column(name = "TOTAL_CHARGES", columnDefinition="Decimal(10,5) default '0.00'")
	double totalCharges;

	public TradeLedger() {
		super();
	}

	
	
	public TradeLedger(UserProfile userId, Stock stockId, double price, long quantity, StockTransactionType transactionType,
			Exchange exchange, LocalDate transactionDate) {
		super();
		this.userId = userId;
		this.stockId = stockId;
		this.price = price;
		this.quantity = quantity;
		this.transactionType = transactionType;
		this.exchange = exchange;
		this.transactionDate = transactionDate;
	}
	public TradeLedger(UserProfile userId, Stock stockId, long quantity, StockTransactionType transactionType,
			Exchange exchange, LocalDate transactionDate) {
		super();
		this.userId = userId;
		this.stockId = stockId;
		this.quantity = quantity;
		this.transactionType = transactionType;
		this.exchange = exchange;
		this.transactionDate = transactionDate;
	}


	public long getTradeId() {
		return tradeId;
	}

	public void setTradeId(long tradeId) {
		this.tradeId = tradeId;
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

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public long getQuantity() {
		return quantity;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	public StockTransactionType getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(StockTransactionType transactionType) {
		this.transactionType = transactionType;
	}

	public Exchange getExchange() {
		return exchange;
	}

	public void setExchange(Exchange exchange) {
		this.exchange = exchange;
	}

	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(LocalDate transactionDate) {
		this.transactionDate = transactionDate;
	}

	public double getBrokerage() {
		return brokerage;
	}

	public void setBrokerage(double brokerage) {
		this.brokerage = brokerage;
	}

	public double getSecurityTxnTax() {
		return securityTxnTax;
	}

	public void setSecurityTxnTax(double securityTxnTax) {
		this.securityTxnTax = securityTxnTax;
	}

	public double getStampDuty() {
		return stampDuty;
	}

	public void setStampDuty(double stampDuty) {
		this.stampDuty = stampDuty;
	}

	public double getNseTransactionCharge() {
		return nseTransactionCharge;
	}

	public void setNseTransactionCharge(double nseTransactionCharge) {
		this.nseTransactionCharge = nseTransactionCharge;
	}

	public double getBseTransactionCharge() {
		return bseTransactionCharge;
	}

	public void setBseTransactionCharge(double bseTransactionCharge) {
		this.bseTransactionCharge = bseTransactionCharge;
	}

	public double getSebiTurnoverFee() {
		return sebiTurnoverFee;
	}

	public void setSebiTurnoverFee(double sebiTurnoverFee) {
		this.sebiTurnoverFee = sebiTurnoverFee;
	}

	public double getGst() {
		return gst;
	}

	public void setGst(double gst) {
		this.gst = gst;
	}

	public double getTotalCharges() {
		return totalCharges;
	}

	public void setTotalCharges(double totalCharges) {
		this.totalCharges = totalCharges;
	}
}
