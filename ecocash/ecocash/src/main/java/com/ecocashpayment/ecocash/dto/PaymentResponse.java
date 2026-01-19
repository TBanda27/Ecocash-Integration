package com.ecocashpayment.ecocash.dto;

import java.math.BigDecimal;

public record PaymentResponse(
        String sourceReference,
        String ecocashReference,
        String transactionStatus,
        String responseMessage,
        BigDecimal amount,
        String currency,
        String customerMsisdn,
        String transactionDateTime
) {}
