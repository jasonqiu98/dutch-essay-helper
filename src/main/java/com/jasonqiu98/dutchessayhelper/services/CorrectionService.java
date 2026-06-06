package com.jasonqiu98.dutchessayhelper.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CorrectionService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${ollama.model}")
    private String ollamaModel;

    public CorrectionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public String correctEssay(String taskType, String prompt, String essay) {
        String enrichedPrompt = buildPrompt(taskType, prompt, essay);
        String feedback = callOllama(enrichedPrompt);
        return feedback;
    }


    private String buildPrompt(String taskType, String prompt, String essay) {
        return """
                You are an NT2 Dutch B2 writing teacher.
                Correct the following Dutch essay.
                Requirements:
                1. Provide score based on the rubric.
                2. Point out the most important grammar mistakes.
                3. Explain the mistakes in English.
                4. Rewrite 3-5 important sentences in natural B2 Dutch.
                5. Do not rewrite the essay into C1/C2 level.
                6. Do not show hidden reasoning. Only output the final feedback.

                Rubric:
                1. Content: 0-5 points
                2. Grammar: 0-2 points
                3. Spelling: 0-2 points
                4. Fluency: 0-2 points
                5. Vocabulary: 0-2 points
                6. Logic: 0-2 points
                7. Punctuation: 0-2 points

                Task type: %s
                Writing prompt: %s
                Student essay: %s
                """.formatted(taskType, prompt, essay);
    }

    private String callOllama(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                "model", ollamaModel,
                "prompt", prompt,
                "stream", false
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(ollamaBaseUrl + "/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
            
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() < 200 || httpResponse.statusCode() >= 300) {
                throw new RuntimeException("Ollama returned status " + httpResponse.statusCode()
                        + ": " + httpResponse.body());
            }

            JsonNode root = objectMapper.readTree(httpResponse.body());
            String response = root.path("response").asText();

            if (response == null || response.isBlank()) {
                return "Ollama returned an empty response.";
            }

            return response.trim();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ollama call was interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Error calling Ollama: ", e);
        }
    }
}
