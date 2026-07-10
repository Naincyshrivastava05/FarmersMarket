package com.cropdeal.crop.controller;

import com.cropdeal.common.dto.CropAvailabilityResponse;
import com.cropdeal.crop.dto.CropRequest;
import com.cropdeal.crop.model.Crop;
import com.cropdeal.crop.model.CropCategory;
import com.cropdeal.crop.service.CropService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/crops")
public class CropController {

    private final CropService cropService;

    public CropController(CropService cropService) {
        this.cropService = cropService;
    }

    @PostMapping
    public ResponseEntity<Crop> listCrop(
            @Valid @RequestBody CropRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cropService.listCrop(
                        request.farmerId(),
                        request.name(),
                        request.description(),
                        request.category(),
                        request.unitPrice(),
                        request.unit(),
                        request.quantity()));
    }

    @GetMapping
    public ResponseEntity<List<Crop>> getAvailable(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CropCategory category) {
        return ResponseEntity.ok(
                cropService.searchAvailable(search, category));
    }

    @GetMapping("/{cropId}")
    public ResponseEntity<Crop> getById(@PathVariable UUID cropId) {
        return ResponseEntity.ok(cropService.getById(cropId));
    }

    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<List<Crop>> getByFarmer(
            @PathVariable UUID farmerId) {
        return ResponseEntity.ok(cropService.getByFarmer(farmerId));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Crop>> getByCategory(
            @PathVariable CropCategory category) {
        return ResponseEntity.ok(cropService.getByCategory(category));
    }

    /**
     * This is the sync endpoint order-service calls via Feign.
     * GET /api/crops/{cropId}/availability?requestedQty=100
     */
    @GetMapping("/{cropId}/availability")
    public ResponseEntity<CropAvailabilityResponse> checkAvailability(
            @PathVariable UUID cropId,
            @RequestParam int requestedQty) {
        return ResponseEntity.ok(
                cropService.checkAvailability(cropId, requestedQty));
    }
}