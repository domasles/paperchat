package lt.domax.paperchat.domain.ai;

import java.util.concurrent.CompletableFuture;

public abstract class Provider {
    protected final String apiKey;
    protected final String model;

    protected final double temperature;
    protected final int timeout;

    public Provider(String apiKey, String model, double temperature, int timeout) {
        this.apiKey = apiKey;
        this.model = model;
        this.temperature = temperature;
        this.timeout = timeout;
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
