package com.example.util.io.model;

import java.io.Serializable;
import java.time.LocalDate;

public class DownloadTriggerIO implements Serializable{

	public enum DownloadType{
		BHAV, NIFTY50, NIFTY100, NIFTY250, NIFTY500
	}
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2613190007704749698L;

	private LocalDate downloadDate;
	
	private DownloadType downloadType;
	
	

	public DownloadTriggerIO(LocalDate downloadDate, DownloadType downloadType) {
		super();
		this.downloadDate = downloadDate;
		this.downloadType = downloadType;
		
	}

	public DownloadTriggerIO(DownloadType downloadType) {
		super();
		this.downloadDate = LocalDate.now();
		this.downloadType = downloadType;
	
	}

	public LocalDate getDownloadDate() {
		return downloadDate;
	}

	public void setDownloadDate(LocalDate downloadDate) {
		this.downloadDate = downloadDate;
	}

	public DownloadType getDownloadType() {
		return downloadType;
	}

	public void setDownloadType(DownloadType downloadType) {
		this.downloadType = downloadType;
	}

	@Override
	public String toString() {
		return "DownloadTriggerIO [downloadDate=" + downloadDate + ", downloadType=" + downloadType + "]";
	}

}
