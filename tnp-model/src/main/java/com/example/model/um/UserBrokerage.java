package com.example.model.um;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Entity
@Table(name = "USER_BROKERAGE")
@AssociationOverrides({ @AssociationOverride(name = "brokerageId.user", joinColumns = @JoinColumn(name = "USER_ID")),
		@AssociationOverride(name = "brokerageId.broker", joinColumns = @JoinColumn(name = "BROKER_ID")) })
public class UserBrokerage {

	@EmbeddedId
	UserBrokerageKey brokerageId = new UserBrokerageKey();

	@Column(name = "DELIVERY_CHARGE", columnDefinition="Decimal(10,4) default '0.00'")
	double deliveryCharge;

	@Column(name = "DP_ID")
	String dpId;

	@Column(name = "CLIENT_ID")
	String clientId;

	@Column(name = "IS_ACTIVE")
	boolean active;
	
	public UserBrokerageKey getBrokerageId() {
		return brokerageId;
	}

	public void setBrokerageId(UserBrokerageKey brokerageId) {
		this.brokerageId = brokerageId;
	}

	public double getDeliveryCharge() {
		return deliveryCharge;
	}

	public void setDeliveryCharge(double deliveryCharge) {
		this.deliveryCharge = deliveryCharge;
	}

	public String getDpId() {
		return dpId;
	}

	public void setDpId(String dpId) {
		this.dpId = dpId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return "UserBrokerage [brokerageId=" + brokerageId + ", deliveryCharge=" + deliveryCharge + ", dpId=" + dpId
				+ ", clientId=" + clientId + "]";
	}

}
