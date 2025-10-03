package com.example.external.dhan.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsentResponse {
    private String consentAppId;
    private String consentAppStatus;
    private String status;

    public String getConsentAppId() {
        return consentAppId;
    }

    public void setConsentAppId(String consentAppId) {
        this.consentAppId = consentAppId;
    }

    public String getConsentAppStatus() {
        return consentAppStatus;
    }

    public void setConsentAppStatus(String consentAppStatus) {
        this.consentAppStatus = consentAppStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
