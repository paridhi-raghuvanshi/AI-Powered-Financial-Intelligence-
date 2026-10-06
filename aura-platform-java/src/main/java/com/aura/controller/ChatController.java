package com.aura.controller;

import com.aura.model.ChatMessage;
import com.aura.model.ChatSession;
import com.aura.orchestrator.AgentOrchestrator;
import com.aura.service.LoggerService;
import com.aura.service.MongoDbService;
import com.aura.socket.SocketIOEventHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private AgentOrchestrator orchestrator;

    @Autowired
    private MongoDbService mongoDbService;

    @Autowired
    private SocketIOEventHandler socketHandler;

    @Autowired
    private LoggerService logger;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, Object> body) {
        String message = (String) body.get("message");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
        }

        String sessionId = (String) body.get("sessionId");
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = "chat_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        }

        String socketId = (String) body.get("socketId");
        String userId = (String) body.getOrDefault("userId", "anonymous");
        String requestId = "CHAT-" + Long.toString(System.currentTimeMillis(), 36).toUpperCase();

        logger.info("CHAT", String.format("💬 [%s] Chat: \"%s...\"", requestId, message.length() > 50 ? message.substring(0, 50) : message));

        // Get chat context
        List<ChatMessage> chatContext = mongoDbService.getRecentContext(sessionId, 20);

        // Save user message
        mongoDbService.saveMessage(sessionId, new ChatMessage("user", message), userId);

        final String finalSessionId = sessionId;
        final String finalRequestId = requestId;

        // Process chat with agent orchestrator
        Map<String, Object> result = orchestrator.processChat(
                message,
                Map.of("chatHistory", chatContext, "userId", userId, "sessionId", finalSessionId),
                progress -> {
                    if (socketId != null && !socketId.isBlank()) {
                        Map<String, Object> progEvent = new HashMap<>(progress);
                        progEvent.put("requestId", finalRequestId);
                        socketHandler.sendProgressToSocket(socketId, progEvent);
                    }
                }
        );

        String responseText = (String) result.getOrDefault("response", "I could not generate an answer. Please rephrase.");
        @SuppressWarnings("unchecked")
        List<String> agentsUsed = (List<String>) result.getOrDefault("agentsUsed", List.of());

        // Save assistant response
        ChatMessage assistantMessage = new ChatMessage("assistant", responseText);
        assistantMessage.setAgentsUsed(agentsUsed);
        assistantMessage.setIntent((String) result.get("intent"));
        assistantMessage.setComplexity((String) result.get("complexity"));
        assistantMessage.setExecutionTime((String) result.get("executionTime"));
        mongoDbService.saveMessage(finalSessionId, assistantMessage, userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", result.get("success"));
        response.put("response", responseText);
        response.put("sessionId", finalSessionId);
        response.put("requestId", finalRequestId);
        response.put("intent", result.get("intent"));
        response.put("complexity", result.get("complexity"));
        response.put("agentsUsed", agentsUsed);
        response.put("executionTime", result.get("executionTime"));
        response.put("agentActivity", result.get("agentActivity"));
        response.put("messageCount", chatContext.size() + 2);

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam("message") String message) {
        SseEmitter emitter = new SseEmitter(60_000L);
        String requestId = "STREAM-" + Long.toString(System.currentTimeMillis(), 36).toUpperCase();

        Thread.ofVirtual().start(() -> {
            try {
                emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(Map.of("type", "start", "requestId", requestId))));

                Map<String, Object> result = orchestrator.processChat(message, Map.of(), progress -> {
                    try {
                        Map<String, Object> p = new HashMap<>(progress);
                        p.put("type", "progress");
                        emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(p)));
                    } catch (IOException ignored) {}
                });

                emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(Map.of(
                        "type", "response",
                        "response", result.get("response"),
                        "agentsUsed", result.get("agentsUsed"),
                        "executionTime", result.get("executionTime")
                ))));

                emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(Map.of("type", "complete", "requestId", requestId))));
                emitter.complete();

            } catch (Exception e) {
                try {
                    emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(Map.of("type", "error", "error", e.getMessage()))));
                } catch (IOException ignored) {}
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @PostMapping("/new")
    public ResponseEntity<Map<String, Object>> createNewChat(@RequestBody Map<String, Object> body) {
        String userId = (String) body.getOrDefault("userId", "anonymous");
        String title = (String) body.getOrDefault("title", "New Chat");
        ChatSession session = mongoDbService.createNewChat(userId, title);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "sessionId", session.getSessionId(),
                "chat", session
        ));
    }

    @GetMapping("/sessions/{userId}")
    public ResponseEntity<Map<String, Object>> getUserSessions(@PathVariable("userId") String userId) {
        List<ChatSession> sessions = mongoDbService.getUserChatSessions(userId, 20);
        return ResponseEntity.ok(Map.of("success", true, "sessions", sessions));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSession(@PathVariable("sessionId") String sessionId,
                                                          @RequestParam(value = "userId", required = false) String userId) {
        Optional<ChatSession> sessionOpt = mongoDbService.getChatSession(sessionId, userId);
        if (sessionOpt.isPresent()) {
            return ResponseEntity.ok(Map.of("success", true, "session", sessionOpt.get()));
        } else {
            return ResponseEntity.ok(Map.of("success", false, "error", "Session not found"));
        }
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<Map<String, Object>> getHistory(@PathVariable("sessionId") String sessionId) {
        List<ChatMessage> messages = mongoDbService.getChatHistory(sessionId);
        return ResponseEntity.ok(Map.of("success", true, "messages", messages));
    }

    @PostMapping("/clear/{sessionId}")
    public ResponseEntity<Map<String, Object>> clearChat(@PathVariable("sessionId") String sessionId) {
        boolean success = mongoDbService.clearChat(sessionId);
        return ResponseEntity.ok(Map.of("success", success));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Map<String, Object>> deleteChat(@PathVariable("sessionId") String sessionId) {
        boolean success = mongoDbService.deleteChat(sessionId);
        return ResponseEntity.ok(Map.of("success", success));
    }
}
