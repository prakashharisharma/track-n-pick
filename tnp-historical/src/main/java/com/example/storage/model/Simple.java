package com.example.storage.model;

public class Simple {

	private Double avg50;
	private Double avg100;
	private Double avg200;

	public Simple(Double avg50, Double avg100, Double avg200) {
		super();
		this.avg50 = avg50 != null ? avg50 : 0.00;
		this.avg100 = avg100 != null ? avg100 : 0.00;
		this.avg200 = avg200 != null ? avg200 : 0.00;
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
		return "Simple [avg50=" + avg50 + ", avg100=" + avg100 + ", avg200=" + avg200 + "]";
	}

}
