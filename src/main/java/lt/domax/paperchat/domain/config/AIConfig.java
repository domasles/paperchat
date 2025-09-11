package lt.domax.paperchat.domain.config;

public class AIConfig {
    private final PluginConfig config;

    public AIConfig(PluginConfig config) {
        this.config = config;
    }

    public String getApiKey() { return config.getApiKey(); }
    public String getModel() { return config.getModel(); }
    public String getProvider() { return config.getProvider(); }

    public int getTimeout() { return config.getTimeout(); }
    public double getTemperature() { return config.getTemperature(); }
}
