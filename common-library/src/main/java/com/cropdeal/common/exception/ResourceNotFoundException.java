package com.cropdeal.common.exception;

/**
 * Thrown by any service when a lookup (by id, by email, etc.) finds nothing.
 * Shared across services so each one's @ControllerAdvice can map it to a
 * 404 response using the shared ErrorResponse shape, without every service
 * reinventing its own "NotFoundException".
 *
 * This is the ONLY kind of shared logic that belongs in common-library -
 * a pure data-carrying exception, no behavior, no dependency on any one
 * service's domain model.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException forId(String resourceType, Object id) {
        return new ResourceNotFoundException(resourceType + " not found with id: " + id);
    }
}