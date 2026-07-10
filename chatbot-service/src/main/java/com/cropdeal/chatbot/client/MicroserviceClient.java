package com.cropdeal.chatbot.client;

import com.cropdeal.chatbot.dto.CropData;
import com.cropdeal.chatbot.dto.OrderData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

/**
 * Makes HTTP calls to your existing microservices to fetch
 * live data for the chatbot. Uses WebClient (reactive HTTP
 * client) since we added spring-boot-starter-webflux.
 *
 * These calls go directly to service ports (bypassing the
 * gateway) because chatbot-service is a backend service --
 * internal service-to-service calls don't need JWT tokens.
 */
@Component
public class MicroserviceClient {

    private static final Logger log =
            LoggerFactory.getLogger(MicroserviceClient.class);

    private final WebClient cropClient;
    private final WebClient orderClient;

    public MicroserviceClient(
            @Value("${services.crop-service.url}") String cropUrl,
            @Value("${services.order-service.url}") String orderUrl) {

        this.cropClient = WebClient.builder()
                .baseUrl(cropUrl)
                .build();
        this.orderClient = WebClient.builder()
                .baseUrl(orderUrl)
                .build();
    }

    /**
     * Fetches all crops with available stock.
     * Maps to GET /api/crops in crop-service.
     */
    public List<CropData> getAvailableCrops() {
        try {
            return cropClient.get()
                    .uri("/api/crops")
                    .retrieve()
                    .bodyToMono(
                        new ParameterizedTypeReference<List<CropData>>() {})
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch crops: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Fetches crops filtered by category.
     * Maps to GET /api/crops/category/{category}.
     */
    public List<CropData> getCropsByCategory(String category) {
        try {
            return cropClient.get()
                    .uri("/api/crops/category/{category}", category)
                    .retrieve()
                    .bodyToMono(
                        new ParameterizedTypeReference<List<CropData>>() {})
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch crops by category {}: {}",
                    category, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Fetches a single crop by its ID.
     * Maps to GET /api/crops/{cropId}.
     */
    public CropData getCropById(String cropId) {
        try {
            return cropClient.get()
                    .uri("/api/crops/{cropId}", cropId)
                    .retrieve()
                    .bodyToMono(CropData.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch crop {}: {}",
                    cropId, e.getMessage());
            return null;
        }
    }

    /**
     * Fetches all orders for a specific dealer.
     * Maps to GET /api/orders/dealer/{dealerId}.
     */
    public List<OrderData> getOrdersByDealer(String dealerId) {
        try {
            return orderClient.get()
                    .uri("/api/orders/dealer/{dealerId}", dealerId)
                    .retrieve()
                    .bodyToMono(
                        new ParameterizedTypeReference<List<OrderData>>() {})
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch orders for dealer {}: {}",
                    dealerId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Fetches a single order by ID.
     * Maps to GET /api/orders/{orderId}.
     */
    public OrderData getOrderById(String orderId) {
        try {
            return orderClient.get()
                    .uri("/api/orders/{orderId}", orderId)
                    .retrieve()
                    .bodyToMono(OrderData.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to fetch order {}: {}",
                    orderId, e.getMessage());
            return null;
        }
    }
}