package com.example.dto.io;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BseSectorListResponse {

    private String securityCode;
    private String securityName;
    private String ltp; // Last Traded Price
    private String percentChange;
    private String timestamp;

    // Constructor
    public BseSectorListResponse(
            String securityCode,
            String securityName,
            String ltp,
            String percentChange,
            String timestamp) {
        this.securityCode = securityCode;
        this.securityName = securityName;
        this.ltp = ltp;
        this.percentChange = percentChange;
        this.timestamp = timestamp;
    }

    // Getters
    public String getSecurityCode() {
        return securityCode;
    }

    public String getSecurityName() {
        return securityName;
    }

    public String getLtp() {
        return ltp;
    }

    public String getPercentChange() {
        return percentChange;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Security{"
                + "securityCode="
                + securityCode
                + ", securityName='"
                + securityName
                + '\''
                + ", ltp="
                + ltp
                + ", percentChange="
                + percentChange
                + ", timestamp="
                + timestamp
                + '}';
    }

    // Utility Method to Parse Date
    public static LocalDateTime parseDate(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy | hh:mm a");
        return LocalDateTime.parse(dateString, formatter);
    }
}
