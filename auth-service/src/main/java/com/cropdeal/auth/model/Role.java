package com.cropdeal.auth.model;

/**
 * The three roles from the design doc's security section. Stored as a
 * String in the DB (see @Enumerated below) rather than an ordinal number,
 * so the database stays human-readable and safe to reorder/extend later
 * without corrupting existing rows.
 */
public enum Role {
    ADMIN,
    FARMER,
    DEALER
}