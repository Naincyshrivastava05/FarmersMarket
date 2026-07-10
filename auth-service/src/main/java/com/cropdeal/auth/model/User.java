package com.cropdeal.auth.model;


import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * The one table auth-service owns. Note what's NOT here: no farmer-specific
 * fields, no dealer-specific fields. This table only knows "who can log in
 * and what role do they have" -- farmer-service and dealer-service build
 * their own profile tables, keyed by this same userId, when they hear the
 * user.registered event. That's the ownership boundary from the design doc
 * in practice.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    // We generate the UUID ourselves (not relying on DB auto-increment)
    // because this same id needs to travel as plain data inside the
    // user.registered event payload to other services -- a UUID is
    // globally unique and doesn't depend on any one database's sequence.
    @GeneratedValue(generator = "UUID")
    @org.hibernate.annotations.GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    // Stores a BCrypt hash, never the plain password. See SecurityConfig
    // for the PasswordEncoder bean that produces this value.
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // JPA requires a no-args constructor to instantiate entities via
    // reflection when loading rows from the database.
    protected User() {
    }

    public User(String email, String password, Role role) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}