package com.example.model.type;

import java.io.Serializable;


public class IndicedAllocation implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8498709811598328707L;
	private IndiceType indice;
	private Double allocation;
	public IndicedAllocation() {
		super();
		// TODO Auto-generated constructor stub
	}
	public IndicedAllocation(IndiceType indice, Double allocation) {
		super();
		this.indice = indice;
		this.allocation = allocation;
	}
	public IndiceType getIndice() {
		return indice;
	}
	public void setIndice(IndiceType indice) {
		this.indice = indice;
	}
	public Double getAllocation() {
		return allocation;
	}
	public void setAllocation(Double allocation) {
		this.allocation = allocation;
	}
	
	
}
