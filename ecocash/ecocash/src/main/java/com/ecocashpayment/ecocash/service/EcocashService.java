package com.ecocashpayment.ecocash.service;

import com.ecocashpayment.ecocash.config.EcocashProperties;
import com.ecocashpayment.ecocash.dto.*;
import com.ecocashpayment.ecocash.exception.EcocashApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class EcocashService {

    private final OkHttpClient client;
    private final EcocashProperties ecocashProperties;
    private final ObjectMapper objectMapper;
    private static final MediaType JSON = MediaType.parse("application/json");

    public EcocashService(EcocashProperties ecocashProperties, ObjectMapper objectMapper) {
        this.client = new OkHttpClient().newBuilder().build();
        this.ecocashProperties = ecocashProperties;
        this.objectMapper = objectMapper;
    }

    public PaymentResponse initiatePayment(PaymentRequest paymentRequest) {
        log.info("Initiating payment for customer: {}, amount: {} {}",
                paymentRequest.customerMsisdn(), paymentRequest.amount(), paymentRequest.currency());

        String url = ecocashProperties.getPaymentEndpoint();
        log.info("Calling endpoint: {}", url);

        try {
            String jsonBody = objectMapper.writeValueAsString(paymentRequest);
            RequestBody body = RequestBody.create(jsonBody, JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .method("POST", body)
                    .addHeader("X-API-KEY", ecocashProperties.apiKey())
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : null;
                log.info("Response code: {}, body: {}", response.code(), responseBody);

                if (!response.isSuccessful()) {
                    throw new EcocashApiException("Payment failed: " + responseBody,
                            HttpStatusCode.valueOf(response.code()), responseBody);
                }

                // EcoCash returns 200 with empty body - payment accepted, poll for result
                if (responseBody == null || responseBody.isBlank()) {
                    log.info("Payment request accepted. Polling for result...");
                    return pollForPaymentResult(paymentRequest);
                }

                PaymentResponse paymentResponse = objectMapper.readValue(responseBody, PaymentResponse.class);
                log.info("Payment initiated successfully. Reference: {}", paymentResponse.ecocashReference());
                return paymentResponse;
            }
        } catch (IOException e) {
            log.error("Payment initiation failed: {}", e.getMessage());
            throw new EcocashApiException("Payment request failed", e);
        }
    }

    public RefundResponse initiateRefund(RefundRequest refundRequest) {
        log.info("Initiating refund for transaction: {}, amount: {} {}",
                refundRequest.origionalEcocashTransactionReference(), refundRequest.amount(), refundRequest.currency());

        String url = ecocashProperties.getRefundEndpoint();
        log.info("Calling endpoint: {}", url);

        try {
            String jsonBody = objectMapper.writeValueAsString(refundRequest);
            RequestBody body = RequestBody.create(jsonBody, JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .method("POST", body)
                    .addHeader("X-API-KEY", ecocashProperties.apiKey())
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : null;
                log.info("Response code: {}, body: {}", response.code(), responseBody);

                if (!response.isSuccessful()) {
                    throw new EcocashApiException("Refund failed: " + responseBody,
                            HttpStatusCode.valueOf(response.code()), responseBody);
                }

                // Handle empty response
                if (responseBody == null || responseBody.isBlank()) {
                    log.info("Refund request accepted. Awaiting callback for result.");
                    return new RefundResponse(
                            refundRequest.origionalEcocashTransactionReference(),
                            null, null, null,
                            refundRequest.sourceMobileNumber(),
                            "PENDING",
                            refundRequest.amount(),
                            null, null, null, null, null,
                            "Refund request accepted",
                            refundRequest.currency(),
                            refundRequest.amount(),
                            null, null
                    );
                }

                RefundResponse refundResponse = objectMapper.readValue(responseBody, RefundResponse.class);
                log.info("Refund initiated successfully. Reference: {}", refundResponse.ecocashReference());
                return refundResponse;
            }
        } catch (IOException e) {
            log.error("Refund initiation failed: {}", e.getMessage());
            throw new EcocashApiException("Refund request failed", e);
        }
    }

    public TransactionLookupResponse lookupTransaction(TransactionLookupRequest lookupRequest) {
        log.info("Looking up transaction: {} for mobile: {}",
                lookupRequest.sourceReference(), lookupRequest.sourceMobileNumber());

        String url = ecocashProperties.getTransactionLookupEndpoint();
        log.info("Calling endpoint: {}", url);

        try {
            String jsonBody = objectMapper.writeValueAsString(lookupRequest);
            RequestBody body = RequestBody.create(jsonBody, JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .method("POST", body)
                    .addHeader("X-API-KEY", ecocashProperties.apiKey())
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body() != null ? response.body().string() : null;
                log.info("Response code: {}, body: {}", response.code(), responseBody);

                if (!response.isSuccessful()) {
                    throw new EcocashApiException("Transaction lookup failed: " + responseBody,
                            HttpStatusCode.valueOf(response.code()), responseBody);
                }

                // Handle empty response
                if (responseBody == null || responseBody.isBlank()) {
                    log.info("Transaction lookup returned empty response");
                    return new TransactionLookupResponse(
                            null,
                            lookupRequest.sourceMobileNumber(),
                            lookupRequest.sourceReference(),
                            null,
                            "NOT_FOUND",
                            null
                    );
                }

                TransactionLookupResponse lookupResponse = objectMapper.readValue(responseBody, TransactionLookupResponse.class);
                log.info("Transaction lookup successful. Status: {}", lookupResponse.status());
                return lookupResponse;
            }
        } catch (IOException e) {
            log.error("Transaction lookup failed: {}", e.getMessage());
            throw new EcocashApiException("Transaction lookup request failed", e);
        }
    }

    /**
     * Polls the transaction lookup endpoint until payment completes or times out.
     * Waits up to 90 seconds, checking every 3 seconds.
     */
    private PaymentResponse pollForPaymentResult(PaymentRequest paymentRequest) {
        int maxAttempts = 30;  // 30 attempts * 3 seconds = 90 seconds max wait
        int pollIntervalSeconds = 3;

        TransactionLookupRequest lookupRequest = new TransactionLookupRequest(
                paymentRequest.customerMsisdn(),
                paymentRequest.sourceReference()
        );

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                TimeUnit.SECONDS.sleep(pollIntervalSeconds);
                log.info("Polling for payment result... attempt {}/{}", attempt, maxAttempts);

                TransactionLookupResponse lookupResponse = lookupTransaction(lookupRequest);

                if (lookupResponse != null && lookupResponse.status() != null) {
                    String status = lookupResponse.status().toUpperCase();

                    if ("SUCCESS".equals(status) || "COMPLETED".equals(status)) {
                        log.info("Payment completed successfully!");
                        return new PaymentResponse(
                                paymentRequest.sourceReference(),
                                lookupResponse.ecocashReference(),
                                "SUCCESS",
                                "Payment completed successfully",
                                lookupResponse.amount() != null ? lookupResponse.amount().amount() : paymentRequest.amount(),
                                lookupResponse.amount() != null ? lookupResponse.amount().currency() : paymentRequest.currency(),
                                lookupResponse.customerMsisdn(),
                                lookupResponse.transactionDateTime()
                        );
                    } else if ("FAILED".equals(status) || "CANCELLED".equals(status) || "REJECTED".equals(status)) {
                        log.info("Payment failed with status: {}", status);
                        return new PaymentResponse(
                                paymentRequest.sourceReference(),
                                lookupResponse.ecocashReference(),
                                status,
                                "Payment " + status.toLowerCase(),
                                paymentRequest.amount(),
                                paymentRequest.currency(),
                                paymentRequest.customerMsisdn(),
                                lookupResponse.transactionDateTime()
                        );
                    }
                    // Status is still PENDING or unknown, continue polling
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Polling interrupted");
                break;
            } catch (Exception e) {
                log.warn("Error during polling attempt {}: {}", attempt, e.getMessage());
                // Continue polling despite errors
            }
        }

        // Timeout - return pending status
        log.warn("Polling timed out after {} seconds", maxAttempts * pollIntervalSeconds);
        return new PaymentResponse(
                paymentRequest.sourceReference(),
                null,
                "TIMEOUT",
                "Payment status unknown - check transaction lookup or wait for callback",
                paymentRequest.amount(),
                paymentRequest.currency(),
                paymentRequest.customerMsisdn(),
                null
        );
    }
}
