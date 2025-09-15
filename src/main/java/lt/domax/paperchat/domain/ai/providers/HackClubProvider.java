package lt.domax.paperchat.domain.ai.providers;

import lt.domax.paperchat.domain.ai.AIProvider;
import lt.domax.paperchat.domain.ai.Provider;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

import okhttp3.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@AIProvider("hackclub")
public class HackClubProvider extends Provider {
    private final String endpoint = "https://ai.hackclub.com/chat/completions";
    private OkHttpClient client;

    public HackClubProvider() {
        super();
    }

    public HackClubProvider(String apiKey, String model, double temperature, int timeout, int maxOutputTokens, String systemPrompt) {
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
                JsonArray messages = new JsonArray();

                JsonObject systemMessage = new JsonObject();
                JsonObject userMessage = new JsonObject();

                requestBody.addProperty("temperature", temperature);
                requestBody.addProperty("max_completion_tokens", maxOutputTokens);

                systemMessage.addProperty("role", "system");
                systemMessage.addProperty("content", systemPrompt);

                userMessage.addProperty("role", "user");
                userMessage.addProperty("content", prompt);

                messages.add(systemMessage);
                messages.add(userMessage);

                requestBody.add("messages", messages);

                RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    MediaType.parse("application/json")
                );

                Request request = new Request.Builder().url(endpoint).header("Content-Type", "application/json").post(body).build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) return "{\"message\": \"I'm having trouble connecting right now. Please try again in a moment.\"}";

                    String responseBody = response.body().string();
                    JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

                    if (jsonResponse.has("choices") && jsonResponse.getAsJsonArray("choices").size() > 0) {
                        JsonObject choice = jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject();
                        JsonObject message = choice.getAsJsonObject("message");

                        String aiResponse = message.get("content").getAsString().replaceAll("(?s)<think>.*?</think>", "").trim();

                        try {
                            JsonParser.parseString(aiResponse);
                            return aiResponse;
                        }

                        catch (Exception e) {
                            return "{\"message\": \"" + aiResponse.replace("\"", "\\\"") + "\"}";
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
    public boolean isAvailable() { return client != null; }

    @Override
    public void shutdown() {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
    }
}
