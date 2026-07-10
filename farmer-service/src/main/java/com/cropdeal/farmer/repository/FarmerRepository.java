package com.cropdeal.farmer.repository;

import com.cropdeal.farmer.model.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FarmerRepository extends JpaRepository<Farmer, UUID> {

    Optional<Farmer> findByUserId(UUID userId);

    Optional<Farmer> findByEmail(String email);

    boolean existsByUserId(UUID userId);
}