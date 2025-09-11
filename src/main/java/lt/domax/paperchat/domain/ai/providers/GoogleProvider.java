package lt.domax.paperchat.domain.ai.providers;

import lt.domax.paperchat.domain.ai.Provider;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import okhttp3.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class GoogleProvider extends Provider {
    private final OkHttpClient client;
    private final String endpoint = "https://generativelanguage.googleapis.com";

    public GoogleProvider(String apiKey, String model, double temperature, int timeout, String systemPrompt) {
        super(apiKey, model, temperature, timeout, systemPrompt);
        this.client = new OkHttpClient.Builder().connectTimeout(timeout, TimeUnit.SECONDS).readTimeout(timeout, TimeUnit.SECONDS).build();
    }
    
    @Override
    public CompletableFuture<String> sendMessage(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject requestBody = new JsonObject();

                JsonArray contents = new JsonArray();
                JsonObject content = new JsonObject();

                JsonArray parts = new JsonArray();
                JsonObject part = new JsonObject();

                JsonObject generationConfig = new JsonObject();

                part.addProperty("text", prompt);
                parts.add(part);

                content.add("parts", parts);
                contents.add(content);

                generationConfig.addProperty("temperature", temperature);
                generationConfig.addProperty("maxOutputTokens", 500);

                requestBody.add("contents", contents);
                requestBody.add("generationConfig", generationConfig);

                RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    MediaType.parse("application/json")
                );

                String url = endpoint + "/v1beta/models/" + model + ":generateContent";
                Request request = new Request.Builder().url(url).header("x-goog-api-key", apiKey).header("Content-Type", "application/json").post(body).build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) return "{\"message\": \"Error: Failed to get AI response (HTTP " + response.code() + ")\"}";

                    String responseBody = response.body().string();
                    JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

                    if (jsonResponse.has("candidates") && jsonResponse.getAsJsonArray("candidates").size() > 0) {
                        JsonObject candidate = jsonResponse.getAsJsonArray("candidates").get(0).getAsJsonObject();
                        JsonObject responseContent = candidate.getAsJsonObject("content");
                        JsonArray responseParts = responseContent.getAsJsonArray("parts");

                        if (responseParts.size() > 0) {
                            String aiResponse = responseParts.get(0).getAsJsonObject().get("text").getAsString().trim();

                            try {
                                JsonParser.parseString(aiResponse);
                                return aiResponse;
                            }

                            catch (Exception e) {
                                return "{\"message\": \"" + aiResponse.replace("\"", "\\\"") + "\"}";
                            }
                        }
                    }

                    return "{\"message\": \"Error: Invalid response format from AI\"}";
                }
            }

            catch (Exception e) {
                return "{\"message\": \"Error: " + e.getMessage() + "\"}";
            }
        });
    }

    @Override
    public boolean isAvailable() { return !apiKey.isEmpty(); }

    @Override
    public void shutdown() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }
}
