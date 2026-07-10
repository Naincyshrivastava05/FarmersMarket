package com.cropdeal.payment.repository;

import com.cropdeal.payment.model.PaymentSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentSnapshotRepository
        extends JpaRepository<PaymentSnapshot, UUID> {
}