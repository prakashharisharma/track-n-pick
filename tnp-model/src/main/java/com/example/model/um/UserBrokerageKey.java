package com.example.model.um;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import com.example.model.master.Broker;

@Embeddable
public class UserBrokerageKey implements Serializable{

	private static final long serialVersionUID = -2018505779781526348L;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	UserProfile user;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	Broker broker;

	public UserProfile getUser() {
		return user;
	}

	public void setUser(UserProfile user) {
		this.user = user;
	}

	public Broker getBroker() {
		return broker;
	}

	public void setBroker(Broker broker) {
		this.broker = broker;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((broker == null) ? 0 : broker.hashCode());
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
		UserBrokerageKey other = (UserBrokerageKey) obj;
		if (broker == null) {
			if (other.broker != null)
				return false;
		} else if (!broker.equals(other.broker))
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
		return "UserBrokerageKey [user=" + user + ", broker=" + broker + "]";
	}
    
}
