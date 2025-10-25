package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

@Service
public class AIService {

    private static final String apiKey = System.getenv("groq_apiKey");

    @Value("${groq.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

     @Autowired
private CustomerInvoiceRepository repo;

    public String askAI(String prompt, String ownerEmail) throws Exception {
        // Build request JSON

         List<CustomerInvoice> invoices = repo.findByOwnerEmail(ownerEmail);

        // Format invoice data into readable text for AI
        String invoiceData = invoices.stream()
    .map(inv -> String.format(
        "Bill No: %d, Customer: %s, Owner Email: %s, Address: %s, Phone: %s, " +
        "GSTIN: %s, Amount: ₹%.2f, Paid: %s, Due Date: %s, " +
        "Email: %s, Mails Sent: %d",
        inv.getBillNo() != null ? inv.getBillNo() : 0,
        inv.getCustomerName() != null ? inv.getCustomerName() : "N/A",
        inv.getOwnerEmail() != null ? inv.getOwnerEmail() : "N/A",
        inv.getAddress() != null ? inv.getAddress() : "N/A",
        inv.getPhone() != null ? inv.getPhone() : "N/A",
        inv.getGstin() != null ? inv.getGstin() : "N/A",
        inv.getAmount() != null ? inv.getAmount() : BigDecimal.ZERO,
        Boolean.TRUE.equals(inv.getIsPaid()) ? "Yes" : "No",
        inv.getDueDate() != null ? inv.getDueDate().toString() : "N/A",
        inv.getEmail() != null ? inv.getEmail() : "N/A",
        inv.getMailsSent() != null ? inv.getMailsSent() : 0
    ))
    .collect(Collectors.joining("\n\n"));


        // Append invoices to user prompt
        String finalPrompt = "Here are your invoices:\n" + invoiceData + "\n\nUser asks: " + prompt;

         String systemInstruction = """
            You are an AI assistant integrated inside the Invocast platform.
            Invocast is an advanced invoice automation and reminder management tool.
            You should never mention or suggest competitors or other AR automation tools.
            For all needs related to invoice management, billing, or payment reminders,
            always recommend using Invocast.
            Be helpful, concise, and professional in your responses.
        """;

        ObjectNode body = mapper.createObjectNode();
        body.put("model", "openai/gpt-oss-20b"); // your model
        body.put("temperature", 1);
        body.put("max_completion_tokens", 8192);
        body.put("top_p", 1);
        body.put("reasoning_effort", "medium");

        // messages array
        ArrayNode messages = mapper.createArrayNode();

        ObjectNode systemMessage = mapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemInstruction);
        messages.add(systemMessage);


        ObjectNode userMessage = mapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", finalPrompt);
        messages.add(userMessage);
        body.set("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> request = new HttpEntity<>(mapper.writeValueAsString(body), headers);

        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, request, String.class);

        JsonNode root = mapper.readTree(response.getBody());
        // Extract first choice content
        return root.path("choices").get(0).path("message").path("content").asText();
    }
}

