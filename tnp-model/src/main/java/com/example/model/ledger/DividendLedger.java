package com.example.model.ledger;

import java.io.Serializable;
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
@Table(name = "DIVIDEND_LEDGER")
public class DividendLedger implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6400370693147894820L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "DIVIDEND_ID")
	long dividendId;
	
	@ManyToOne
	@JoinColumn(name = "userId")
	UserProfile userId;
	
	@ManyToOne
	@JoinColumn(name = "stockId")
	Stock stockId;
	
	@Column(name = "PER_SHARE_AMOUNT", columnDefinition="Decimal(10,2) default '0.00'")
	double perShareAmount;
	
	@Column(name = "QUANTITY")
	long quantity;
	
	@Column(name = "EX_DATE")
	LocalDate exDate = LocalDate.now();
	
	@Column(name = "RECORD_DATE")
	LocalDate recordDate= LocalDate.now();
	
	@Column(name = "TXN_DATE")
	LocalDate transactionDate= LocalDate.now();

	public DividendLedger() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DividendLedger(UserProfile userId, Stock stockId, double perShareAmount, long quantity, LocalDate exDate,
			LocalDate recordDate, LocalDate transactionDate) {
		super();
		this.userId = userId;
		this.stockId = stockId;
		this.perShareAmount = perShareAmount;
		this.quantity = quantity;
		this.exDate = exDate;
		this.recordDate = recordDate;
		this.transactionDate = transactionDate;
	}

	public long getDividendId() {
		return dividendId;
	}

	public void setDividendId(long dividendId) {
		this.dividendId = dividendId;
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

	public double getPerShareAmount() {
		return perShareAmount;
	}

	public void setPerShareAmount(double perShareAmount) {
		this.perShareAmount = perShareAmount;
	}

	public long getQuantity() {
		return quantity;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	public LocalDate getExDate() {
		return exDate;
	}

	public void setExDate(LocalDate exnDate) {
		this.exDate = exnDate;
	}

	public LocalDate getRecordDate() {
		return recordDate;
	}

	public void setRecordDate(LocalDate recordDate) {
		this.recordDate = recordDate;
	}

	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(LocalDate transactionDate) {
		this.transactionDate = transactionDate;
	}
	
}
