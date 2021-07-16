package com.example.model.stocks;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.example.model.master.Stock;
import com.example.model.um.UserProfile;

@Entity
@Table(name = "USER_PORTFOLIO")
@AssociationOverrides({ @AssociationOverride(name = "portfolioId.user", joinColumns = @JoinColumn(name = "USER_ID")),
		@AssociationOverride(name = "portfolioId.stock", joinColumns = @JoinColumn(name = "STOCK_ID")) })
public class UserPortfolio implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7852890085397046636L;

	@EmbeddedId
	private UserPortfolioKey portfolioId = new UserPortfolioKey();

	@Column(name = "PRICE", columnDefinition="Decimal(10,2) default '0.00'")
	double averagePrice;

	@Column(name = "QUANTITY")
	long quantity;

	@Column(name = "TARGET_PER", columnDefinition="Decimal(10,2) default '20.00'")
	double targetPer = 30.0;
	
	@Column(name = "AVERAGING_PER", columnDefinition="Decimal(10,2) default '20.00'")
	double averagingPer = 20.0;
	
	@Column(name = "FIRST_TXN_DATE")
	LocalDate firstTxnDate = LocalDate.now();
	
	@Column(name = "LAST_TXN_DATE")
	LocalDate lastTxnDate = LocalDate.now();
	
	public UserPortfolio() {
		super();
	}

	public UserPortfolioKey getPortfolioId() {
		return portfolioId;
	}

	public void setPortfolioId(UserPortfolioKey portfolioId) {
		this.portfolioId = portfolioId;
	}

	@Transient
	public UserProfile getUser() {
		return getPortfolioId().getUser();
	}

	public void setUser(UserProfile user) {
		getPortfolioId().setUser(user);
	}

	@Transient
	public Stock getStock() {
		return getPortfolioId().getStock();
	}

	public void setStock(Stock stock) {
		getPortfolioId().setStock(stock);
	}
	
	public double getAveragePrice() {
		return averagePrice;
	}

	public void setAveragePrice(double averagePrice) {
		this.averagePrice = averagePrice;
	}

	public long getQuantity() {
		return quantity;
	}

	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}

	public double getTargetPer() {
		return targetPer;
	}

	public void setTargetPer(double targetPer) {
		this.targetPer = targetPer;
	}

	public double getAveragingPer() {
		return averagingPer;
	}

	public void setAveragingPer(double averagingPer) {
		this.averagingPer = averagingPer;
	}

	
	
/*	@Override
	public String toString() {
		return "UserPortfolio [portfolioId=" + portfolioId + ", averagePrice=" + averagePrice + ", quantity=" + quantity
				+ "]";
	}*/
	
	public LocalDate getFirstTxnDate() {
		return firstTxnDate;
	}

	public void setFirstTxnDate(LocalDate firstTxnDate) {
		this.firstTxnDate = firstTxnDate;
	}

	public LocalDate getLastTxnDate() {
		return lastTxnDate;
	}

	public void setLastTxnDate(LocalDate lastTxnDate) {
		this.lastTxnDate = lastTxnDate;
	}

	@Override
	public String toString() {
		return "UserPortfolio [portfolioId=" + portfolioId + ", averagePrice=" + averagePrice+ ", targetPer=" + targetPer+ ", averagingPer=" + averagingPer + ", quantity=" + quantity
				+ "]";
	}

}
