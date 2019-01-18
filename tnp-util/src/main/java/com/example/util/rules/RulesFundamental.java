package com.example.util.rules;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "stock.filter")
@PropertySource("classpath:config/rules/fundamental_rules.properties")
public class RulesFundamental {

	private double price;
	private double mcap;
	private double debtEquity;
	private double dividend;
	private double roe;
	private double roce;

	
	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
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

}
