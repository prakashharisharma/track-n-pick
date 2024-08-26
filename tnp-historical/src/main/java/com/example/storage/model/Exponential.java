package com.example.storage.model;

public class Exponential {

	private Double avg5;

	private Double avg10;

	private Double avg12;
	private Double avg20;

	private Double avg26;
	private Double avg50;
	private Double avg100;
	private Double avg200;

	public Exponential(Double avg5, Double avg10, Double avg20,Double avg50, Double avg100, Double avg200) {
		super();
		this.avg5 = avg5 != null ? avg5 : 0.00;
		this.avg10 = avg10 != null ? avg10 : 0.00;
		this.avg20 = avg20 != null ? avg20 : 0.00;
		this.avg50 = avg50 != null ? avg50 : 0.00;
		this.avg100 = avg100 != null ? avg100 : 0.00;
		this.avg200 = avg200 != null ? avg200 : 0.00;
	}

	public Double getAvg5() {
		return avg5;
	}

	public void setAvg5(Double avg5) {
		this.avg5 = avg5;
	}

	public Double getAvg10() {
		return avg10;
	}

	public void setAvg10(Double avg10) {
		this.avg10 = avg10;
	}

	public Double getAvg20() {
		return avg20;
	}


	public Double getAvg12() {
		return avg12;
	}

	public void setAvg12(Double avg12) {
		this.avg12 = avg12;
	}

	public Double getAvg26() {
		return avg26;
	}

	public void setAvg26(Double avg26) {
		this.avg26 = avg26;
	}

	public void setAvg20(Double avg20) {
		this.avg20 = avg20;
	}

	public Double getAvg50() {
		return avg50;
	}

	public void setAvg50(Double avg50) {
		this.avg50 = avg50;
	}

	public Double getAvg100() {
		return avg100;
	}

	public void setAvg100(Double avg100) {
		this.avg100 = avg100;
	}

	public Double getAvg200() {
		return avg200;
	}

	public void setAvg200(Double avg200) {
		this.avg200 = avg200;
	}

	@Override
	public String toString() {
		return "Exponential [avg50=" + avg50 + ", avg100=" + avg100 + ", avg200=" + avg200 + "]";
	}

}
