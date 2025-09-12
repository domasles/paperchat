package lt.domax.paperchat.domain.ai;

import lt.domax.paperchat.domain.config.PluginConfig;

import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.Map;

import java.lang.reflect.Constructor;

import java.io.File;
import java.net.URL;

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
        try {
            String packageName = "lt.domax.paperchat.domain.ai.providers";
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            URL resource = classLoader.getResource(path);

            if (resource != null) {
                File directory = new File(resource.getFile());

                if (directory.exists()) {
                    for (File file : directory.listFiles()) {
                        if (file.getName().endsWith(".class")) {
                            String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);

                            try {
                                Class<?> clazz = Class.forName(className);

                                if (Provider.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(AIProvider.class)) {
                                    AIProvider annotation = clazz.getAnnotation(AIProvider.class);
                                    String providerName = annotation.value();
                                    Constructor<?> constructor = clazz.getConstructor(String.class, String.class, double.class, int.class, int.class, String.class);

                                    Provider provider = (Provider) constructor.newInstance(
                                        config.getApiKey(),
                                        config.getModel(),
                                        config.getTemperature(),
                                        config.getTimeout(),
                                        config.getmaxOutputTokens(),
                                        config.getSystemPrompt()
                                    );

                                    providers.put(providerName, provider);
                                    System.out.println("[Registry] Auto-registered provider: " + providerName);
                                }
                            }

                            catch (Exception e) {
                                System.err.println("[Registry] Failed to register provider from class " + className + ": " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }

        catch (Exception e) {
            System.err.println("[Registry] Auto-registration failed: " + e.getMessage());
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
