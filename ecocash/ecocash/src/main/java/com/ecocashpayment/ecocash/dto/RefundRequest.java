package com.ecocashpayment.ecocash.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record RefundRequest(
        @NotBlank(message = "Original EcoCash transaction reference is required")
        String origionalEcocashTransactionReference,

        @NotBlank(message = "Refund correlator is required")
        String refundCorelator,

        @NotBlank(message = "Source mobile number is required")
        String sourceMobileNumber,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotBlank(message = "Client name is required")
        String clientName,

        @NotBlank(message = "Currency is required")
        String currency,

        @NotBlank(message = "Reason for refund is required")
        String reasonForRefund
) {}
