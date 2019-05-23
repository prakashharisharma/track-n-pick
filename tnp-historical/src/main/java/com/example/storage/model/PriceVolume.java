package com.example.storage.model;

public class PriceVolume {

	private Long obv;

	private Double roc;

	public PriceVolume(Long obv,Double roc) {
		super();
		
		
		if (obv != null) {
			this.obv = obv;	
		}else {
			this.obv = 0l;
		}
		if(roc != null) {
			this.roc = roc;
		}else {
			this.roc = 0.00;
		}
		
	}

	public Long getObv() {
		return obv;
	}

	public void setObv(Long obv) {
		this.obv = obv;
	}

	public Double getRoc() {
		return roc;
	}

	public void setRoc(Double roc) {
		this.roc = roc;
	}

	@Override
	public String toString() {
		return "PriceVolume [obv=" + obv + ", roc=" + roc + "]";
	}

}
