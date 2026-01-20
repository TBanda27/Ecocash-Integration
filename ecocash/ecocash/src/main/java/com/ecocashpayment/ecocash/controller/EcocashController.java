package com.ecocashpayment.ecocash.controller;

import com.ecocashpayment.ecocash.dto.*;
import com.ecocashpayment.ecocash.service.EcocashService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class EcocashController {

    private final EcocashService ecocashService;

    public EcocashController(EcocashService ecocashService) {
        this.ecocashService = ecocashService;
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = ecocashService.initiatePayment(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refunds")
    public ResponseEntity<RefundResponse> initiateRefund(@Valid @RequestBody RefundRequest request) {
        RefundResponse response = ecocashService.initiateRefund(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transactions/lookup")
    public ResponseEntity<TransactionLookupResponse> lookupTransaction(@Valid @RequestBody TransactionLookupRequest request) {
        TransactionLookupResponse response = ecocashService.lookupTransaction(request);
        return ResponseEntity.ok(response);
    }
}
