package com.example.MelodAI.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiGenerstionService {
    @Value("${replicate.api.token}")
    private String apiToken;
    private final CloudinaryStorageService cloudinaryStorageService;

    // Meta's MusicGen Melody Model URL
    private final String REPLICATE_API_URL = "https://api.replicate.com/v1/predictions";
    public String generateAudioUrl(String prompt) {
        RestClient restClient = RestClient.builder()
                .defaultHeader("Authorization", "Token " + apiToken)
                .defaultHeader("Content-Type", "application/json")
                .build();

        // 1. Prepare the JSON request payload for the AI model
        Map<String, Object> requestBody = Map.of(
                "version", "671ac645ce5e552cc63a54a2bbff63fcf798043055d2dac5fc9e36a837eedcfb", // Official MusicGen stable version
                "input", Map.of(
                        "prompt", prompt,
                        "model_version", "melody",
                        "duration", 10
                )
        );

        try {
            // 2. Trigger the AI generation request (Asynchronous on Replicate's servers)
            Map<?, ?> initialResponse = restClient.post()
                    .uri(REPLICATE_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (initialResponse == null || !initialResponse.containsKey("id")) {
                throw new RuntimeException("Failed to initialize AI prediction");
            }

            String predictionId = (String) initialResponse.get("id");
            String statusUrl = (String) ((Map<?, ?>) initialResponse.get("urls")).get("get");

            // 3. Poll the endpoint until processing is "succeeded"
            String finalAudioUrl = null;
            boolean isFinished = false;
            int maxAttempts = 20; // Safeguard timeout loop

            while (!isFinished && maxAttempts > 0) {
                Thread.sleep(3000); // Wait 3 seconds between polls

                Map<?, ?> pollResponse = restClient.get()
                        .uri(statusUrl)
                        .retrieve()
                        .body(Map.class);

                String status = (String) pollResponse.get("status");
                System.out.println("⏳ AI Generation Status: " + status);

                if ("succeeded".equals(status)) {
                    // Extract the output file string URL from response payload
                    finalAudioUrl = (String) pollResponse.get("output");
                    isFinished = true;
                } else if ("failed".equals(status) || "canceled".equals(status)) {
                    throw new RuntimeException("AI compilation failed on remote server.");
                }

                maxAttempts--;
            }

            byte[] audioBytes = restClient.get()
                    .uri(finalAudioUrl)
                    .retrieve()
                    .body(byte[].class);

            System.out.println("🎵 File downloaded. Uploading permanently to Cloudinary...");

            // 3. Upload those bytes directly into Cloudinary
            String permanentCloudinaryUrl = cloudinaryStorageService.uploadAudioBytes(audioBytes, prompt);

            // 4. Return the secure permanent Cloudinary URL to the worker to save in PostgreSQL
            return permanentCloudinaryUrl;

        } catch (Exception e) {
            throw new RuntimeException("Error during AI integration pipeline: " + e.getMessage());
        }
    }
}
