package lt.domax.paperchat;

import lt.domax.paperchat.infrastructure.commands.ChatCommand;

import lt.domax.paperchat.domain.player.PlayerChatManager;
import lt.domax.paperchat.domain.config.PluginConfig;
import lt.domax.paperchat.domain.chat.ChatService;
import lt.domax.paperchat.domain.ai.Registry;

import org.bukkit.plugin.java.JavaPlugin;

public class PaperChat extends JavaPlugin {
    private PlayerChatManager chatManager;
    private ChatCommand chatCommand;
    private ChatService chatService;

    private PluginConfig config;
    private Registry aiRegistry;

    @Override
    public void onEnable() {
        getLogger().info("PaperChat starting up...");
        saveDefaultConfig();

        config = new PluginConfig(this);

        aiRegistry = new Registry();
        aiRegistry.initialize(config);

        if (!aiRegistry.isReady()) {
            getLogger().severe("AI registry failed to initialize! Check your configuration.");
            getServer().getPluginManager().disablePlugin(this);

            return;
        }

        chatManager = new PlayerChatManager(config.getMaxHistory());
        chatCommand = new ChatCommand(chatService, config);
        chatService = new ChatService(aiRegistry, config, chatManager);

        getCommand("paperchat").setExecutor(chatCommand);
        getCommand("paperchat").setTabCompleter(chatCommand);

        getLogger().info("PaperChat enabled successfully!");
    }

    public String getConfigValue(String ymlKey, String envKey, String defaultValue) {
        String ymlValue = getConfig().getString(ymlKey);
        if (ymlValue != null && !ymlValue.isEmpty()) return ymlValue;

        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isEmpty()) return envValue;

        return defaultValue;
    }

    @Override
    public void onDisable() {
        if (aiRegistry != null) aiRegistry.shutdown();
        getLogger().info("PaperChat disabled.");
    }
}
