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
	
	private double pricegt100;
	private double pricelt100;
	
	private double pricegt250;
	private double pricelt250;
	
	private double pricegt500;
	private double pricelt500;
	
	private double mcap;
	private double debtEquity;
	private double debtEquity100;
	private double debtEquity250;
	private double debtEquity500;
	private double debtEquityTweaked;
	
	private double dividend;
	private double roe;
	private double roce;
	
	private double roe100;
	private double roce100;
	
	private double roe250;
	private double roce250;
	
	private double roe500;
	private double roce500;
	
	private double roeTweaked;
	private double roceTweaked;
	
	private double currentRatio;
	private double currentRatio100;
	private double currentRatio250;
	private double currentRatio500;
	private double quickRatioBanks;

	private double currentRatioTweaked;
	private double quickRatioBanksTweaked;
	
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

	public double getDebtEquityTweaked() {
		return debtEquityTweaked;
	}

	public void setDebtEquityTweaked(double debtEquityTweaked) {
		this.debtEquityTweaked = debtEquityTweaked;
	}

	public double getRoeTweaked() {
		return roeTweaked;
	}

	public void setRoeTweaked(double roeTweaked) {
		this.roeTweaked = roeTweaked;
	}

	public double getRoceTweaked() {
		return roceTweaked;
	}

	public void setRoceTweaked(double roceTweaked) {
		this.roceTweaked = roceTweaked;
	}

	public double getCurrentRatioTweaked() {
		return currentRatioTweaked;
	}

	public void setCurrentRatioTweaked(double currentRatioTweaked) {
		this.currentRatioTweaked = currentRatioTweaked;
	}

	public double getQuickRatioBanksTweaked() {
		return quickRatioBanksTweaked;
	}

	public void setQuickRatioBanksTweaked(double quickRatioBanksTweaked) {
		this.quickRatioBanksTweaked = quickRatioBanksTweaked;
	}

	public double getPricegt100() {
		return pricegt100;
	}

	public void setPricegt100(double pricegt100) {
		this.pricegt100 = pricegt100;
	}

	public double getPricelt100() {
		return pricelt100;
	}

	public void setPricelt100(double pricelt100) {
		this.pricelt100 = pricelt100;
	}

	public double getPricegt250() {
		return pricegt250;
	}

	public void setPricegt250(double pricegt250) {
		this.pricegt250 = pricegt250;
	}

	public double getPricelt250() {
		return pricelt250;
	}

	public void setPricelt250(double pricelt250) {
		this.pricelt250 = pricelt250;
	}

	public double getPricegt500() {
		return pricegt500;
	}

	public void setPricegt500(double pricegt500) {
		this.pricegt500 = pricegt500;
	}

	public double getPricelt500() {
		return pricelt500;
	}

	public void setPricelt500(double pricelt500) {
		this.pricelt500 = pricelt500;
	}

	public double getDebtEquity100() {
		return debtEquity100;
	}

	public void setDebtEquity100(double debtEquity100) {
		this.debtEquity100 = debtEquity100;
	}

	public double getDebtEquity250() {
		return debtEquity250;
	}

	public void setDebtEquity250(double debtEquity250) {
		this.debtEquity250 = debtEquity250;
	}

	public double getDebtEquity500() {
		return debtEquity500;
	}

	public void setDebtEquity500(double debtEquity500) {
		this.debtEquity500 = debtEquity500;
	}

	public double getRoe100() {
		return roe100;
	}

	public void setRoe100(double roe100) {
		this.roe100 = roe100;
	}

	public double getRoce100() {
		return roce100;
	}

	public void setRoce100(double roce100) {
		this.roce100 = roce100;
	}

	public double getRoe250() {
		return roe250;
	}

	public void setRoe250(double roe250) {
		this.roe250 = roe250;
	}

	public double getRoce250() {
		return roce250;
	}

	public void setRoce250(double roce250) {
		this.roce250 = roce250;
	}

	public double getRoe500() {
		return roe500;
	}

	public void setRoe500(double roe500) {
		this.roe500 = roe500;
	}

	public double getRoce500() {
		return roce500;
	}

	public void setRoce500(double roce500) {
		this.roce500 = roce500;
	}

	public double getCurrentRatio100() {
		return currentRatio100;
	}

	public void setCurrentRatio100(double currentRatio100) {
		this.currentRatio100 = currentRatio100;
	}

	public double getCurrentRatio250() {
		return currentRatio250;
	}

	public void setCurrentRatio250(double currentRatio250) {
		this.currentRatio250 = currentRatio250;
	}

	public double getCurrentRatio500() {
		return currentRatio500;
	}

	public void setCurrentRatio500(double currentRatio500) {
		this.currentRatio500 = currentRatio500;
	}

}
