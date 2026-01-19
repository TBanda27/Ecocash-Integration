package com.ecocashpayment.ecocash.controller;

import com.ecocashpayment.ecocash.dto.*;
import com.ecocashpayment.ecocash.service.EcocashService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EcocashController {

    private final EcocashService ecocashService;

    @PostMapping("/payments")
    public Mono<ResponseEntity<PaymentResponse>> initiatePayment(@Valid @RequestBody PaymentRequest request) {
        return ecocashService.initiatePayment(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/refunds")
    public Mono<ResponseEntity<RefundResponse>> initiateRefund(@Valid @RequestBody RefundRequest request) {
        return ecocashService.initiateRefund(request)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/transactions/lookup")
    public Mono<ResponseEntity<TransactionLookupResponse>> lookupTransaction(
            @Valid @RequestBody TransactionLookupRequest request) {
        return ecocashService.lookupTransaction(request)
                .map(ResponseEntity::ok);
    }
}
