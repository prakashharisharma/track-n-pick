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

@Entity
@Table(name = "PERFORMANCE_LEDGER")
public class PerformanceLedger {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PERFORMANCE_LEDGER_ID")
	long performanceLedgerId;
	
	@ManyToOne
	@JoinColumn(name = "userId")
	UserProfile userId;
	
	@Column(name = "INVESTMENT_VALUE", columnDefinition="Decimal(10,2) default '0.00'")
	double investmentValue;
	
	@Column(name = "PORTFOLIO_VALUE", columnDefinition="Decimal(10,2) default '0.00'")
	double portfolioValue;
	
	@Column(name = "PERFORMANCE_DATE")
	LocalDate performanceDate = LocalDate.now();

	public PerformanceLedger() {
		super();
		
	}

	public PerformanceLedger(UserProfile userId, double investmentValue, double portfolioValue,
			LocalDate performanceDate) {
		super();
		this.userId = userId;
		this.investmentValue = investmentValue;
		this.portfolioValue = portfolioValue;
		this.performanceDate = performanceDate;
	}

	public long getPerformanceLedgerId() {
		return performanceLedgerId;
	}

	public void setPerformanceLedgerId(long performanceLedgerId) {
		this.performanceLedgerId = performanceLedgerId;
	}

	public UserProfile getUserId() {
		return userId;
	}

	public void setUserId(UserProfile userId) {
		this.userId = userId;
	}

	public double getInvestmentValue() {
		return investmentValue;
	}

	public void setInvestmentValue(double investmentValue) {
		this.investmentValue = investmentValue;
	}

	public double getPortfolioValue() {
		return portfolioValue;
	}

	public void setPortfolioValue(double portfolioValue) {
		this.portfolioValue = portfolioValue;
	}

	public LocalDate getPerformanceDate() {
		return performanceDate;
	}

	public void setPerformanceDate(LocalDate performanceDate) {
		this.performanceDate = performanceDate;
	}

	@Override
	public String toString() {
		return "PerformanceLedger [performanceLedgerId=" + performanceLedgerId 
				+ ", investmentValue=" + investmentValue + ", portfolioValue=" + portfolioValue + ", performanceDate="
				+ performanceDate + "]";
	}
	
	
}
