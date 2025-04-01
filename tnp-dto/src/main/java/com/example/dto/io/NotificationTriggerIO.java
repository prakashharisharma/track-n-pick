package com.example.dto.io;

import java.io.Serializable;

public class NotificationTriggerIO implements Serializable {

    public enum TriggerType {
        PORTFOLIO,
        RESEARCH,
        CURRENT_UNDERVALUE
    }

    /** */
    private static final long serialVersionUID = -2269885119216610117L;

    private TriggerType triggerType;

    public NotificationTriggerIO(TriggerType triggerType) {
        super();
        this.triggerType = triggerType;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    @Override
    public String toString() {
        return "NotificationTriggerIO [triggerType=" + triggerType + "]";
    }
}
