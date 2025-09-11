package lt.domax.paperchat.domain.ai.providers;

import lt.domax.paperchat.domain.ai.Provider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

import okhttp3.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class OpenAIProvider extends Provider {
    private final OkHttpClient client;
    private final String endpoint = "https://api.openai.com/v1/chat/completions";

    public OpenAIProvider(String apiKey, String model, double temperature, int timeout) {
        super(apiKey, model, temperature, timeout);
        this.client = new OkHttpClient.Builder().connectTimeout(timeout, TimeUnit.SECONDS).readTimeout(timeout, TimeUnit.SECONDS).build();
    }

    @Override
    public CompletableFuture<String> sendMessage(String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonArray messages = new JsonArray();

                JsonObject userMessage = new JsonObject();
                JsonObject requestBody = new JsonObject();

                requestBody.addProperty("model", model);
                requestBody.addProperty("temperature", temperature);
                requestBody.addProperty("max_tokens", 500);

                userMessage.addProperty("role", "user");
                userMessage.addProperty("content", prompt);

                messages.add(userMessage);
                requestBody.add("messages", messages);

                RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    MediaType.parse("application/json")
                );

                Request request = new Request.Builder().url(endpoint).header("Authorization", "Bearer " + apiKey).header("Content-Type", "application/json").post(body).build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) return "{\"message\": \"Error: Failed to get AI response (HTTP " + response.code() + ")\"}";

                    String responseBody = response.body().string();
                    JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

                    if (jsonResponse.has("choices") && jsonResponse.getAsJsonArray("choices").size() > 0) {
                        JsonObject choice = jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject();
                        JsonObject message = choice.getAsJsonObject("message");

                        String aiResponse = message.get("content").getAsString().trim();

                        try {
                            JsonParser.parseString(aiResponse);
                            return aiResponse;
                        }

                        catch (Exception e) {
                            return "{\"message\": \"" + aiResponse.replace("\"", "\\\"") + "\"}";
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
