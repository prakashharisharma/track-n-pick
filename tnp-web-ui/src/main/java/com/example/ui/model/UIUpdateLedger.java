package com.example.ui.model;

public class UIUpdateLedger {

	String updateledgerType;

	public UIUpdateLedger() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UIUpdateLedger(String updateledgerType) {
		super();
		this.updateledgerType = updateledgerType;
	}

	public String getUpdateledgerType() {
		return updateledgerType;
	}

	public void setUpdateledgerType(String updateledgerType) {
		this.updateledgerType = updateledgerType;
	}

	@Override
	public String toString() {
		return "UIUpdateLedger [updateledgerType=" + updateledgerType + "]";
	}
	
	
}
