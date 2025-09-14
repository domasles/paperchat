package lt.domax.paperchat.domain.chat;

import lt.domax.paperchat.domain.player.PlayerChatManager;
import lt.domax.paperchat.domain.config.PluginConfig;
import lt.domax.paperchat.domain.ai.Registry;

import org.bukkit.entity.Player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.bukkit.Bukkit;

public class ChatService {
    private final Registry aiRegistry;
    private final PlayerChatManager playerManager;

    public ChatService(Registry aiRegistry, PluginConfig config, PlayerChatManager chatManager) {
        this.aiRegistry = aiRegistry;
        this.playerManager = chatManager;
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

        aiRegistry.sendMessage(prompt).thenAccept(response -> {
                String actualMessage;

                try {
                    JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
                    actualMessage = jsonResponse.get("message").getAsString();
                }

                catch (Exception e) {
                    actualMessage = extractMessageFromResponse(response);
                }

                session.addMessage(senderName, message, actualMessage);

                String formattedMessage = "§6[AI]: §f" + actualMessage;
                Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("PaperChat"), () -> { targetPlayer.sendMessage(formattedMessage); });
            })

            .exceptionally(throwable -> {
                String errorMessage = "§c[AI]: I'm experiencing technical difficulties. Please try again later.";
                Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("PaperChat"), () -> { notifyPlayer(senderName, errorMessage); });

                return null;
            });

        notifyPlayer(senderName, "§aAI is thinking... Response will be sent to " + targetPlayerName);
    }

    private String extractMessageFromResponse(String response) {
        try {
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

            if (jsonResponse.has("message")) {
                String message = jsonResponse.get("message").getAsString();
                return "Sorry, I had trouble formatting my response. Here's what I meant: " + message;
            }
        }

        catch (Exception e) {}
        
        try {
            String cleanedResponse = response.trim();

            if (cleanedResponse.startsWith("\"") && cleanedResponse.endsWith("\"")) {
                cleanedResponse = cleanedResponse.substring(1, cleanedResponse.length() - 1);
            }

            if (!cleanedResponse.startsWith("{") && !cleanedResponse.startsWith("[")) {
                return cleanedResponse.replace("\\\"", "\"").replace("\\n", "\n").trim();
            }

            JsonObject jsonResponse = JsonParser.parseString(cleanedResponse).getAsJsonObject();

            if (jsonResponse.has("message")) {
                return "Sorry, I had trouble formatting my response. Here's what I meant: " + jsonResponse.get("message").getAsString();
            }
            
        }

        catch (Exception e) {}
        return "I encountered a formatting issue. Please try asking your question again.";
    }

    private void notifyPlayer(String playerName, String message) {
        Player player = Bukkit.getPlayer(playerName);

        if (player != null) {
            player.sendMessage(message);
        }
    }
}
