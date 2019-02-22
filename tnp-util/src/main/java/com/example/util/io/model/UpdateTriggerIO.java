package com.example.util.io.model;

import java.io.Serializable;

public class UpdateTriggerIO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7457882787830533577L;
	private TriggerType trigger;

	public UpdateTriggerIO(TriggerType trigger) {
		super();
		this.trigger = trigger;
	}

	public TriggerType getTrigger() {
		return trigger;
	}

	public void setTrigger(TriggerType trigger) {
		this.trigger = trigger;
	}

	@Override
	public String toString() {
		return "UpdateTriggerIO [trigger=" + trigger + "]";
	}
	
	
	
}
