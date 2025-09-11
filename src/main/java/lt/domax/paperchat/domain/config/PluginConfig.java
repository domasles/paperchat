package lt.domax.paperchat.domain.config;

public class PluginConfig {
    private final String apiKey;
    private final int maxHistory;
    private final String provider;
    private final String model;
    private final double temperature;
    private final int timeout;

    public PluginConfig() {
        this.apiKey = getEnvOrDefault("PAPERCHAT_API_KEY", "");
        this.maxHistory = Integer.parseInt(getEnvOrDefault("PAPERCHAT_MAX_HISTORY", "10"));
        this.provider = getEnvOrDefault("PAPERCHAT_PROVIDER", "google");
        this.model = getEnvOrDefault("PAPERCHAT_MODEL", "gemini-2.0-flash");
        this.temperature = Double.parseDouble(getEnvOrDefault("PAPERCHAT_TEMPERATURE", "0.7"));
        this.timeout = Integer.parseInt(getEnvOrDefault("PAPERCHAT_TIMEOUT", "30"));
    }

    private String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    public String getApiKey() { return apiKey; }
    public String getProvider() { return provider; }
    public String getModel() { return model; }

    public int getMaxHistory() { return maxHistory; }
    public int getTimeout() { return timeout; }

    public double getTemperature() { return temperature; }
    public boolean isValid() { return !apiKey.isEmpty(); }
}
