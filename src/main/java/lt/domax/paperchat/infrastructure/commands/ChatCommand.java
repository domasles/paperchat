package lt.domax.paperchat.infrastructure.commands;

import lt.domax.paperchat.domain.config.PluginConfig;
import lt.domax.paperchat.domain.chat.ChatService;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.Command;

import org.bukkit.entity.Player;

public class ChatCommand implements CommandExecutor, TabCompleter {
    private final ChatService chatService;
    private final PluginConfig config;

    public ChatCommand(ChatService chatService, PluginConfig config) {
        this.chatService = chatService;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("paperchat.use")) {
            player.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {            
            player.sendMessage("§cUsage: /paperchat <option>");
            player.sendMessage("§cAvailable options:");
            player.sendMessage("§c - /paperchat <your desired question>");
            player.sendMessage("§c - /paperchat history");
            player.sendMessage("§c - /paperchat clear");

            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("history")) {
                chatService.showHistory(player.getName());
                return true;
        }
        
        if (args.length == 1 && args[0].equalsIgnoreCase("clear")) {
            chatService.clearHistory(player.getName());
            return true;
        }

        StringBuilder messageBuilder = new StringBuilder();

        for (int i = 0; i < args.length; i++) {
            messageBuilder.append(args[i]);
            if (i < args.length - 1) messageBuilder.append(" ");
        }

        String message = messageBuilder.toString();

        if (message.startsWith("\"") && message.endsWith("\"") && message.length() > 1) {
            message = message.substring(1, message.length() - 1);
        }

        if (message.trim().isEmpty()) {
            player.sendMessage("§cMessage cannot be empty.");
            return true;
        }

        if (message.length() > config.getMaxInputCharacters()) {
            sender.sendMessage("§cThe message exceeds the maximum length of " + config.getMaxInputCharacters() + " characters.");
            return true;
        }

        chatService.sendMessage(player.getName(), player.getName(), message);
        return true;
    }

    @Override
    public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        java.util.List<String> completions = new java.util.ArrayList<>();

        if (args.length == 1) {
            if ("history".startsWith(args[0].toLowerCase())) completions.add("history");
            if ("clear".startsWith(args[0].toLowerCase())) completions.add("clear");
        }

        return completions;
    }
}
