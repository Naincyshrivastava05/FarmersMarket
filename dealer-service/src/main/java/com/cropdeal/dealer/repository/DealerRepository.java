package com.cropdeal.dealer.repository;

import com.cropdeal.dealer.model.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DealerRepository extends JpaRepository<Dealer, UUID> {

    Optional<Dealer> findByUserId(UUID userId);

    Optional<Dealer> findByEmail(String email);

    boolean existsByUserId(UUID userId);
}