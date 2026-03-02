package com.coverletter.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service pour communiquer avec l'API Groq (compatible OpenAI).
 * Endpoint: https://api.groq.com/openai/v1/chat/completions
 */
public class GroqApiService {

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

    private final HttpClient httpClient;
    private final Gson gson;
    private String apiKey;

    public GroqApiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.gson = new Gson();

        // Lire la clé API depuis la variable d'environnement
        this.apiKey = System.getenv("GROQ_API_KEY");
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Envoie une liste de messages à l'API Groq et retourne la réponse de l'assistant.
     *
     * @param messages Liste de maps avec "role" et "content"
     * @return La réponse textuelle de l'assistant
     */
    public CompletableFuture<String> chat(List<Map<String, String>> messages) {
        if (!isApiKeyConfigured()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Clé API Groq non configurée. " +
                            "Définissez la variable d'environnement GROQ_API_KEY ou entrez-la dans les paramètres.")
            );
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", MODEL);
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("max_tokens", 4096);

        JsonArray messagesArray = new JsonArray();
        for (Map<String, String> msg : messages) {
            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("role", msg.get("role"));
            msgObj.addProperty("content", msg.get("content"));
            messagesArray.add(msgObj);
        }
        requestBody.add("messages", messagesArray);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new RuntimeException("Erreur API Groq (HTTP " + response.statusCode() + "): " + response.body());
                    }

                    JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                    return jsonResponse
                            .getAsJsonArray("choices")
                            .get(0)
                            .getAsJsonObject()
                            .getAsJsonObject("message")
                            .get("content")
                            .getAsString();
                });
    }
}
