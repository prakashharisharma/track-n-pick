package com.example.model.um;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "USER_BROKERAGE")
public class UserBrokerage {

	@EmbeddedId
	UserBrokerageKey brokerageId;
	
	@Column(name = "DELIVERY_CHARGE")
	double deliveryCharge;
	
	@Column(name = "DP_ID")
	String dpId;
	
	@Column(name = "CLIENT_ID")
	String clientId;
	
}
