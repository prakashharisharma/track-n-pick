package com.example.external.dhan.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FundLimit {
    @JsonProperty("dhanClientId")
    private String dhanClientId;

    @JsonProperty("availabelBalance")
    private Double availabelBalance;

    @JsonProperty("sodLimit")
    private Double sodLimit;

    @JsonProperty("collateralAmount")
    private Double collateralAmount;

    @JsonProperty("receiveableAmount")
    private Double receiveableAmount;

    @JsonProperty("utilizedAmount")
    private Double utilizedAmount;

    @JsonProperty("blockedPayoutAmount")
    private Double blockedPayoutAmount;

    @JsonProperty("withdrawableBalance")
    private Double withdrawableBalance;
}
