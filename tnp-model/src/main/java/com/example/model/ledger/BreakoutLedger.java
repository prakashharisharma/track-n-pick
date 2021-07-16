package com.example.model.ledger;

import java.io.Serializable;
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

import com.example.model.master.Stock;

@Entity
@Table(name = "BREAKOUT_LEDGER")
public class BreakoutLedger implements Serializable{

	public enum BreakoutType {POSITIVE, NEGATIVE}
	
	public enum BreakoutCategory { CROSS50,CROSS100, CROSS200 }
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2127000992678133638L;


	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "BREAKOUT_LEDGER_ID")
	long breakoutId;

	
	@ManyToOne
	@JoinColumn(name = "stockId")
	Stock stockId;
	
	@Column(name = "BREAKOUT_DATE")
	LocalDate breakoutDate = LocalDate.now();
	
	@Column(name = "TYPE")
	@Enumerated(EnumType.STRING)
	BreakoutType breakoutType;
	
	@Column(name = "CATEGORY")
	@Enumerated(EnumType.STRING)
	BreakoutCategory breakoutCategory;

	@Column(name = "PRICE", columnDefinition="Decimal(10,2) default '0.00'")
	double price;
	
	@Column(name = "SMA", columnDefinition="Decimal(10,2) default '0.00'")
	double sma;

	public BreakoutLedger() {
		super();
	}

	public BreakoutLedger(Stock stockId, LocalDate breakoutDate, BreakoutType breakoutType,
			BreakoutCategory breakoutCategory, double price, double sma) {
		super();
		this.stockId = stockId;
		this.breakoutDate = breakoutDate;
		this.breakoutType = breakoutType;
		this.breakoutCategory = breakoutCategory;
		this.price = price;
		this.sma = sma;
	}

	public long getBreakoutId() {
		return breakoutId;
	}

	public void setBreakoutId(long breakoutId) {
		this.breakoutId = breakoutId;
	}

	public Stock getStockId() {
		return stockId;
	}

	public void setStockId(Stock stockId) {
		this.stockId = stockId;
	}

	public LocalDate getBreakoutDate() {
		return breakoutDate;
	}

	public void setBreakoutDate(LocalDate breakoutDate) {
		this.breakoutDate = breakoutDate;
	}

	public BreakoutType getBreakoutType() {
		return breakoutType;
	}

	public void setBreakoutType(BreakoutType breakoutType) {
		this.breakoutType = breakoutType;
	}

	public BreakoutCategory getBreakoutCategory() {
		return breakoutCategory;
	}

	public void setBreakoutCategory(BreakoutCategory breakoutCategory) {
		this.breakoutCategory = breakoutCategory;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getSma() {
		return sma;
	}

	public void setSma(double sma) {
		this.sma = sma;
	}

	@Override
	public String toString() {
		return "BreakoutLedger [breakoutId=" + breakoutId + ", stockId=" + stockId + ", breakoutDate=" + breakoutDate
				+ ", breakoutType=" + breakoutType + ", breakoutCategory=" + breakoutCategory + ", price=" + price
				+ ", sma=" + sma + "]";
	}
	
}
