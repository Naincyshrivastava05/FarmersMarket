package com.cropdeal.chatbot.model;

/**
 * Response body sent back to the frontend.
 */
public record ChatResponse(
        String message,
        boolean success
) {
    public static ChatResponse success(String message) {
        return new ChatResponse(message, true);
    }

    public static ChatResponse error(String message) {
        return new ChatResponse(message, false);
    }
}