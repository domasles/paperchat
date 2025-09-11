package lt.domax.paperchat.domain.player;

import lt.domax.paperchat.domain.chat.ChatSession;

import java.util.HashMap;
import java.util.Map;

public class PlayerChatManager {
    private final Map<String, ChatSession> sessions;
    private final int maxHistory;

    public PlayerChatManager() {
        this.sessions = new HashMap<>();
        this.maxHistory = 10;
    }

    public PlayerChatManager(int maxHistory) {
        this.sessions = new HashMap<>();
        this.maxHistory = maxHistory;
    }

    public ChatSession getOrCreateSession(String playerName) {
        return sessions.computeIfAbsent(playerName, 
            name -> new ChatSession(name, maxHistory));
    }

    public ChatSession getSession(String playerName) { return sessions.get(playerName); }

    public void removeSession(String playerName) { sessions.remove(playerName); }
    public void clearAllSessions() { sessions.clear(); }

    public int getActiveSessionsCount() { return sessions.size(); }
}
