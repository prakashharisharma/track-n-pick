package com.example.ui.model;

import java.io.Serializable;

public class RecentCrossOverIO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8645477969313276959L;
	String recentCrossOver;

	public RecentCrossOverIO(String recentCrossOver) {
		super();
		this.recentCrossOver = recentCrossOver;
	}

	public String getRecentCrossOver() {
		return recentCrossOver;
	}

	public void setRecentCrossOver(String recentCrossOver) {
		this.recentCrossOver = recentCrossOver;
	}
	
	
	
}
