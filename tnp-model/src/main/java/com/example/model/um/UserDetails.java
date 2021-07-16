package com.example.model.um;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "USER_DETAILS")
public class UserDetails implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4074709849715045209L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "USER_DETAILS_ID")
	long userDetailsId;
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID", referencedColumnName ="USER_ID",  nullable = false)
	UserProfile user;
	
	@Column(name = "USER_NAME", unique=true)
	String userName;
	
	@Column(name = "PASSWORD")
	String password;
	
	@Column(name = "ENABLED")
	boolean enabled = true;
	
	@Column(name = "ACCOUNT_EXPIRED")
	boolean accountExpired = false;
	
	@Column(name = "PASSWORD_EXPIRED")
	boolean credentialsExpired = false;
	
	@Column(name = "ACCOUNT_LOCKED")
	boolean accountLocked = false;

	public long getUserDetailsId() {
		return userDetailsId;
	}

	public void setUserDetailsId(long userDetailsId) {
		this.userDetailsId = userDetailsId;
	}

	public UserProfile getUser() {
		return user;
	}

	public void setUser(UserProfile user) {
		this.user = user;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isAccountExpired() {
		return accountExpired;
	}

	public void setAccountExpired(boolean accountExpired) {
		this.accountExpired = accountExpired;
	}

	public boolean isCredentialsExpired() {
		return credentialsExpired;
	}

	public void setCredentialsExpired(boolean credentialsExpired) {
		this.credentialsExpired = credentialsExpired;
	}

	public boolean isAccountLocked() {
		return accountLocked;
	}

	public void setAccountLocked(boolean accountLocked) {
		this.accountLocked = accountLocked;
	}

}
