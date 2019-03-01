package com.example.model.ledger;

import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.example.util.io.model.DownloadTriggerIO.DownloadType;


@Entity
@Table(name = "DOWNLOAD_LEDGER")
public class DownloadLedger  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6577669318519157750L;
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "DOWNLOAD_ID")
	long downloadId;
	
	@Column(name = "DOWNLOAD_DATE")
	LocalDate downloadDate = LocalDate.now();
	
	@Enumerated(EnumType.STRING)
	DownloadType downloadType;

	public DownloadLedger() {
		super();
		// TODO Auto-generated constructor stub
	}

	public DownloadLedger(LocalDate downloadDate, DownloadType downloadType) {
		super();
		this.downloadDate = downloadDate;
		this.downloadType = downloadType;
	}

	public long getDownloadId() {
		return downloadId;
	}

	public void setDownloadId(long downloadId) {
		this.downloadId = downloadId;
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
		return "DownloadLedger [downloadId=" + downloadId + ", downloadDate=" + downloadDate + ", downloadType="
				+ downloadType + "]";
	}
	
	
}
