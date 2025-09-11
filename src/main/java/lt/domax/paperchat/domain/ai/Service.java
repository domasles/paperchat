package lt.domax.paperchat.domain.ai;

import java.util.concurrent.CompletableFuture;

public interface Service {
    CompletableFuture<String> sendMessage(String prompt);
    CompletableFuture<String> sendMessage(String prompt, String conversationHistory);
    
    boolean isReady();
    void shutdown();
}
