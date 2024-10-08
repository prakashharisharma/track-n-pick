package com.example.model.um;

import javax.persistence.*;

/**
 *
 * BrokerageType FIXED
 * 	FLAT - Per Order / PERCENTAGE - Of total transaction value
 *
 * BrokerageType MIN - Minimum of
 * 	FLAT - Per Order / PERCENTAGE - Of total transaction value
 *
 */
@Entity
@Table(name = "USER_BROKERAGE")
@AssociationOverrides({ @AssociationOverride(name = "brokerageId.user", joinColumns = @JoinColumn(name = "USER_ID")),
		@AssociationOverride(name = "brokerageId.broker", joinColumns = @JoinColumn(name = "BROKER_ID")) })
public class UserBrokerage {

	public enum Type {FIXED, MIN};

	public enum Calculation {FLAT, PERCENTAGE};

	@EmbeddedId
	UserBrokerageKey brokerageId = new UserBrokerageKey();

	@Column(name = "BROKERAGE_TYPE")
	@Enumerated(EnumType.STRING)
	Type type;

	@Column(name = "BROKERAGE_CALCULATION")
	@Enumerated(EnumType.STRING)
	Calculation calculation;

	@Column(name = "FLAT_CHARGE", columnDefinition="Decimal(10,5) default '0.00'")
	double flatCharge;

	@Column(name = "PERCENTAGE_CHARGE", columnDefinition="Decimal(10,5) default '0.00'")
	double percentageCharge;

	@Column(name = "DP_CHARGE", columnDefinition="Decimal(10,5) default '0.00'")
	double dpCharge;

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

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Calculation getCalculation() {
		return calculation;
	}

	public void setCalculation(Calculation calculation) {
		this.calculation = calculation;
	}

	public double getFlatCharge() {
		return flatCharge;
	}

	public void setFlatCharge(double flatCharge) {
		this.flatCharge = flatCharge;
	}

	public double getPercentageCharge() {
		return percentageCharge;
	}

	public void setPercentageCharge(double percentageCharge) {
		this.percentageCharge = percentageCharge;
	}

	public double getDpCharge() {
		return dpCharge;
	}

	public void setDpCharge(double dpCharge) {
		this.dpCharge = dpCharge;
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
		return "UserBrokerage{" +
				"brokerageId=" + brokerageId +
				", type=" + type +
				", calculation=" + calculation +
				", flatCharge=" + flatCharge +
				", percentageCharge=" + percentageCharge +
				", dpId='" + dpId + '\'' +
				", clientId='" + clientId + '\'' +
				", active=" + active +
				'}';
	}
}
