package com.cropdeal.chatbot.controller;

import com.cropdeal.chatbot.model.ChatRequest;
import com.cropdeal.chatbot.model.ChatResponse;
import com.cropdeal.chatbot.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private static final Logger log =
            LoggerFactory.getLogger(ChatbotController.class);

    private final ChatService chatService;

    public ChatbotController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @RequestBody ChatRequest request) {
        try {
            log.info("Chat request from userId: {} role: {}",
                    request.userId(), request.userRole());

            String response = chatService.chat(
                    request.messages(),
                    request.userId(),
                    request.userRole()
            );

            return ResponseEntity.ok(
                    ChatResponse.success(response));

        } catch (Exception e) {
            log.error("Chat failed: {}", e.getMessage(), e);
            return ResponseEntity.ok(
                    ChatResponse.error(
                            "Sorry, I'm having trouble right now. " +
                            "Please try again in a moment."));
        }
    }
}