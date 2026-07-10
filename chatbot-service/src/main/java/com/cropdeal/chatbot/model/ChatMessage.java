package com.cropdeal.chatbot.model;

/**
 * Represents a single message in a conversation.
 * role is either "user" or "assistant" -- matching
 * exactly what Claude API expects in the messages array.
 */
public record ChatMessage(
        String role,
        String content
) {
}