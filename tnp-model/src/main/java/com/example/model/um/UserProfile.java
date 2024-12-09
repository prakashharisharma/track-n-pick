package com.example.model.um;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.example.model.master.Stock;
import com.example.model.stocks.UserPortfolio;

@Entity
@Table(name = "USERS")
public class UserProfile implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8670137481092592835L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "USER_ID")
	long userId;
	
	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = false)
	private UserDetails userDetails;
	
	@Column(name = "USER_EMAIL")
	String userEmail;
	
	@Column(name = "FIRST_NAME")
	String firstName;
	
	@Column(name = "LAST_NAME")
	String lastName;
	
	@Column(name = "IS_ACTIVE")
	boolean active;
	
	@Column(name = "SUBSCRIBE_PORTFOLOIO")
	boolean subscribePortfolio = false;
	
	@Column(name = "SUBSCRIBE_RESEARCH")
	boolean subscribeResearch = false;
	
	@Column(name = "SUBSCRIBE_CURRENT_UNDERVALUE")
	boolean subscribeCurrentUndervalue = false;
	
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

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
	private Set<Role> roles = new HashSet<Role>(0);

	public UserProfile() {
		super();
	}

	public UserProfile(String userEmail, String firstName, String lastName) {
		super();
		this.userEmail = userEmail;
		this.firstName = firstName;
		this.lastName = lastName;
	}

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

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
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

	public UserDetails getUserDetails() {
		return userDetails;
	}

	public void setUserDetails(UserDetails userDetails) {
		this.userDetails = userDetails;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	
	public boolean isSubscribePortfolio() {
		return subscribePortfolio;
	}

	public void setSubscribePortfolio(boolean subscribePortfolio) {
		this.subscribePortfolio = subscribePortfolio;
	}

	public boolean isSubscribeResearch() {
		return subscribeResearch;
	}

	public void setSubscribeResearch(boolean subscribeResearch) {
		this.subscribeResearch = subscribeResearch;
	}

	public boolean isSubscribeCurrentUndervalue() {
		return subscribeCurrentUndervalue;
	}

	public void setSubscribeCurrentUndervalue(boolean subscribeCurrentUndervalue) {
		this.subscribeCurrentUndervalue = subscribeCurrentUndervalue;
	}

	@Override
	public String toString() {
		return "User [userId=" + userId + ",name="+firstName+ ", userEmail=" + userEmail  + "]";
	}

}
