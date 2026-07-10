package com.cropdeal.payment.controller;

import com.cropdeal.payment.dto.PaymentIntentResponse;
import com.cropdeal.payment.model.PaymentEvent;
import com.cropdeal.payment.model.PaymentSnapshot;
import com.cropdeal.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Frontend calls this after an order is created to get
     * the clientSecret needed to render the Stripe payment form.
     */
    @PostMapping("/create-intent")
    public ResponseEntity<PaymentIntentResponse> createIntent(
            @RequestParam UUID orderId,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(
                paymentService.initiatePayment(orderId, amount));
    }

    @GetMapping("/{orderId}/history")
    public ResponseEntity<List<PaymentEvent>> getHistory(
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(
                paymentService.getPaymentHistory(orderId));
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<PaymentSnapshot> getStatus(
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(
                paymentService.getCurrentState(orderId));
    }
}