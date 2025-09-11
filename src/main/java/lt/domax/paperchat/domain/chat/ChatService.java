package lt.domax.paperchat.domain.chat;

import lt.domax.paperchat.domain.player.PlayerChatManager;
import lt.domax.paperchat.domain.ai.Registry;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.util.concurrent.CompletableFuture;

public class ChatService {
    private final Registry aiRegistry;
    private final PlayerChatManager playerManager;

    public ChatService(Registry aiRegistry) {
        this.aiRegistry = aiRegistry;
        this.playerManager = new PlayerChatManager(10);
    }

    public void sendMessage(String senderName, String targetPlayerName, String message) {
        if (!aiRegistry.isReady()) {
            notifyPlayer(senderName, "§cError: AI service is not available");
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

        if (targetPlayer == null) {
            notifyPlayer(senderName, "§cError: Player '" + targetPlayerName + "' not found");
            return;
        }

        ChatSession session = playerManager.getOrCreateSession(senderName);

        String conversationHistory = session.getConversationHistory();
        String prompt = session.hasHistory() ? aiRegistry.getActiveProvider().formatPrompt(message, conversationHistory) : message;

        aiRegistry.sendMessage(prompt)
            .thenAccept(response -> {
                session.addMessage(senderName, message, response);

                String formattedMessage = "§6[AI]: §f" + response;
                Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("PaperChat"), () -> { targetPlayer.sendMessage(formattedMessage); });
            })

            .exceptionally(throwable -> {
                String errorMessage = "§cError: Failed to get AI response - " + throwable.getMessage();
                Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("PaperChat"), () -> { notifyPlayer(senderName, errorMessage); });

                return null;
            });

        notifyPlayer(senderName, "§aAI is thinking... Response will be sent to " + targetPlayerName);
    }

    private void notifyPlayer(String playerName, String message) {
        Player player = Bukkit.getPlayer(playerName);

        if (player != null) {
            player.sendMessage(message);
        }
    }
}
