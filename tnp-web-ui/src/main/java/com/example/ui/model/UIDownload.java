package com.example.ui.model;

public class UIDownload {

	String downloadDate;
	
	String downloadType;

	public UIDownload() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UIDownload(String downloadDate, String downloadType) {
		super();
		this.downloadDate = downloadDate;
		this.downloadType = downloadType;
	}

	public String getDownloadDate() {
		return downloadDate;
	}

	public void setDownloadDate(String downloadDate) {
		this.downloadDate = downloadDate;
	}

	public String getDownloadType() {
		return downloadType;
	}

	public void setDownloadType(String downloadType) {
		this.downloadType = downloadType;
	}

	@Override
	public String toString() {
		return "UIDownload [downloadDate=" + downloadDate + ", downloadType=" + downloadType + "]";
	}
	

	
	
}
