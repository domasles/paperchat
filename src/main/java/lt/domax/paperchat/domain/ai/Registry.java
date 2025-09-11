package lt.domax.paperchat.domain.ai;

import lt.domax.paperchat.domain.ai.providers.OpenAIProvider;
import lt.domax.paperchat.domain.ai.providers.GoogleProvider;
import lt.domax.paperchat.domain.config.PluginConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Registry {
    private final Map<String, Provider> providers;
    private Provider activeProvider;

    public Registry() {
        this.providers = new HashMap<>();
    }

    public void initialize(PluginConfig config) {
        OpenAIProvider openaiProvider = new OpenAIProvider(
            config.getApiKey(),
            config.getModel(),
            config.getTemperature(),
            config.getTimeout()
        );

        providers.put("openai", openaiProvider);

        GoogleProvider googleProvider = new GoogleProvider(
            config.getApiKey(),
            config.getModel(),
            config.getTemperature(),
            config.getTimeout()
        );

        providers.put("google", googleProvider);

        String providerName = config.getProvider();
        activeProvider = providers.get(providerName);

        if (activeProvider == null) {
            activeProvider = providers.get("openai");
        }
    }

    public CompletableFuture<String> sendMessage(String prompt) {
        if (activeProvider == null) return CompletableFuture.completedFuture("Error: No AI provider configured");
        if (!activeProvider.isAvailable()) return CompletableFuture.completedFuture("Error: AI provider not available");

        return activeProvider.sendMessage(prompt);
    }

    public Provider getActiveProvider() { return activeProvider; }

    public void registerProvider(String name, Provider provider) {
        providers.put(name, provider);
    }

    public void setActiveProvider(String name) {
        Provider provider = providers.get(name);

        if (provider != null) {
            activeProvider = provider;
        }
    }

    public boolean isReady() { return activeProvider != null && activeProvider.isAvailable(); }

    public void shutdown() {
        for (Provider provider : providers.values()) {
            provider.shutdown();
        }

        providers.clear();
        activeProvider = null;
    }
}
