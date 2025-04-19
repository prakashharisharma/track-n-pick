package com.example.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.data.transactional.entities.type.FundTransactionType;
import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class FundsLedgerRequest {
    @NotNull
    private BigDecimal amount;

    @NotBlank
    private String paymentMode;

    @NotNull
    private LocalDate transactionDate;
}
