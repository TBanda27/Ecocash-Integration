package com.ecocashpayment.ecocash.exception;

import com.ecocashpayment.ecocash.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(WebExchangeBindException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        log.warn("Validation failed: {}", errors);

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Invalid request data",
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleWebClientException(WebClientResponseException ex) {
        log.error("EcoCash API error - Status: {}, Body: {}",
                ex.getStatusCode(), ex.getResponseBodyAsString());

        String message = switch (ex.getStatusCode().value()) {
            case 400 -> "Invalid request to EcoCash API";
            case 401 -> "Invalid API key";
            case 403 -> "API key doesn't have required permissions";
            case 404 -> "Resource not found";
            case 429 -> "Too many requests - please retry later";
            case 500, 502, 503 -> "EcoCash service temporarily unavailable";
            default -> "EcoCash API error";
        };

        ErrorResponse response = new ErrorResponse(
                ex.getStatusCode().value(),
                "EcoCash API Error",
                message
        );

        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(EcocashApiException.class)
    public ResponseEntity<ErrorResponse> handleEcocashApiException(EcocashApiException ex) {
        log.error("EcoCash API exception: {}", ex.getMessage());

        HttpStatus status = ex.getStatusCode() != null
                ? HttpStatus.valueOf(ex.getStatusCode().value())
                : HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse response = new ErrorResponse(
                status.value(),
                "EcoCash Error",
                ex.getMessage()
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error: ", ex);

        ErrorResponse response = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
