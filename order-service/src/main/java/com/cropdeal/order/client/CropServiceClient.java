package com.cropdeal.order.client;

import com.cropdeal.common.dto.CropAvailabilityResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

/**
 * Feign client for synchronous calls to crop-service.
 * The @FeignClient annotation tells Spring to generate a real HTTP
 * client implementation of this interface at startup -- you never
 * write the implementation yourself.
 *
 * url points at crop-service's base URL. In production this would
 * be a service discovery name instead of a hardcoded URL, but
 * hardcoded is fine for local development.
 *
 * The method signature mirrors the exact endpoint in CropController:
 * GET /api/crops/{cropId}/availability?requestedQty=x
 * If that endpoint changes, this interface must change too -- which
 * is exactly why CropAvailabilityResponse lives in common-library,
 * so both sides always agree on the response shape.
 */
@FeignClient(name = "crop-service", url = "${services.crop-service.url}")
public interface CropServiceClient {

    @GetMapping("/api/crops/{cropId}/availability")
    CropAvailabilityResponse checkAvailability(
            @PathVariable UUID cropId,
            @RequestParam int requestedQty);
}