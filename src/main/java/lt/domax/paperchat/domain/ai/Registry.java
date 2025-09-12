package lt.domax.paperchat.domain.ai;

import lt.domax.paperchat.domain.config.PluginConfig;

import java.util.concurrent.CompletableFuture;
import java.util.ServiceLoader;
import java.util.HashMap;
import java.util.Map;

public class Registry {
    private final Map<String, Provider> providers;
    private Provider activeProvider;

    public Registry() {
        this.providers = new HashMap<>();
    }

    public void initialize(PluginConfig config) {
        autoRegisterProviders(config);

        String providerName = config.getProvider();
        activeProvider = providers.get(providerName);

        if (activeProvider == null && !providers.isEmpty()) {
            activeProvider = providers.values().iterator().next();
        }
    }

    private void autoRegisterProviders(PluginConfig config) {
        ServiceLoader<Provider> loader = ServiceLoader.load(Provider.class, this.getClass().getClassLoader());

        for (Provider provider : loader) {
            AIProvider annotation = provider.getClass().getAnnotation(AIProvider.class);

            if (annotation != null) {
                String providerType = annotation.value();

                provider.initialize(
                    config.getApiKey(),
                    config.getModel(),
                    config.getTemperature(),
                    config.getTimeout(),
                    config.getMaxOutputTokens(),
                    config.getSystemPrompt()
                );

                providers.put(providerType.toLowerCase(), provider);
            }
        }
    }

    public CompletableFuture<String> sendMessage(String prompt) {
        if (activeProvider == null) return CompletableFuture.completedFuture("{\"message\": \"I'm not properly configured. Please contact an administrator.\"}");
        if (!activeProvider.isAvailable()) return CompletableFuture.completedFuture("{\"message\": \"I'm temporarily unavailable. Please try again later.\"}");

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
