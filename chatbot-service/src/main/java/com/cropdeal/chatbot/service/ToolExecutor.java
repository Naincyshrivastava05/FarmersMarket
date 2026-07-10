package com.cropdeal.chatbot.service;

import com.cropdeal.chatbot.client.MicroserviceClient;
import com.cropdeal.chatbot.dto.CropData;
import com.cropdeal.chatbot.dto.OrderData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Executes the actual tool calls Claude requests.
 *
 * When Claude decides it needs live data to answer a question,
 * it responds with a tool_use block naming which tool to call
 * and what parameters to pass. This class receives that request,
 * calls the right microservice method, and returns the result
 * as a string Claude can read and incorporate into its answer.
 *
 * Tool names here must exactly match the names defined in
 * AnthropicService's tool definitions -- any mismatch means
 * Claude asks for a tool that never runs.
 */
@Component
public class ToolExecutor {

    private static final Logger log =
            LoggerFactory.getLogger(ToolExecutor.class);

    private final MicroserviceClient microserviceClient;
    private final ObjectMapper objectMapper;

    public ToolExecutor(MicroserviceClient microserviceClient,
                        ObjectMapper objectMapper) {
        this.microserviceClient = microserviceClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Routes a tool call to the right method based on tool name.
     * Returns a plain-text result Claude will read as context.
     */
    public String execute(String toolName, JsonNode toolInput,
            String userId, String userRole) {
log.info("Executing tool: {}", toolName);

return switch (toolName) {
case "get_available_crops" ->
      getAvailableCrops();
case "get_crops_by_category" ->
      // Guard against null input
      toolInput != null
              ? getCropsByCategory(toolInput)
              : "No category specified.";
case "get_crop_details" ->
      toolInput != null
              ? getCropDetails(toolInput)
              : "No crop ID specified.";
case "get_my_orders" ->
      getMyOrders(userId, userRole);
case "get_order_details" ->
      toolInput != null
              ? getOrderDetails(toolInput)
              : "No order ID specified.";
case "search_crops_by_price" ->
      toolInput != null
              ? searchCropsByPrice(toolInput)
              : "No price specified.";
default -> "Tool not found: " + toolName;
};
}
    private String getAvailableCrops() {
        List<CropData> crops =
                microserviceClient.getAvailableCrops();

        if (crops.isEmpty()) {
            return "No crops are currently available on the marketplace.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Available crops on CropDeal (")
          .append(crops.size())
          .append(" listings):\n\n");

        for (CropData crop : crops) {
            sb.append("- ").append(crop.name())
              .append(" | Category: ").append(crop.category())
              .append(" | Price: ₹").append(crop.unitPrice())
              .append("/").append(crop.unit())
              .append(" | Available: ").append(crop.availableQuantity())
              .append(" ").append(crop.unit());
            if (crop.description() != null
                    && !crop.description().isEmpty()) {
                sb.append(" | ").append(crop.description());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String getCropsByCategory(JsonNode input) {
        String category = input.get("category").asText()
                               .toUpperCase();
        List<CropData> crops =
                microserviceClient.getCropsByCategory(category);

        if (crops.isEmpty()) {
            return "No " + category + " crops found.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(category).append(" crops available:\n\n");
        for (CropData crop : crops) {
            sb.append("- ").append(crop.name())
              .append(" | ₹").append(crop.unitPrice())
              .append("/").append(crop.unit())
              .append(" | Stock: ").append(crop.availableQuantity())
              .append(" ").append(crop.unit()).append("\n");
        }
        return sb.toString();
    }

    private String getCropDetails(JsonNode input) {
        String cropId = input.get("crop_id").asText();
        CropData crop = microserviceClient.getCropById(cropId);

        if (crop == null) {
            return "Crop not found with ID: " + cropId;
        }

        return "Crop details:\n" +
               "Name: " + crop.name() + "\n" +
               "Category: " + crop.category() + "\n" +
               "Price: ₹" + crop.unitPrice() +
               "/" + crop.unit() + "\n" +
               "Available quantity: " +
               crop.availableQuantity() + " " + crop.unit() + "\n" +
               "Status: " + crop.status() + "\n" +
               "Description: " + (crop.description() != null
                       ? crop.description() : "Not provided");
    }

    private String getMyOrders(String userId, String userRole) {
        if (!"DEALER".equals(userRole)) {
            return "Order history is only available for dealers. " +
                   "You are logged in as a " + userRole + ".";
        }

        List<OrderData> orders =
                microserviceClient.getOrdersByDealer(userId);

        if (orders.isEmpty()) {
            return "You have no orders yet.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Your orders (").append(orders.size())
          .append(" total):\n\n");

        for (OrderData order : orders) {
            sb.append("Order #").append(
                    order.id().substring(0, 8)).append("...\n")
              .append("  Status: ").append(order.status()).append("\n")
              .append("  Total: ₹").append(order.totalAmount())
              .append("\n")
              .append("  Placed: ").append(order.createdAt())
              .append("\n\n");
        }
        return sb.toString();
    }

    private String getOrderDetails(JsonNode input) {
        String orderId = input.get("order_id").asText();
        OrderData order = microserviceClient.getOrderById(orderId);

        if (order == null) {
            return "Order not found with ID: " + orderId;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Order details for #")
          .append(order.id().substring(0, 8))
          .append("...\n")
          .append("Status: ").append(order.status()).append("\n")
          .append("Total: ₹").append(order.totalAmount()).append("\n")
          .append("Placed: ").append(order.createdAt()).append("\n")
          .append("Items:\n");

        if (order.items() != null) {
            for (OrderData.OrderItemData item : order.items()) {
                sb.append("  - Crop ID: ")
                  .append(item.cropId().substring(0, 8))
                  .append("... | Qty: ").append(item.quantity())
                  .append(" | ₹").append(item.unitPrice())
                  .append(" each | Subtotal: ₹")
                  .append(item.subtotal()).append("\n");
            }
        }
        return sb.toString();
    }

    private String searchCropsByPrice(JsonNode input) {
        BigDecimal maxPrice = new BigDecimal(
                input.get("max_price").asText());
        String category = input.has("category")
                ? input.get("category").asText() : null;

        List<CropData> crops = category != null
                ? microserviceClient.getCropsByCategory(
                        category.toUpperCase())
                : microserviceClient.getAvailableCrops();

        List<CropData> filtered = crops.stream()
                .filter(c -> c.unitPrice()
                        .compareTo(maxPrice) <= 0)
                .toList();

        if (filtered.isEmpty()) {
            return "No crops found" +
                   (category != null
                           ? " in " + category : "") +
                   " under ₹" + maxPrice + "/unit.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Crops under ₹").append(maxPrice)
          .append("/unit");
        if (category != null) {
            sb.append(" in ").append(category);
        }
        sb.append(":\n\n");

        for (CropData crop : filtered) {
            sb.append("- ").append(crop.name())
              .append(" | ₹").append(crop.unitPrice())
              .append("/").append(crop.unit())
              .append(" | Stock: ").append(crop.availableQuantity())
              .append(" ").append(crop.unit()).append("\n");
        }
        return sb.toString();
    }
}