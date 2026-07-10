package com.cropdeal.crop.service;

import com.cropdeal.common.dto.CropAvailabilityResponse;
import com.cropdeal.common.event.EventEnvelope;
import com.cropdeal.common.event.EventTypes;
import com.cropdeal.common.event.InventoryUpdatedEvent;
import com.cropdeal.common.exception.ResourceNotFoundException;
import com.cropdeal.crop.model.Crop;
import com.cropdeal.crop.model.CropCategory;
import com.cropdeal.crop.repository.CropRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CropService {

    private final CropRepository cropRepository;
    private final RabbitTemplate rabbitTemplate;

    public CropService(CropRepository cropRepository,
                       RabbitTemplate rabbitTemplate) {
        this.cropRepository = cropRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Crop listCrop(UUID farmerId, String name, String description,
                         CropCategory category, BigDecimal unitPrice,
                         String unit, Integer quantity) {
        Crop crop = new Crop(farmerId, name, description,
                category, unitPrice, unit, quantity);
        return cropRepository.save(crop);
    }

    public Crop getById(UUID cropId) {
        return cropRepository.findById(cropId)
                .orElseThrow(() ->
                        ResourceNotFoundException.forId("Crop", cropId));
    }

    public List<Crop> getAllAvailable() {
       
        return cropRepository.findAllWithAvailableStock();
    }

    public List<Crop> searchAvailable(String search,
                                      CropCategory category) {
        return getAllAvailable().stream()
                .filter(crop -> {
                    if (search == null || search.isBlank()) {
                        return true;
                    }
                    String normalized = search.trim().toLowerCase();
                    return crop.getName().toLowerCase().contains(normalized)
                            || (crop.getDescription() != null
                            && crop.getDescription().toLowerCase().contains(normalized));
                })
                .filter(crop -> category == null || crop.getCategory() == category)
                .toList();
    }

    public List<Crop> getByFarmer(UUID farmerId) {
        return cropRepository.findByFarmerId(farmerId);
    }

    public List<Crop> getByCategory(CropCategory category) {
        return cropRepository.findByCategory(category);
    }

    /**
     * Called synchronously by order-service via Feign BEFORE accepting
     * an order -- "is this crop available in this quantity right now?"
     * Returns the shared CropAvailabilityResponse DTO from common-library
     * so both sides agree on the shape without duplicating code.
     */
    public CropAvailabilityResponse checkAvailability(UUID cropId,
                                                       int requestedQty) {
        return cropRepository.findById(cropId)
                .map(crop -> new CropAvailabilityResponse(
                        crop.getId(),
                        crop.getAvailableQuantity() >= requestedQty,
                        crop.getAvailableQuantity(),
                        crop.getUnitPrice()
                ))
                .orElse(new CropAvailabilityResponse(
                        cropId, false, 0, BigDecimal.ZERO));
    }

    /**
     * Called when order.created event arrives -- decrements stock.
     * @Transactional ensures that if saving fails, no partial update
     * is committed -- the whole operation rolls back atomically.
     */
    @Transactional
    public void reserveStock(UUID cropId, int quantity) {
        Crop crop = getById(cropId);
        boolean reserved = crop.reserveQuantity(quantity);

        if (!reserved) {
            throw new IllegalStateException(
                    "Insufficient stock for crop: " + cropId);
        }

        cropRepository.save(crop);

        // Publish inventory.updated so any future search/cache service
        // can stay in sync -- design doc section 3.6.
        InventoryUpdatedEvent payload = new InventoryUpdatedEvent(
                crop.getId(),
                crop.getAvailableQuantity(),
                Instant.now()
        );
        rabbitTemplate.convertAndSend(
                EventTypes.EXCHANGE,
                EventTypes.INVENTORY_UPDATED,
                new EventEnvelope<>(EventTypes.INVENTORY_UPDATED, payload)
        );
    }

    /**
     * Called when order.cancelled event arrives -- releases stock back.
     */
    @Transactional
    public void releaseStock(UUID cropId, int quantity) {
        Crop crop = getById(cropId);
        crop.releaseQuantity(quantity);
        cropRepository.save(crop);

        InventoryUpdatedEvent payload = new InventoryUpdatedEvent(
                crop.getId(),
                crop.getAvailableQuantity(),
                Instant.now()
        );
        rabbitTemplate.convertAndSend(
                EventTypes.EXCHANGE,
                EventTypes.INVENTORY_UPDATED,
                new EventEnvelope<>(EventTypes.INVENTORY_UPDATED, payload)
        );
    }
}