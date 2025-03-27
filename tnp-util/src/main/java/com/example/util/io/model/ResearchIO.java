package com.example.util.io.model;

import com.example.util.io.model.type.Timeframe;

import java.io.Serializable;
import java.time.Instant;

public class ResearchIO implements Serializable{

	public enum ResearchTrigger{
		BUY, SELL, BUY_SELL
	}
	
	public enum ResearchType{
		
		FUNDAMENTAL, TECHNICAL
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2062220561400715373L;

	String nseSymbol;
	
	ResearchType researchType;
	
	ResearchTrigger researchTrigger;

	double researchPrice;
	
	double pe;
	
	double pb;
	
	Instant researchDate = Instant.now();
	
	public ResearchIO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ResearchIO(String nseSymbol, ResearchType researchType, ResearchTrigger researchTrigger) {
		super();
		this.nseSymbol = nseSymbol;
		this.researchType = researchType;
		this.researchTrigger = researchTrigger;
		this.researchDate = Instant.now();
	}

	public ResearchIO(String nseSymbol, ResearchType researchType, ResearchTrigger researchTrigger,
			double researchPrice, double pe, double pb) {
		super();
		this.nseSymbol = nseSymbol;
		this.researchType = researchType;
		this.researchTrigger = researchTrigger;
		this.researchPrice = researchPrice;
		this.pe = pe;
		this.pb = pb;
		this.researchDate = Instant.now();
	}

	public String getNseSymbol() {
		return nseSymbol;
	}

	public void setNseSymbol(String nseSymbol) {
		this.nseSymbol = nseSymbol;
	}

	public ResearchType getResearchType() {
		return researchType;
	}

	public void setResearchType(ResearchType researchType) {
		this.researchType = researchType;
	}

	public ResearchTrigger getResearchTrigger() {
		return researchTrigger;
	}

	public void setResearchTrigger(ResearchTrigger researchTrigger) {
		this.researchTrigger = researchTrigger;
	}

	public double getResearchPrice() {
		return researchPrice;
	}

	public void setResearchPrice(double researchPrice) {
		this.researchPrice = researchPrice;
	}

	public Instant getResearchDate() {
		return researchDate;
	}

	public void setResearchDate(Instant researchDate) {
		this.researchDate = researchDate;
	}

	public double getPe() {
		return pe;
	}

	public void setPe(double pe) {
		this.pe = pe;
	}

	public double getPb() {
		return pb;
	}

	public void setPb(double pb) {
		this.pb = pb;
	}

	@Override
	public String toString() {
		return "ResearchIO [nseSymbol=" + nseSymbol + ", researchType=" + researchType + ", researchTrigger="
				+ researchTrigger + "]";
	}

	
	
}
