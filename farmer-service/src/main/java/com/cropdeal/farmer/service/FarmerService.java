package com.cropdeal.farmer.service;

import com.cropdeal.common.exception.ResourceNotFoundException;
import com.cropdeal.farmer.model.Farmer;
import com.cropdeal.farmer.repository.FarmerRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FarmerService {

    private final FarmerRepository farmerRepository;

    public FarmerService(FarmerRepository farmerRepository) {
        this.farmerRepository = farmerRepository;
    }

    public Farmer getByUserId(UUID userId) {
        return farmerRepository.findByUserId(userId)
                .orElseThrow(() -> ResourceNotFoundException
                        .forId("Farmer", userId));
    }

    public Farmer updateProfile(UUID userId,
                                String fullName,
                                String phoneNumber,
                                String farmLocation) {
        Farmer farmer = getByUserId(userId);
        farmer.setFullName(fullName);
        farmer.setPhoneNumber(phoneNumber);
        farmer.setFarmLocation(farmLocation);
        return farmerRepository.save(farmer);
    }
}