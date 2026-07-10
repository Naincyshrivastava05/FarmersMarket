package com.cropdeal.auth.repository;

import com.cropdeal.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA generates the implementation of this interface at
 * runtime -- you never write a class that "implements UserRepository"
 * yourself. Method names like findByEmail are parsed by Spring Data and
 * turned into the equivalent SQL query automatically, based on the
 * field name "email" on the User entity.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}