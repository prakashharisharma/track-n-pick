package com.example.util.rules;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "stock.filter")
@PropertySource("classpath:config/rules/fundamental_rules.properties")
public class RulesFundamental {

	private double pricegt;
	private double pricelt;
	private double mcap;
	private double debtEquity;
	private double dividend;
	private double roe;
	private double roce;
	private double currentRatio;
	private double quickRatioBanks;

	public double getPricegt() {
		return pricegt;
	}

	public void setPricegt(double pricegt) {
		this.pricegt = pricegt;
	}

	public double getPricelt() {
		return pricelt;
	}

	public void setPricelt(double pricelt) {
		this.pricelt = pricelt;
	}

	public double getMcap() {
		return mcap;
	}

	public void setMcap(double mcap) {
		this.mcap = mcap;
	}

	public double getDebtEquity() {
		return debtEquity;
	}

	public void setDebtEquity(double debtEquity) {
		this.debtEquity = debtEquity;
	}

	public double getDividend() {
		return dividend;
	}

	public void setDividend(double dividend) {
		this.dividend = dividend;
	}

	public double getRoe() {
		return roe;
	}

	public void setRoe(double roe) {
		this.roe = roe;
	}

	public double getRoce() {
		return roce;
	}

	public void setRoce(double roce) {
		this.roce = roce;
	}

	public double getCurrentRatio() {
		return currentRatio;
	}

	public void setCurrentRatio(double currentRatio) {
		this.currentRatio = currentRatio;
	}

	public double getQuickRatioBanks() {
		return quickRatioBanks;
	}

	public void setQuickRatioBanks(double quickRatioBanks) {
		this.quickRatioBanks = quickRatioBanks;
	}

}
