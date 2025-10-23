package com.optimizer.analyzer_service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// These are the correct imports for version 0.22.0
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionCreateParams;
// We DON'T need ChatMessage or ChatRole imports!

import java.util.List;

@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);
    private final OpenAIClient openAIClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String modelName;

    public AIService(
            @Value("${ai.api.key}") String apiKey,
            @Value("${ai.base.url}") String baseUrl,
            @Value("${ai.model.name}") String modelName
    ) {
        // This constructor is correct.
        this.openAIClient = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl) // Set the custom Groq URL
                .build();
        this.modelName = modelName;
    }

    public String getInsightForData(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode results = root.path("data").path("result");

            if (results.isMissingNode() || !results.iterator().hasNext()) {
                log.info("AI Service: No actionable data found in JSON.");
                return null;
            }

            String prompt = buildPrompt(results.toString());
            log.info("AI Service: Sending prompt to Groq ({})...", this.modelName);

            // *** THIS IS THE FIX ***
            // We use helper methods .addSystemMessage() and .addUserMessage()
            // This avoids the ChatMessage/ChatRole import error completely.
            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(this.modelName)
                .addSystemMessage("You are a helpful expert Java Spring Boot performance engineer. You give short, actionable advice in 1-2 sentences.")
                .addUserMessage(prompt)
                .build();

            // This part remains the same and is correct.
            ChatCompletion chatCompletion = openAIClient.chat().completions().create(params);
            
           // NEW, CORRECT
String aiResponse = chatCompletion.choices().get(0).message().content().orElse("Error: No content received");
            return aiResponse;

        } catch (Exception e) {
            log.error("Error communicating with AI: " + e.getMessage(), e); 
            return "Error generating insight.";
        }
    }

    private String buildPrompt(String resultsJson) {
        return "My Prometheus monitoring tool found these performance issues. " +
               "Please give me a 1-2 sentence recommendation for each one. " +
               "Format the output simply. \n\n" +
               "Data: \n" + resultsJson;
    }
}