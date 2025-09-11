package lt.domax.paperchat.infrastructure.commands;

import lt.domax.paperchat.domain.chat.ChatService;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;

import org.bukkit.entity.Player;

public class ChatCommand implements CommandExecutor {
    private final ChatService chatService;

    public ChatCommand(ChatService chatService) {
        this.chatService = chatService;
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

        if (args.length < 2) {
            player.sendMessage("§cUsage: /paperchat <player> <message>");
            return true;
        }

        String targetPlayer = args[0];
        StringBuilder messageBuilder = new StringBuilder();

        for (int i = 1; i < args.length; i++) {
            if (i > 1) messageBuilder.append(" ");
            messageBuilder.append(args[i]);
        }

        String message = messageBuilder.toString();

        if (message.startsWith("\"") && message.endsWith("\"") && message.length() > 1) {
            message = message.substring(1, message.length() - 1);
        }

        if (message.trim().isEmpty()) {
            player.sendMessage("§cMessage cannot be empty.");
            return true;
        }

        chatService.sendMessage(player.getName(), targetPlayer, message);
        return true;
    }
}
