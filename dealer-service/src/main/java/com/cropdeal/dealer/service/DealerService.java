package com.cropdeal.dealer.service;

import com.cropdeal.common.exception.ResourceNotFoundException;
import com.cropdeal.dealer.model.Dealer;
import com.cropdeal.dealer.repository.DealerRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DealerService {

    private final DealerRepository dealerRepository;

    public DealerService(DealerRepository dealerRepository) {
        this.dealerRepository = dealerRepository;
    }

    public Dealer getByUserId(UUID userId) {
        return dealerRepository.findByUserId(userId)
                .orElseThrow(() ->
                        ResourceNotFoundException.forId("Dealer", userId));
    }

    // This method is called synchronously by order-service via Feign
    // (design doc section 2) to check if a dealer exists and is active
    // before accepting an order.
    public boolean isDealerActive(UUID userId) {
        return dealerRepository.findByUserId(userId)
                .map(d -> d.getStatus() == Dealer.DealerStatus.ACTIVE)
                .orElse(false);
    }

    public Dealer updateProfile(UUID userId,
                                String fullName,
                                String phoneNumber,
                                String businessName,
                                String businessLocation) {
        Dealer dealer = getByUserId(userId);
        dealer.setFullName(fullName);
        dealer.setPhoneNumber(phoneNumber);
        dealer.setBusinessName(businessName);
        dealer.setBusinessLocation(businessLocation);
        return dealerRepository.save(dealer);
    }
}