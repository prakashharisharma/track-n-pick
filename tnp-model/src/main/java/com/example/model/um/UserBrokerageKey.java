package com.example.model.um;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class UserBrokerageKey implements Serializable{

	private static final long serialVersionUID = -2018505779781526348L;

	@Column(name = "USER_ID", nullable = false)
    private long userId;

    @Column(name = "BROKER_ID", nullable = false)
    private long brokerId;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (brokerId ^ (brokerId >>> 32));
		result = prime * result + (int) (userId ^ (userId >>> 32));
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
		if (brokerId != other.brokerId)
			return false;
		if (userId != other.userId)
			return false;
		return true;
	}
    
    
    
}
