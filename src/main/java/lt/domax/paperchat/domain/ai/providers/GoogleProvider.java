package lt.domax.paperchat.domain.ai.providers;

import lt.domax.paperchat.domain.ai.AIProvider;
import lt.domax.paperchat.domain.ai.Provider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

import okhttp3.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@AIProvider("google")
public class GoogleProvider extends Provider {
    private final String endpoint = "https://generativelanguage.googleapis.com";
    private OkHttpClient client;

    public GoogleProvider() {
        super();
    }

    public GoogleProvider(String apiKey, String model, double temperature, int timeout, int maxOutputTokens, String systemPrompt) {
        super(apiKey, model, temperature, timeout, maxOutputTokens, systemPrompt);
        this.client = new OkHttpClient.Builder().connectTimeout(timeout, TimeUnit.SECONDS).readTimeout(timeout, TimeUnit.SECONDS).build();
    }

    @Override
    public void initialize(String apiKey, String model, double temperature, int timeout, int maxOutputTokens, String systemPrompt) {
        super.initialize(apiKey, model, temperature, timeout, maxOutputTokens, systemPrompt);
        this.client = new OkHttpClient.Builder().connectTimeout(timeout, TimeUnit.SECONDS).readTimeout(timeout, TimeUnit.SECONDS).build();
    }

    @Override
    public CompletableFuture<String> sendMessage(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject requestBody = new JsonObject();

                JsonArray contents = new JsonArray();
                JsonObject content = new JsonObject();

                JsonArray systemParts = new JsonArray();
                JsonObject systemPart = new JsonObject();
                JsonObject systemInstruction = new JsonObject();

                JsonArray parts = new JsonArray();
                JsonObject part = new JsonObject();

                JsonObject generationConfig = new JsonObject();

                part.addProperty("text", prompt);
                parts.add(part);

                systemPart.addProperty("text", systemPrompt);
                systemParts.add(systemPart);
                systemInstruction.add("parts", systemParts);

                content.add("parts", parts);
                contents.add(content);

                generationConfig.addProperty("temperature", temperature);
                generationConfig.addProperty("maxOutputTokens", maxOutputTokens);

                requestBody.add("systemInstruction", systemInstruction);
                requestBody.add("contents", contents);
                requestBody.add("generationConfig", generationConfig);

                RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    MediaType.parse("application/json")
                );

                String url = endpoint + "/v1beta/models/" + model + ":generateContent";
                Request request = new Request.Builder().url(url).header("x-goog-api-key", apiKey).header("Content-Type", "application/json").post(body).build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) return "{\"message\": \"I'm having trouble connecting right now. Please try again in a moment.\"}";

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

                    return "{\"message\": \"I couldn't process your request properly. Please try rephrasing your question.\"}";
                }
            }

            catch (Exception e) {
                return "{\"message\": \"I encountered an issue while processing your request. Please try again.\"}";
            }
        });
    }

    @Override
    public boolean isAvailable() { return !apiKey.isEmpty() && client != null; }

    @Override
    public void shutdown() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }
}
