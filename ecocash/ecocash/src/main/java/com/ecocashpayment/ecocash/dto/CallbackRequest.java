package com.ecocashpayment.ecocash.dto;

public record CallbackRequest(
        String transactionOperationStatus,
        String clientReference,
        String ecocashReference
) {}
