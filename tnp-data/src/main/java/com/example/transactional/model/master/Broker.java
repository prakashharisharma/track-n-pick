package com.example.transactional.model.master;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "BROKER_MASTER")
public class Broker {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "BROKER_ID")
	long brokerId;
	
	@Column(name = "BROKER_NAME")
	String brokerName;
	/*
	@OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<User> users = new HashSet<>();
*/
	public long getBrokerId() {
		return brokerId;
	}

	public void setBrokerId(long brokerId) {
		this.brokerId = brokerId;
	}

	public String getBrokerName() {
		return brokerName;
	}

	public void setBrokerName(String brokerName) {
		this.brokerName = brokerName;
	}

	/*public Set<User> getUsers() {
		return users;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}
	*/
	
}
