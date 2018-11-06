package com.example.model.um;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;

@Entity
@Table(name = "USERS")
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "USER_ID")
	long userId;
	
	@Column(name = "USER_EMAIL", unique=true)
	String userEmail;
	
	@Column(name = "FIRST_NAME")
	String firstName;
	
	@Column(name = "LAST_NAME")
	String lastName;
	
	@ManyToMany(fetch = FetchType.LAZY,
            cascade = {
                CascadeType.PERSIST,
                CascadeType.MERGE
            })
    @JoinTable(name = "USER_WATCHLIST",
            joinColumns = { @JoinColumn(name = "USER_ID") },
            inverseJoinColumns = { @JoinColumn(name = "STOCK_ID") })
	private Set<Stock> watchList = new HashSet<>();

	@OneToMany(mappedBy = "portfolioId.user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<UserPortfolio> userPortfolio = new HashSet<>();

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Set<Stock> getWatchList() {
		return watchList;
	}

	public void setWatchList(Set<Stock> watchList) {
		this.watchList = watchList;
	}

	public Set<UserPortfolio> getUserPortfolio() {
		return userPortfolio;
	}

	public void setUserPortfolio(Set<UserPortfolio> userPortfolio) {
		this.userPortfolio = userPortfolio;
	}

	public void addStockToPortfoliop(UserPortfolio userPortfolio) {
        this.userPortfolio.add(userPortfolio);
    }
	
	@Override
	public String toString() {
		return "User [userId=" + userId + ",name="+firstName+ ", userEmail=" + userEmail  + "]";
	}

}
