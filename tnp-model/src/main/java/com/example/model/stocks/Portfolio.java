package com.example.model.stocks;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.example.model.master.Stock;
import com.example.model.um.User;

@Entity
@Table(name = "PORTFOLIO")
public class Portfolio {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "PORTFOLIO_ID")
	long portfolioId;
	
	@ManyToOne(fetch = FetchType.LAZY,  optional = false)
	@JoinColumn(name = "USER_ID",referencedColumnName ="USER_ID",  nullable = false)
	User user;
	
	@ManyToOne(fetch = FetchType.LAZY,  optional = false)
	@JoinColumn(name = "STOCK_ID", referencedColumnName ="STOCK_ID",  nullable = false)
	Stock stock;
	
	@Column(name = "PRICE")
	double averagePrice;
	
	@Column(name = "QUANTITY")
	long quantity;

	public Portfolio() {
		super();
	}

	public Portfolio(User user, Stock stock, double averagePrice, long quantity) {
		super();
		this.user = user;
		this.stock = stock;
		this.averagePrice = averagePrice;
		this.quantity = quantity;
	}

	public long getPortfolioId() {
		return portfolioId;
	}

	public void setPortfolioId(long portfolioId) {
		this.portfolioId = portfolioId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
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
		return "Portfolio [portfolioId=" + portfolioId + ", stock=" + stock + ", averagePrice=" + averagePrice
				+ ", quantity=" + quantity + "]";
	}

	
}
