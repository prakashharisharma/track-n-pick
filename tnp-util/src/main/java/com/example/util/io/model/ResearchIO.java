package com.example.util.io.model;

import java.io.Serializable;

public class ResearchIO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2062220561400715373L;

	String nseSymbol;
	
	ResearchType researchType;
	
	ResearchTrigger researchTrigger;

	public ResearchIO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ResearchIO(String nseSymbol, ResearchType researchType, ResearchTrigger researchTrigger) {
		super();
		this.nseSymbol = nseSymbol;
		this.researchType = researchType;
		this.researchTrigger = researchTrigger;
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

	@Override
	public String toString() {
		return "ResearchIO [nseSymbol=" + nseSymbol + ", researchType=" + researchType + ", researchTrigger="
				+ researchTrigger + "]";
	}

	
	
}
