package com.ecocashpayment.ecocash.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ecocash")
public record EcocashProperties(
        String baseUrl,
        Api api,
        String status,
        Callback callback
) {
    public record Api(String key) {}
    public record Callback(String url) {}

    public String getPaymentEndpoint() {
        return baseUrl + "/api/v2/payment/instant/c2b/" + status;
    }

    public String getRefundEndpoint() {
        return baseUrl + "/api/v2/refund/instant/c2b/" + status;
    }

    public String getTransactionLookupEndpoint() {
        return baseUrl + "/api/v1/transaction/c2b/status/" + status;
    }
}
