package com.aura.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "chathistories")
public class ChatSession {
    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed(unique = true)
    private String sessionId;

    private String title = "New Chat";
    private List<ChatMessage> messages = new ArrayList<>();

    private Instant startedAt = Instant.now();
    private Instant lastMessageAt = Instant.now();
    private Integer messageCount = 0;
    private Boolean isActive = true;

    public ChatSession() {}

    public ChatSession(String userId, String sessionId, String title) {
        this.userId = userId;
        this.sessionId = sessionId;
        this.title = title != null ? title : "New Chat";
        this.messages = new ArrayList<>();
        this.startedAt = Instant.now();
        this.lastMessageAt = Instant.now();
        this.messageCount = 0;
        this.isActive = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<ChatMessage> getMessages() { return messages; }
    public void setMessages(List<ChatMessage> messages) { 
        this.messages = messages != null ? messages : new ArrayList<>();
        this.messageCount = this.messages.size();
    }

    public void addMessage(ChatMessage message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.add(message);
        this.messageCount = this.messages.size();
        this.lastMessageAt = Instant.now();
    }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(Instant lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    public Integer getMessageCount() { return messageCount; }
    public void setMessageCount(Integer messageCount) { this.messageCount = messageCount; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
