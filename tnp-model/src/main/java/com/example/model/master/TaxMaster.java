package com.example.model.master;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TAX_MASTER")
public class TaxMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "TAX_MASTER_ID")
	long taxMasterId;
	
	@Column(name = "SECURITY_TXN_TAX")
	double securityTxnTax;
	
	@Column(name = "STAMP_DUTY")
	double stampDuty;
	
	@Column(name = "NSE_TXN_CHARGE")
	double nseTransactionCharge;
	
	@Column(name = "BSE_TXN_CHARGE")
	double bseTransactionCharge;
	
	@Column(name = "SEBI_TURNOVER_FEE")
	double sebiTurnoverFee;
	
	@Column(name = "GST")
	double gst;
	
	@Column(name = "EFFECTIVE_DATE")
	LocalDate effectiveDate = LocalDate.now();
	
	@Column(name = "EXPIRY_DATE")
	LocalDate expiryDate = LocalDate.now().plusYears(1);
	
	@Column(name = "IS_ACTIVE")
	boolean active = true;

	public long getTaxMasterId() {
		return taxMasterId;
	}

	public void setTaxMasterId(long taxMasterId) {
		this.taxMasterId = taxMasterId;
	}

	public double getSecurityTxnTax() {
		return securityTxnTax;
	}

	public void setSecurityTxnTax(double securityTxnTax) {
		this.securityTxnTax = securityTxnTax;
	}

	public double getStampDuty() {
		return stampDuty;
	}

	public void setStampDuty(double stampDuty) {
		this.stampDuty = stampDuty;
	}

	public double getNseTransactionCharge() {
		return nseTransactionCharge;
	}

	public void setNseTransactionCharge(double nseTransactionCharge) {
		this.nseTransactionCharge = nseTransactionCharge;
	}

	public double getBseTransactionCharge() {
		return bseTransactionCharge;
	}

	public void setBseTransactionCharge(double bseTransactionCharge) {
		this.bseTransactionCharge = bseTransactionCharge;
	}

	public double getSebiTurnoverFee() {
		return sebiTurnoverFee;
	}

	public void setSebiTurnoverFee(double sebiTurnoverFee) {
		this.sebiTurnoverFee = sebiTurnoverFee;
	}

	public double getGst() {
		return gst;
	}

	public void setGst(double gst) {
		this.gst = gst;
	}

	public LocalDate getEffectiveDate() {
		return LocalDate.now();
	}

	public void setEffectiveDate(LocalDate effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public LocalDate getExpiryDate() {
		return LocalDate.now();
	}

	public void setExpiryDate(LocalDate expiryDate) {
		this.expiryDate = expiryDate;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return "TaxMaster [taxMasterId=" + taxMasterId + ", securityTxnTax=" + securityTxnTax + ", stampDuty="
				+ stampDuty + ", nseTransactionCharge=" + nseTransactionCharge + ", bseTransactionCharge="
				+ bseTransactionCharge + ", sebiTurnoverFee=" + sebiTurnoverFee + ", gst=" + gst + ", effectiveDate="
				+ effectiveDate + ", expiryDate=" + expiryDate + ", active=" + active + "]";
	}
	
	
}
