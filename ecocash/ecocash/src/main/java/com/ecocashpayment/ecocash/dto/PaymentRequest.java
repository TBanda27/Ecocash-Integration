package com.ecocashpayment.ecocash.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PaymentRequest(
        @NotBlank(message = "Customer MSISDN is required")
        String customerMsisdn,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotBlank(message = "Reason is required")
        String reason,

        @NotBlank(message = "Currency is required")
        String currency,

        @NotBlank(message = "Source reference is required")
        String sourceReference
) {}
