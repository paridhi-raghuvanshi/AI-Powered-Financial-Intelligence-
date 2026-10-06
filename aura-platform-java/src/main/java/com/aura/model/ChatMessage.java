package com.aura.model;

import java.time.Instant;
import java.util.List;

public class ChatMessage {
    private String role; // 'user' or 'assistant'
    private String content;
    private Instant timestamp = Instant.now();

    // Agent metadata (for assistant messages)
    private List<String> agentsUsed;
    private String intent;
    private String complexity;
    private String executionTime;

    public ChatMessage() {}

    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = Instant.now();
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public List<String> getAgentsUsed() { return agentsUsed; }
    public void setAgentsUsed(List<String> agentsUsed) { this.agentsUsed = agentsUsed; }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public String getComplexity() { return complexity; }
    public void setComplexity(String complexity) { this.complexity = complexity; }

    public String getExecutionTime() { return executionTime; }
    public void setExecutionTime(String executionTime) { this.executionTime = executionTime; }
}
