package com.cropdeal.dealer.controller;

import com.cropdeal.common.dto.DealerStatusResponse;
import com.cropdeal.dealer.model.Dealer;
import com.cropdeal.dealer.service.DealerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/dealers")
public class DealerController {

    private static final Logger log = LoggerFactory.getLogger(DealerController.class);
    private final DealerService dealerService;

    public DealerController(DealerService dealerService) {
        this.dealerService = dealerService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Dealer> getProfile(
            @PathVariable UUID userId) {
        log.info("DealerController GET /api/dealers/{}", userId);
        return ResponseEntity.ok(dealerService.getByUserId(userId));
    }

    // This endpoint is called by order-service via Feign (sync call
    // from design doc section 2) to verify dealer is active before
    // accepting an order. Returns the shared DealerStatusResponse DTO
    // from common-library so both sides agree on the shape.
    @GetMapping("/{userId}/status")
    public ResponseEntity<DealerStatusResponse> getDealerStatus(
            @PathVariable UUID userId) {
        boolean active = dealerService.isDealerActive(userId);
        return ResponseEntity.ok(
                new DealerStatusResponse(userId, true, active));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Dealer> updateProfile(
            @PathVariable UUID userId,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String businessName,
            @RequestParam(required = false) String businessLocation) {
        log.info("DealerController PUT /api/dealers/{} fullName={} phoneNumber={} businessName={} businessLocation={}",
                userId, fullName, phoneNumber, businessName, businessLocation);
        return ResponseEntity.ok(
                dealerService.updateProfile(
                        userId, fullName, phoneNumber,
                        businessName, businessLocation));
    }
}