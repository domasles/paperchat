package lt.domax.paperchat;

import lt.domax.paperchat.infrastructure.commands.ChatCommand;
import lt.domax.paperchat.domain.config.PluginConfig;
import lt.domax.paperchat.domain.chat.ChatService;
import lt.domax.paperchat.domain.ai.Registry;

import org.bukkit.plugin.java.JavaPlugin;

public class PaperChat extends JavaPlugin {
    private PluginConfig config;
    private Registry aiRegistry;
    private ChatService chatService;

    @Override
    public void onEnable() {
        getLogger().info("PaperChat starting up...");
        config = new PluginConfig();

        if (!config.isValid()) {
            getLogger().severe("Invalid configuration! Please set PAPERCHAT_API_KEY environment variable.");
            getServer().getPluginManager().disablePlugin(this);

            return;
        }

        aiRegistry = new Registry();
        aiRegistry.initialize(config);

        if (!aiRegistry.isReady()) {
            getLogger().severe("AI registry failed to initialize!");
            getServer().getPluginManager().disablePlugin(this);

            return;
        }

        chatService = new ChatService(aiRegistry, config);

        getCommand("paperchat").setExecutor(new ChatCommand(chatService));
        getLogger().info("PaperChat enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (aiRegistry != null) aiRegistry.shutdown();
        getLogger().info("PaperChat disabled.");
    }
}
