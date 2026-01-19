package com.ecocashpayment.ecocash.dto;

import java.math.BigDecimal;

public record RefundResponse(
        String sourceReference,
        String transactionEndTime,
        String callbackUrl,
        String destinationReferenceCode,
        String sourceMobileNumber,
        String transactionStatus,
        BigDecimal amount,
        String destinationEcocashReference,
        String clientMerchantCode,
        String clientMerchantNumber,
        String clienttransactionDate,
        String description,
        String responseMessage,
        String currency,
        BigDecimal paymentAmount,
        String ecocashReference,
        String transactionstartTime
) {}
