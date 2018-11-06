package com.example.model.um;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import com.example.model.master.Stock;

@Embeddable
public class UserPortfolioKey implements Serializable {

	private static final long serialVersionUID = -3500343089756518013L;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	User user;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	Stock stock;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stock == null) ? 0 : stock.hashCode());
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserPortfolioKey other = (UserPortfolioKey) obj;
		if (stock == null) {
			if (other.stock != null)
				return false;
		} else if (!stock.equals(other.stock))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "UserPortfolioKey [user=" + user + ", stock=" + stock + "]";
	}

	
	
}
