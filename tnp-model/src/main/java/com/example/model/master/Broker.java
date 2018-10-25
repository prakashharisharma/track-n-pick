package com.example.model.master;

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
	
}
