package com.cropdeal.dealer.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dealers")
public class Dealer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false)
    private String email;

    private String fullName;
    private String phoneNumber;
    private String businessName;
    private String businessLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DealerStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    public enum DealerStatus {
        ACTIVE, SUSPENDED
    }

    protected Dealer() {
    }

    public Dealer(UUID userId, String email) {
        this.userId = userId;
        this.email = email;
        this.status = DealerStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getBusinessName() { return businessName; }
    public String getBusinessLocation() { return businessLocation; }
    public DealerStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public void setBusinessLocation(String businessLocation) { this.businessLocation = businessLocation; }
}