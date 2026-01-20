package com.ecocashpayment.ecocash.exception;

import com.ecocashpayment.ecocash.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
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

    @ExceptionHandler(EcocashApiException.class)
    public ResponseEntity<ErrorResponse> handleEcocashApiException(EcocashApiException ex) {
        log.error("EcoCash API exception: {}", ex.getMessage());

        HttpStatus status = ex.getStatusCode() != null
                ? HttpStatus.valueOf(ex.getStatusCode().value())
                : HttpStatus.INTERNAL_SERVER_ERROR;

        String message = switch (status.value()) {
            case 400 -> "Invalid request to EcoCash API";
            case 401 -> "Invalid API key";
            case 403 -> "API key doesn't have required permissions";
            case 404 -> "Resource not found";
            case 429 -> "Too many requests - please retry later";
            case 500, 502, 503 -> "EcoCash service temporarily unavailable";
            default -> ex.getMessage();
        };

        ErrorResponse response = new ErrorResponse(
                status.value(),
                "EcoCash API Error",
                message
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
