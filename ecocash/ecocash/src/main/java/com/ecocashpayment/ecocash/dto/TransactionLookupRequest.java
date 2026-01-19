package com.ecocashpayment.ecocash.dto;

import jakarta.validation.constraints.NotBlank;

public record   TransactionLookupRequest(
        @NotBlank(message = "Source mobile number is required")
        String sourceMobileNumber,

        @NotBlank(message = "Source reference is required")
        String sourceReference
) {}
