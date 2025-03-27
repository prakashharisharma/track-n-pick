package com.example.ui.model;

public class UINotification {

	
	String notificationType;

	public UINotification() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UINotification(String notificationType) {
		super();
		this.notificationType = notificationType;
	}

	public String getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(String notificationType) {
		this.notificationType = notificationType;
	}

	@Override
	public String toString() {
		return "UiNotification [notificationType=" + notificationType + "]";
	}
	
	
}
