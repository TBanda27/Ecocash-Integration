package com.ecocashpayment.ecocash.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class EcocashApiException extends RuntimeException {

    private final HttpStatusCode statusCode;
    private final String responseBody;

    public EcocashApiException(String message, HttpStatusCode statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public EcocashApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = null;
        this.responseBody = null;
    }
}
