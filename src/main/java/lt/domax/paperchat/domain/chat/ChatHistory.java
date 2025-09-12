package lt.domax.paperchat.domain.chat;

import java.time.LocalDateTime;

public class ChatHistory {
    private String sender;
    private String message;
    private String response;

    private LocalDateTime timestamp;

    public ChatHistory(String sender, String message, String response) {
        this.sender = sender;
        this.message = message;
        this.response = response;
        this.timestamp = LocalDateTime.now();
    }

    public String getSender() { return sender; }
    public String getMessage() { return message; }
    public String getResponse() { return response; }
    public String toHistoryString() { return "User (" + sender + "): " + message + "\nAI: " + response; }

    public LocalDateTime getTimestamp() { return timestamp; }
}
