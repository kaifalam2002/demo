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

import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

@Service
public class AIService {

     @Value("${groq.api.key}")
    private String apiKey;

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
        String invoiceData = invoices.stream().map(inv -> 
            String.format("Bill No: %d, Customer: %s, Amount: ₹%.2f, Paid: %s, Due Date: %s",
                inv.getId(),
                inv.getCustomerName(),
                inv.getAmount(),
                inv.getIsPaid() ? "Yes" : "No",
                inv.getDueDate() != null ? inv.getDueDate().toString() : "N/A"
            )
        ).collect(Collectors.joining("\n"));

        // Append invoices to user prompt
        String finalPrompt = "Here are your invoices:\n" + invoiceData + "\n\nUser asks: " + prompt;

        ObjectNode body = mapper.createObjectNode();
        body.put("model", "openai/gpt-oss-20b"); // your model
        body.put("temperature", 1);
        body.put("max_completion_tokens", 8192);
        body.put("top_p", 1);
        body.put("reasoning_effort", "medium");

        // messages array
        ArrayNode messages = mapper.createArrayNode();
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

