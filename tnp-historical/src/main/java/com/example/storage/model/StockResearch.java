package com.example.storage.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.util.io.model.ResearchIO.ResearchTrigger;
import com.example.util.io.model.ResearchIO.ResearchType;



@Document(collection = "research_history")
public class StockResearch {

	@Id
	private String id;
	String nseSymbol;
	
	ResearchType researchType;
	
	ResearchTrigger researchTrigger;
	
	double researchPrice;
	
	double pe;
	
	double pb;
	
	Instant researchDate = Instant.now();

	public StockResearch() {
		super();
		
	}


	public StockResearch(String nseSymbol, ResearchType researchType, ResearchTrigger researchTrigger,
			double researchPrice, double pe, double pb, Instant researchDate) {
		super();
		this.nseSymbol = nseSymbol;
		this.researchType = researchType;
		this.researchTrigger = researchTrigger;
		this.researchPrice = researchPrice;
		this.pe = pe;
		this.pb = pb;
		this.researchDate = researchDate;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public ResearchTrigger getResearchStatus() {
		return researchTrigger;
	}

	public void setResearchStatus(ResearchTrigger researchStatus) {
		this.researchTrigger = researchStatus;
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
	
}
