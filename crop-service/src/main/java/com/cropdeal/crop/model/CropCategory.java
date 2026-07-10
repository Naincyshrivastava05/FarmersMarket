package com.cropdeal.crop.model;

/**
 * Categorizes crops so dealers can filter by type.
 * Stored as STRING in DB for same reason as Role in auth-service --
 * safe to reorder and human readable in the database.
 */
public enum CropCategory {
    GRAINS,
    VEGETABLES,
    FRUITS,
    PULSES,
    OILSEEDS,
    SPICES,
    COTTON,
    OTHER
}