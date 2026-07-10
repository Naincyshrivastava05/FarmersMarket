package com.cropdeal.chatbot.service;

import com.cropdeal.chatbot.model.ChatMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * Calls DeepSeek via NVIDIA NIM's OpenAI-compatible API.
 *
 * DeepSeek doesn't support structured tool-calling the way
 * Claude does, so we use a different approach:
 *
 * 1. Look at the user's message and decide what live data
 *    might be relevant (crops? orders? prices?)
 * 2. Fetch that data proactively from our microservices
 * 3. Inject the live data into the system prompt as context
 * 4. Send everything to DeepSeek and let it answer naturally
 *
 * This is called "retrieval-augmented generation" (RAG) --
 * the same pattern used in production AI systems. DeepSeek
 * sees real data from your database and answers based on it,
 * without needing formal tool-calling support.
 */
@Service
public class ChatService {

    private static final Logger log =
            LoggerFactory.getLogger(ChatService.class);

    @Value("${nvidia.api-key}")
    private String apiKey;

    @Value("${nvidia.base-url}")
    private String baseUrl;

    @Value("${nvidia.model}")
    private String model;

    private final ToolExecutor toolExecutor;
    private final ObjectMapper objectMapper;

    public ChatService(ToolExecutor toolExecutor,
                       ObjectMapper objectMapper) {
        this.toolExecutor = toolExecutor;
        this.objectMapper = objectMapper;
    }

    public String chat(List<ChatMessage> messages,
                       String userId, String userRole) {

        // Step 1: get the latest user message to analyze.
        String userQuestion = messages.stream()
                .filter(m -> "user".equals(m.role()))
                .reduce((first, second) -> second)
                .map(ChatMessage::content)
                .orElse("");

        log.info("Processing question: {}", userQuestion);

        // Step 2: fetch relevant live data based on what the
        // user is asking about. We check keywords to decide
        // which microservice endpoints to call.
        String liveData = fetchRelevantData(
                userQuestion, userId, userRole);

        log.info("Fetched live data ({} chars)", liveData.length());

        // Step 3: build the request body in OpenAI format.
        // NVIDIA NIM uses exactly the same JSON structure as
        // the OpenAI chat completions API -- same field names,
        // same message format, same response shape.
        ObjectNode requestBody = buildRequestBody(
                messages, userRole, liveData);

        // Step 4: call NVIDIA NIM API.
        String response = callNvidiaApi(requestBody);

        return response;
    }

    /**
     * Decides which live data to fetch based on keywords in
     * the user's question. This is a simple but effective
     * approach -- in a more advanced system you'd use an
     * embedding model to do semantic matching instead of
     * keyword matching, but this works well for CropDeal's
     * focused domain.
     */
    private String fetchRelevantData(String question,
                                      String userId,
                                      String userRole) {

        // Normalize to lowercase for keyword matching.
        String q = question.toLowerCase();

        StringBuilder data = new StringBuilder();

        // Crop-related questions -- fetch available crops.
        if (q.contains("crop") || q.contains("available")
                || q.contains("buy") || q.contains("price")
                || q.contains("wheat") || q.contains("rice")
                || q.contains("grain") || q.contains("vegetable")
                || q.contains("fruit") || q.contains("list")
                || q.contains("what") || q.contains("show")) {

            String crops = toolExecutor.execute(
                    "get_available_crops", null, userId, userRole);
            data.append("=== LIVE CROP DATA FROM CROPDEAL ===\n")
                .append(crops).append("\n\n");
        }

        // Price-related questions -- also fetch crops if not
        // already fetched above.
        if (q.contains("under") || q.contains("below")
                || q.contains("cheap") || q.contains("budget")
                || q.contains("affordable") || q.contains("₹")
                || q.contains("rupee") || q.contains("rs.")) {

            if (data.length() == 0) {
                String crops = toolExecutor.execute(
                        "get_available_crops", null, userId, userRole);
                data.append("=== LIVE CROP DATA FROM CROPDEAL ===\n")
                    .append(crops).append("\n\n");
            }
        }

        // Order-related questions -- fetch dealer's orders.
        if ((q.contains("order") || q.contains("purchase")
                || q.contains("bought") || q.contains("history")
                || q.contains("my order"))
                && "DEALER".equals(userRole)) {

            String orders = toolExecutor.execute(
                    "get_my_orders", null, userId, userRole);
            data.append("=== LIVE ORDER DATA FROM CROPDEAL ===\n")
                .append(orders).append("\n\n");
        }

        // Category-specific questions.
        if (q.contains("grain")) {
            String grains = toolExecutor.execute(
                    "get_crops_by_category",
                    buildCategoryInput("GRAINS"),
                    userId, userRole);
            data.append("=== GRAINS DATA ===\n")
                .append(grains).append("\n\n");
        }
        if (q.contains("vegetable")) {
            String vegs = toolExecutor.execute(
                    "get_crops_by_category",
                    buildCategoryInput("VEGETABLES"),
                    userId, userRole);
            data.append("=== VEGETABLES DATA ===\n")
                .append(vegs).append("\n\n");
        }
        if (q.contains("fruit")) {
            String fruits = toolExecutor.execute(
                    "get_crops_by_category",
                    buildCategoryInput("FRUITS"),
                    userId, userRole);
            data.append("=== FRUITS DATA ===\n")
                .append(fruits).append("\n\n");
        }
        if (q.contains("spice")) {
            String spices = toolExecutor.execute(
                    "get_crops_by_category",
                    buildCategoryInput("SPICES"),
                    userId, userRole);
            data.append("=== SPICES DATA ===\n")
                .append(spices).append("\n\n");
        }

        return data.toString();
    }

    private com.fasterxml.jackson.databind.JsonNode
            buildCategoryInput(String category) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("category", category);
        return node;
    }

    /**
     * Builds the OpenAI-compatible request body.
     * The key insight: we inject live data into the SYSTEM
     * prompt as context, not as a separate "tool result".
     * DeepSeek then uses this context to answer accurately.
     */
    private ObjectNode buildRequestBody(
            List<ChatMessage> messages,
            String userRole, String liveData) {

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", model);
        body.put("temperature", 0.7);
        body.put("max_tokens", 1024);
        body.put("stream", false);

        ArrayNode messagesArray = body.putArray("messages");

        // System message with live data injected.
        ObjectNode systemMsg = messagesArray.addObject();
        systemMsg.put("role", "system");
        systemMsg.put("content",
                buildSystemPrompt(userRole, liveData));

        // Add conversation history -- all previous messages
        // so DeepSeek has context of the full conversation.
        for (ChatMessage msg : messages) {
            ObjectNode msgNode = messagesArray.addObject();
            msgNode.put("role", msg.role());
            msgNode.put("content", msg.content());
        }

        return body;
    }

    private String buildSystemPrompt(String userRole,
                                      String liveData) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                You are CropBot, a helpful AI assistant for CropDeal
                -- an agricultural marketplace in India connecting
                farmers directly with dealers.
                
                The user is logged in as a %s.
                
                Guidelines:
                - Be helpful, friendly, and concise
                - Use Indian Rupees (₹) for all prices
                - For farmers: help with listing crops and pricing
                - For dealers: help find crops and track orders
                - Answer only from the live data provided below
                - If data shows no results, say so honestly
                - Keep responses short and easy to read
                
                """.formatted(userRole));

        if (!liveData.isEmpty()) {
            prompt.append("=== REAL-TIME DATA FROM CROPDEAL ===\n");
            prompt.append(liveData);
            prompt.append("\nUse the above live data to answer ");
            prompt.append("the user's question accurately.\n");
        } else {
            prompt.append("""
                No specific live data was fetched for this question.
                Answer from your general knowledge about agriculture
                and crop trading in India.
                """);
        }

        return prompt.toString();
    }

    /**
     * Makes the actual HTTP call to NVIDIA NIM's API.
     * The endpoint and request format are identical to OpenAI's
     * chat completions API -- only the base URL and model name
     * differ.
     */
    private String callNvidiaApi(ObjectNode requestBody) {
        try {
            WebClient webClient = WebClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION,
                            "Bearer " + apiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE,
                            MediaType.APPLICATION_JSON_VALUE)
                    .build();

            String responseBody = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parse the OpenAI-format response to extract the
            // assistant's message content.
            JsonNode responseJson =
                    objectMapper.readTree(responseBody);

            // Response structure:
            // { "choices": [{ "message": { "content": "..." } }] }
            String content = responseJson
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText("");

            if (content.isEmpty()) {
                log.warn("Empty content in NVIDIA response: {}",
                        responseBody);
                return "I couldn't generate a response. " +
                       "Please try again.";
            }

            // DeepSeek sometimes includes a <think>...</think>
            // reasoning block before the actual answer. We strip
            // it out so only the clean answer reaches the user.
            return stripThinkingBlock(content);

        } catch (Exception e) {
            log.error("NVIDIA API call failed: {}", e.getMessage());
            throw new RuntimeException(
                    "AI service unavailable: " + e.getMessage());
        }
    }

    /**
     * DeepSeek R1/V4 models include a reasoning trace wrapped
     * in <think>...</think> tags before the actual response.
     * This strips that out so users see only the clean answer.
     */
    private String stripThinkingBlock(String content) {
        if (content.contains("<think>") &&
                content.contains("</think>")) {
            int end = content.indexOf("</think>");
            if (end != -1 && end + 8 < content.length()) {
                return content.substring(end + 8).trim();
            }
        }
        return content.trim();
    }
}