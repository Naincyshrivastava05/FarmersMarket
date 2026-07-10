package com.cropdeal.farmer.controller;

import com.cropdeal.farmer.model.Farmer;
import com.cropdeal.farmer.service.FarmerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/farmers")
public class FarmerController {

    private static final Logger log = LoggerFactory.getLogger(FarmerController.class);
    private final FarmerService farmerService;

    public FarmerController(FarmerService farmerService) {
        this.farmerService = farmerService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Farmer> getProfile(@PathVariable UUID userId) {
        log.info("FarmerController GET /api/farmers/{}", userId);
        return ResponseEntity.ok(farmerService.getByUserId(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Farmer> updateProfile(
            @PathVariable UUID userId,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String farmLocation) {
        log.info("FarmerController PUT /api/farmers/{} fullName={} phoneNumber={} farmLocation={}",
                userId, fullName, phoneNumber, farmLocation);
        return ResponseEntity.ok(
                farmerService.updateProfile(userId, fullName,
                        phoneNumber, farmLocation));
    }
}