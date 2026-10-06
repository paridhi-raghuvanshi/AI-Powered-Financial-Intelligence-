package com.aura.orchestrator;

import com.aura.model.ChatMessage;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SharedContext {
    private String userQuery = "";
    private Map<String, Object> intent = new HashMap<>();
    private String ragContext = "";
    private Map<String, Object> marketData = new HashMap<>();
    private List<ChatMessage> chatHistory = new ArrayList<>();
    private final Map<String, Object> agentOutputs = new ConcurrentHashMap<>();
    private final Instant timestamp = Instant.now();

    public SharedContext() {}

    public String getUserQuery() { return userQuery; }
    public void setUserQuery(String userQuery) { this.userQuery = userQuery; }

    public Map<String, Object> getIntent() { return intent; }
    public void setIntent(Map<String, Object> intent) { this.intent = intent; }

    public String getRagContext() { return ragContext; }
    public void setRagContext(String ragContext) { this.ragContext = ragContext; }

    public Map<String, Object> getMarketData() { return marketData; }
    public void setMarketData(Map<String, Object> marketData) { this.marketData = marketData; }

    public List<ChatMessage> getChatHistory() { return chatHistory; }
    public void setChatHistory(List<ChatMessage> chatHistory) { this.chatHistory = chatHistory; }

    public void addAgentOutput(String agentName, Object output) {
        if (agentName != null && output != null) {
            agentOutputs.put(agentName.toLowerCase(), output);
        }
    }

    public Object getAgentOutput(String agentName) {
        if (agentName == null) return null;
        return agentOutputs.get(agentName.toLowerCase());
    }

    public Map<String, Object> getAllAgentOutputs() {
        return Collections.unmodifiableMap(agentOutputs);
    }

    public String toPromptContext() {
        StringBuilder sb = new StringBuilder("=== CONTEXT FROM OTHER AGENTS ===\n");
        for (Map.Entry<String, Object> entry : agentOutputs.entrySet()) {
            String outStr = entry.getValue() instanceof String s ? s : entry.getValue().toString();
            if (outStr.length() > 800) outStr = outStr.substring(0, 800) + "...";
            sb.append("\n[").append(entry.getKey().toUpperCase()).append("]:\n")
              .append(outStr).append("\n");
        }
        return sb.toString();
    }
}
