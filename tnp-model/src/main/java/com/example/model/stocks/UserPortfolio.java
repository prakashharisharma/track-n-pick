package com.example.model.stocks;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.example.model.master.Stock;
import com.example.model.um.User;
import com.example.model.um.UserPortfolioKey;

@Entity
@Table(name = "USER_PORTFOLIO")
@AssociationOverrides({ @AssociationOverride(name = "portfolioId.user", joinColumns = @JoinColumn(name = "USER_ID")),
		@AssociationOverride(name = "portfolioId.stock", joinColumns = @JoinColumn(name = "STOCK_ID")) })
public class UserPortfolio {

	@EmbeddedId
	private UserPortfolioKey portfolioId = new UserPortfolioKey();

	@Column(name = "PRICE")
	double averagePrice;

	@Column(name = "QUANTITY")
	long quantity;

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
	public User getUser() {
		return getPortfolioId().getUser();
	}

	public void setUser(User user) {
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

	@Override
	public String toString() {
		return "UserPortfolio [portfolioId=" + portfolioId + ", averagePrice=" + averagePrice + ", quantity=" + quantity
				+ "]";
	}

	
	
	
}
