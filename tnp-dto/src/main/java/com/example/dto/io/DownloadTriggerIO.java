package com.example.dto.io;

import java.io.Serializable;
import java.time.LocalDate;

public class DownloadTriggerIO implements Serializable{

	public enum DownloadType{
		BHAV, NIFTY50, NIFTY100, NIFTY250, NIFTY500
	}
	
	public enum TriggerType{
		SYSTEM, MANUAL
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2613190007704749698L;

	private LocalDate downloadDate;
	
	private DownloadType downloadType;
	
	private TriggerType triggerType;
	

	public DownloadTriggerIO(LocalDate downloadDate, DownloadType downloadType,TriggerType triggerType) {
		super();
		this.downloadDate = downloadDate;
		this.downloadType = downloadType;
		this.triggerType = triggerType;
		
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

	public TriggerType getTriggerType() {
		return triggerType;
	}

	public void setTriggerType(TriggerType triggerType) {
		this.triggerType = triggerType;
	}

	@Override
	public String toString() {
		return "DownloadTriggerIO [downloadDate=" + downloadDate + ", downloadType=" + downloadType + "]";
	}

}
