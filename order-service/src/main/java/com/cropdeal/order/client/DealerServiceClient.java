package com.cropdeal.order.client;

import com.cropdeal.common.dto.DealerStatusResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign client for synchronous calls to dealer-service.
 * Mirrors GET /api/dealers/{userId}/status in DealerController.
 */
@FeignClient(name = "dealer-service", url = "${services.dealer-service.url}")
public interface DealerServiceClient {

    @GetMapping("/api/dealers/{userId}/status")
    DealerStatusResponse getDealerStatus(@PathVariable UUID userId);
}