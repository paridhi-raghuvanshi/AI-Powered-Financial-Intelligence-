package com.aura.service;

import com.aura.model.ChatMessage;
import com.aura.model.ChatSession;
import com.aura.model.User;
import com.aura.repository.ChatSessionRepository;
import com.aura.repository.InMemoryFallbackStore;
import com.aura.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class MongoDbService {

    @Autowired(required = false)
    private UserRepository userRepository;

    @Autowired(required = false)
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private InMemoryFallbackStore inMemoryStore;

    @Autowired
    private LoggerService logger;

    private boolean isMongoAvailable() {
        return userRepository != null && chatSessionRepository != null;
    }

    public boolean isConnected() {
        if (!isMongoAvailable()) return false;
        try {
            userRepository.count();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> healthCheck() {
        Map<String, Object> status = new HashMap<>();
        boolean connected = isConnected();
        status.put("connected", connected);
        status.put("mode", connected ? "mongodb" : "in-memory-fallback");
        return status;
    }

    // User Operations
    public User createUser(User user) {
        if (isMongoAvailable()) {
            try {
                User saved = userRepository.save(user);
                inMemoryStore.saveUser(saved);
                return saved;
            } catch (Exception e) {
                logger.warn("MONGO", "Failed to save user to MongoDB, falling back to in-memory: " + e.getMessage());
            }
        }
        return inMemoryStore.saveUser(user);
    }

    public Optional<User> findUser(String identifier) {
        if (identifier == null) return Optional.empty();
        String idLower = identifier.toLowerCase();
        
        if (isMongoAvailable()) {
            try {
                Optional<User> byEmail = userRepository.findByEmail(idLower);
                if (byEmail.isPresent()) return byEmail;

                Optional<User> byPhone = userRepository.findByPhoneNumber(identifier);
                if (byPhone.isPresent()) return byPhone;

                Optional<User> byUid = userRepository.findByFirebaseUid(identifier);
                if (byUid.isPresent()) return byUid;
            } catch (Exception e) {
                logger.warn("MONGO", "MongoDB query failed, falling back: " + e.getMessage());
            }
        }
        return inMemoryStore.findUser(identifier);
    }

    public User updateUser(String identifier, Map<String, Object> updates) {
        Optional<User> userOpt = findUser(identifier);
        User user = userOpt.orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(identifier.contains("@") ? identifier : null);
            return newUser;
        });

        if (updates.containsKey("lastLoginAt")) {
            user.setLastLoginAt(Instant.now());
        }
        if (updates.containsKey("onboardingComplete")) {
            user.setOnboardingComplete((Boolean) updates.get("onboardingComplete"));
        }
        if (updates.containsKey("photoURL")) {
            user.setPhotoURL((String) updates.get("photoURL"));
        }
        if (updates.containsKey("age")) {
            Object age = updates.get("age");
            if (age instanceof Number n) user.setAge(n.intValue());
        }
        if (updates.containsKey("monthlyIncome")) {
            Object inc = updates.get("monthlyIncome");
            if (inc instanceof Number n) user.setMonthlyIncome(n.doubleValue());
        }
        if (updates.containsKey("monthlyExpenses")) {
            Object exp = updates.get("monthlyExpenses");
            if (exp instanceof Number n) user.setMonthlyExpenses(n.doubleValue());
        }
        if (updates.containsKey("riskTolerance")) {
            user.setRiskTolerance((String) updates.get("riskTolerance"));
        }

        user.setUpdatedAt(Instant.now());
        return createUser(user);
    }

    // Chat Operations
    public ChatSession createNewChat(String userId, String title) {
        String sessionId = "chat_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 9);
        ChatSession session = new ChatSession(userId, sessionId, title != null ? title : "New Chat");
        
        if (isMongoAvailable()) {
            try {
                ChatSession saved = chatSessionRepository.save(session);
                inMemoryStore.saveSession(saved);
                return saved;
            } catch (Exception e) {
                logger.warn("MONGO", "Failed to save chat session to MongoDB: " + e.getMessage());
            }
        }
        return inMemoryStore.saveSession(session);
    }

    public Optional<ChatSession> getChatSession(String sessionId, String userId) {
        if (isMongoAvailable()) {
            try {
                Optional<ChatSession> sessionOpt = chatSessionRepository.findBySessionId(sessionId);
                if (sessionOpt.isPresent()) return sessionOpt;
            } catch (Exception e) {
                logger.warn("MONGO", "MongoDB session fetch failed: " + e.getMessage());
            }
        }
        return inMemoryStore.findSession(sessionId);
    }

    public List<ChatMessage> getChatHistory(String sessionId) {
        return getChatSession(sessionId, null)
                .map(ChatSession::getMessages)
                .orElse(Collections.emptyList());
    }

    public List<ChatMessage> getRecentContext(String sessionId, int limit) {
        List<ChatMessage> all = getChatHistory(sessionId);
        if (all.size() <= limit) return all;
        return all.subList(all.size() - limit, all.size());
    }

    public List<ChatSession> getUserChatSessions(String userId, int limit) {
        if (isMongoAvailable()) {
            try {
                List<ChatSession> sessions = chatSessionRepository.findByUserIdOrderByLastMessageAtDesc(userId);
                if (!sessions.isEmpty()) {
                    return sessions.stream().limit(limit).toList();
                }
            } catch (Exception e) {
                logger.warn("MONGO", "MongoDB user sessions fetch failed: " + e.getMessage());
            }
        }
        return inMemoryStore.getUserSessions(userId, limit);
    }

    public void saveMessage(String sessionId, ChatMessage message, String userId) {
        ChatSession session = getChatSession(sessionId, userId).orElseGet(() -> {
            ChatSession newSession = new ChatSession(userId, sessionId, "New Chat");
            return inMemoryStore.saveSession(newSession);
        });

        session.addMessage(message);

        if (isMongoAvailable()) {
            try {
                chatSessionRepository.save(session);
            } catch (Exception e) {
                logger.warn("MONGO", "Could not persist message to MongoDB: " + e.getMessage());
            }
        }
        inMemoryStore.saveSession(session);
    }

    public boolean clearChat(String sessionId) {
        if (isMongoAvailable()) {
            try {
                Optional<ChatSession> sessionOpt = chatSessionRepository.findBySessionId(sessionId);
                if (sessionOpt.isPresent()) {
                    ChatSession session = sessionOpt.get();
                    session.getMessages().clear();
                    session.setMessageCount(0);
                    chatSessionRepository.save(session);
                }
            } catch (Exception e) {
                logger.warn("MONGO", "MongoDB clear chat failed: " + e.getMessage());
            }
        }
        return inMemoryStore.clearSession(sessionId);
    }

    public boolean deleteChat(String sessionId) {
        if (isMongoAvailable()) {
            try {
                chatSessionRepository.findBySessionId(sessionId).ifPresent(s -> chatSessionRepository.delete(s));
            } catch (Exception e) {
                logger.warn("MONGO", "MongoDB delete chat failed: " + e.getMessage());
            }
        }
        return inMemoryStore.deleteSession(sessionId);
    }
}
