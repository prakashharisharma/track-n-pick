package com.example.storage.model;

public class RSI {

	private Double rs;
	
	private Double rsi;
	
	private Double smoothedRs;
	
	private Double smoothedRsi;

	private Double avgGain;
	
	private Double avgLoss;
	
	public RSI() {
		super();
		
	}

	public RSI(Double rs, Double rsi, Double smoothedRs, Double smoothedRsi,Double avgGain,Double avgLoss ) {
		super();
		this.rs = rs != null ?rs : 0.00;
		this.rsi = rsi != null ? rsi : 0.00;
		this.smoothedRs = smoothedRs != null ?smoothedRs :0.00;
		this.smoothedRsi = smoothedRsi != null ? smoothedRsi : 0.00;
		this.avgGain = avgGain != null ? avgGain :0.00;
		this.avgLoss = avgLoss != null ? avgLoss : 0.00;
	}

	public Double getRs() {
		return rs;
	}

	public void setRs(Double rs) {
		this.rs = rs;
	}

	public Double getRsi() {
		return rsi;
	}

	public void setRsi(Double rsi) {
		this.rsi = rsi;
	}

	public Double getSmoothedRs() {
		return smoothedRs;
	}

	public void setSmoothedRs(Double smoothedRs) {
		this.smoothedRs = smoothedRs;
	}

	public Double getSmoothedRsi() {
		return smoothedRsi;
	}

	public void setSmoothedRsi(Double smoothedRsi) {
		this.smoothedRsi = smoothedRsi;
	}

	public Double getAvgGain() {
		return avgGain;
	}

	public void setAvgGain(Double avgGain) {
		this.avgGain = avgGain;
	}

	public Double getAvgLoss() {
		return avgLoss;
	}

	public void setAvgLoss(Double avgLoss) {
		this.avgLoss = avgLoss;
	}

	@Override
	public String toString() {
		return "RSI [rs=" + rs + ", rsi=" + rsi + ", smoothedRs=" + smoothedRs + ", smoothedRsi=" + smoothedRsi
				+ ", avgGain=" + avgGain + ", avgLoss=" + avgLoss + "]";
	}

	
}
