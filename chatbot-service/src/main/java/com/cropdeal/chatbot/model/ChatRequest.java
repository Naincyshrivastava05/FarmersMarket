package com.cropdeal.chatbot.model;

import java.util.List;

/**
 * Request body the frontend sends to the chatbot endpoint.
 * Includes the full conversation history so Claude has context
 * of previous messages -- Claude has no memory between calls,
 * so the frontend must send the entire conversation each time.
 */
public record ChatRequest(
        List<ChatMessage> messages,
        String userId,
        String userRole
) {
}