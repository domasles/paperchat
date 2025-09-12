package lt.domax.paperchat.domain.ai;

import java.util.concurrent.CompletableFuture;

public abstract class Provider {
    protected String apiKey;
    protected String model;
    protected String systemPrompt;
    protected int timeout;
    protected int maxOutputTokens;
    protected double temperature;

    public Provider() {}

    public Provider(String apiKey, String model, double temperature, int timeout, int maxOutputTokens, String systemPrompt) {
        this.apiKey = apiKey;
        this.model = model;
        this.temperature = temperature;
        this.timeout = timeout;
        this.maxOutputTokens = maxOutputTokens;
        this.systemPrompt = systemPrompt;
    }

    public void initialize(String apiKey, String model, double temperature, int timeout, int maxOutputTokens, String systemPrompt) {
        this.apiKey = apiKey;
        this.model = model;
        this.temperature = temperature;
        this.timeout = timeout;
        this.maxOutputTokens = maxOutputTokens;
        this.systemPrompt = systemPrompt;
    }

    public abstract CompletableFuture<String> sendMessage(String prompt);

    public abstract boolean isAvailable();
    public abstract void shutdown();

    public String formatPrompt(String userMessage, String conversationHistory) {
        StringBuilder prompt = new StringBuilder();

        if (!conversationHistory.isEmpty()) {
            prompt.append("Previous conversation:\n").append(conversationHistory).append("\n\n");
        }

        prompt.append("User: ").append(userMessage);
        return prompt.toString();
    }
}
