package com.cropdeal.farmer.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Farmer profile table -- owned exclusively by farmer-service.
 * Notice this table has NO password, NO role, NO JWT-related fields.
 * That data lives in auth-service's users table. This table only stores
 * farmer-specific profile information, keyed by the same userId that
 * auth-service assigned when the user registered.
 *
 * This is the boundary rule from the design doc in practice:
 * auth-service owns identity, farmer-service owns profile.
 * They share nothing except the userId as a reference key.
 */
@Entity
@Table(name = "farmers")
public class Farmer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    // This is the userId from auth-service -- NOT a foreign key in the
    // database sense (farmer_db has no connection to auth_db), but a
    // logical reference we use to link the two records together when needed.
    @Column(nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false)
    private String email;

    private String fullName;
    private String phoneNumber;
    private String farmLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FarmerStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    public enum FarmerStatus {
        ACTIVE, SUSPENDED
    }

    protected Farmer() {
    }

    // This constructor is called when the user.registered event arrives --
    // we create a minimal profile shell with just the data the event carries.
    // The farmer can fill in fullName, phoneNumber, farmLocation later via
    // a profile update endpoint.
    public Farmer(UUID userId, String email) {
        this.userId = userId;
        this.email = email;
        this.status = FarmerStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getFarmLocation() { return farmLocation; }
    public FarmerStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setFarmLocation(String farmLocation) { this.farmLocation = farmLocation; }
}