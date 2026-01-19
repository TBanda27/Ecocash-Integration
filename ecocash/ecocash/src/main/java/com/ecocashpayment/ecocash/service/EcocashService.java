package com.ecocashpayment.ecocash.service;

import com.ecocashpayment.ecocash.config.EcocashProperties;
import com.ecocashpayment.ecocash.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class EcocashService {

    private final WebClient ecocashWebClient;
    private final EcocashProperties ecocashProperties;

    public Mono<PaymentResponse> initiatePayment(PaymentRequest request) {
        log.info("Initiating payment for customer: {}, amount: {} {}",
                request.customerMsisdn(), request.amount(), request.currency());

        return ecocashWebClient.post()
                .uri(ecocashProperties.getPaymentEndpoint())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .doOnSuccess(response -> log.info("Payment initiated successfully. Reference: {}",
                        response.ecocashReference()))
                .doOnError(error -> log.error("Payment initiation failed: {}", error.getMessage()));
    }

    public Mono<RefundResponse> initiateRefund(RefundRequest request) {
        log.info("Initiating refund for transaction: {}, amount: {} {}",
                request.origionalEcocashTransactionReference(), request.amount(), request.currency());

        return ecocashWebClient.post()
                .uri(ecocashProperties.getRefundEndpoint())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RefundResponse.class)
                .doOnSuccess(response -> log.info("Refund initiated successfully. Reference: {}",
                        response.ecocashReference()))
                .doOnError(error -> log.error("Refund initiation failed: {}", error.getMessage()));
    }

    public Mono<TransactionLookupResponse> lookupTransaction(TransactionLookupRequest request) {
        log.info("Looking up transaction: {} for mobile: {}",
                request.sourceReference(), request.sourceMobileNumber());

        return ecocashWebClient.post()
                .uri(ecocashProperties.getTransactionLookupEndpoint())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TransactionLookupResponse.class)
                .doOnSuccess(response -> log.info("Transaction lookup successful. Status: {}",
                        response.status()))
                .doOnError(error -> log.error("Transaction lookup failed: {}", error.getMessage()));
    }
}
