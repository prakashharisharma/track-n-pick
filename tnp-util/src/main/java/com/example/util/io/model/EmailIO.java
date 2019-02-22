package com.example.util.io.model;

import java.io.Serializable;

public class EmailIO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8845370467660959749L;

	private String emailTo;
	
	private String emailSubject;
	
	private String emailBody;

	public EmailIO(String emailTo, String emailSubject, String emailBody) {
		super();
		this.emailTo = emailTo;
		this.emailSubject = emailSubject;
		this.emailBody = emailBody;
	}

	public String getEmailTo() {
		return emailTo;
	}

	public void setEmailTo(String emailTo) {
		this.emailTo = emailTo;
	}

	public String getEmailSubject() {
		return emailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	public String getEmailBody() {
		return emailBody;
	}

	public void setEmailBody(String emailBody) {
		this.emailBody = emailBody;
	}

	@Override
	public String toString() {
		return "EmailIO [emailTo=" + emailTo + ", emailSubject=" + emailSubject + ", emailBody=" + emailBody + "]";
	} 
	
	
	
}
