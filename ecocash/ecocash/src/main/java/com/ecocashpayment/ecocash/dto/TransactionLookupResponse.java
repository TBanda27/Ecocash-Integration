package com.ecocashpayment.ecocash.dto;

import java.math.BigDecimal;

public record TransactionLookupResponse(
        Amount amount,
        String customerMsisdn,
        String reference,
        String ecocashReference,
        String status,
        String transactionDateTime
) {
    public record Amount(
            BigDecimal amount,
            String currency
    ) {}
}
