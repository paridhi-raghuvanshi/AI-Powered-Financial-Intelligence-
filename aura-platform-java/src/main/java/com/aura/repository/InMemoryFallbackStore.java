package com.aura.repository;

import com.aura.model.ChatMessage;
import com.aura.model.ChatSession;
import com.aura.model.User;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class InMemoryFallbackStore {
    private final Map<String, User> usersByIdentifier = new ConcurrentHashMap<>();
    private final Map<String, ChatSession> sessionsById = new ConcurrentHashMap<>();

    public User saveUser(User user) {
        if (user.getId() == null) {
            user.setId("user_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8));
        }
        user.setUpdatedAt(Instant.now());
        
        if (user.getEmail() != null) {
            usersByIdentifier.put(user.getEmail().toLowerCase(), user);
        }
        if (user.getPhoneNumber() != null) {
            usersByIdentifier.put(user.getPhoneNumber(), user);
        }
        if (user.getFirebaseUid() != null) {
            usersByIdentifier.put(user.getFirebaseUid(), user);
        }
        usersByIdentifier.put(user.getId(), user);
        return user;
    }

    public Optional<User> findUser(String identifier) {
        if (identifier == null) return Optional.empty();
        return Optional.ofNullable(usersByIdentifier.get(identifier.toLowerCase()));
    }

    public ChatSession saveSession(ChatSession session) {
        sessionsById.put(session.getSessionId(), session);
        return session;
    }

    public Optional<ChatSession> findSession(String sessionId) {
        if (sessionId == null) return Optional.empty();
        return Optional.ofNullable(sessionsById.get(sessionId));
    }

    public List<ChatSession> getUserSessions(String userId, int limit) {
        return sessionsById.values().stream()
                .filter(s -> userId == null || userId.equals(s.getUserId()))
                .sorted((a, b) -> b.getLastMessageAt().compareTo(a.getLastMessageAt()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public boolean deleteSession(String sessionId) {
        return sessionsById.remove(sessionId) != null;
    }

    public boolean clearSession(String sessionId) {
        ChatSession session = sessionsById.get(sessionId);
        if (session != null) {
            session.getMessages().clear();
            session.setMessageCount(0);
            return true;
        }
        return false;
    }
}
