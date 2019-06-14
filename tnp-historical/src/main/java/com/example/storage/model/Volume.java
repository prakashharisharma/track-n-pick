package com.example.storage.model;

public class Volume {

	private Long obv;

	private Double roc;

	private Long volume;

	private Double voumeChange;

	private Long avgVolume10;

	public Volume(Long obv, Double roc, Long volume, Double voumeChange, Long avgVolume10) {
		super();
		this.obv = obv != null ? obv : 1;
		this.roc = roc != null ? roc : 0.00;
		this.volume = volume != null ? volume : 0l;
		this.voumeChange = voumeChange !=null ? voumeChange : 0.00 ;
		this.avgVolume10 = avgVolume10 != null ? avgVolume10 : 0l;
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

	public Long getVolume() {
		return volume;
	}

	public void setVolume(Long volume) {
		this.volume = volume;
	}

	public Double getVoumeChange() {
		return voumeChange;
	}

	public void setVoumeChange(Double voumeChange) {
		this.voumeChange = voumeChange;
	}

	public Long getAvgVolume10() {
		return avgVolume10;
	}

	public void setAvgVolume10(Long avgVolume10) {
		this.avgVolume10 = avgVolume10;
	}

	@Override
	public String toString() {
		return "PriceVolume [obv=" + obv + ", roc=" + roc + ", volume=" + volume + ", voumeChange=" + voumeChange
				+ ", avgVolume10=" + avgVolume10 + "]";
	}

}
