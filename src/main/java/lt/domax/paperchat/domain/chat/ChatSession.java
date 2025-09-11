package lt.domax.paperchat.domain.chat;

import java.util.ArrayList;
import java.util.List;

public class ChatSession {
    private final String sessionId;
    private final List<ChatHistory> history;
    private final int maxHistory;

    public ChatSession(String sessionId, int maxHistory) {
        this.sessionId = sessionId;
        this.history = new ArrayList<>();
        this.maxHistory = maxHistory;
    }

    public void addMessage(String sender, String message, String response) {
        ChatHistory chatHistory = new ChatHistory(sender, message, response);
        history.add(chatHistory);

        while (history.size() > maxHistory) {
            history.remove(0);
        }
    }

    public String getConversationHistory() {
        StringBuilder sb = new StringBuilder();

        for (ChatHistory entry : history) {
            sb.append(entry.toHistoryString()).append("\n\n");
        }

        return sb.toString().trim();
    }

    public String getSessionId() { return sessionId; }

    public int getHistorySize() { return history.size(); }
    public boolean hasHistory() { return !history.isEmpty(); }
}
