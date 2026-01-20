package com.ecocashpayment.ecocash.controller;

import com.ecocashpayment.ecocash.dto.CallbackRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ecocash")
@Slf4j
public class CallbackController {

    @PostMapping("/callback")
    public ResponseEntity<String> handleCallback(@RequestBody CallbackRequest request) {
        log.info("Received EcoCash callback - Status: {}, ClientRef: {}, EcocashRef: {}",
                request.transactionOperationStatus(),
                request.clientReference(),
                request.ecocashReference());
        return ResponseEntity.ok("Callback received");
    }
}
