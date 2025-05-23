package com.example.dto.request;

import com.sun.istack.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FundsLedgerRequest {
    @NotNull private BigDecimal amount;

    @NotBlank private String paymentMode;

    @NotNull private LocalDate transactionDate;
}
