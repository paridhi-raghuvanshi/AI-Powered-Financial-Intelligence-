package com.aura.socket;

import com.aura.orchestrator.AgentOrchestrator;
import com.aura.service.LoggerService;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SocketIOEventHandler {

    @Autowired
    private SocketIOServer server;

    @Autowired
    private AgentOrchestrator orchestrator;

    @Autowired
    private LoggerService logger;

    // Track active client sessions
    private final Map<String, SocketIOClient> clientsBySession = new ConcurrentHashMap<>();

    @PostConstruct
    public void startServer() {
        server.addConnectListener(client -> {
            String sid = client.getSessionId().toString();
            clientsBySession.put(sid, client);
            logger.info("SOCKET", "🔌 Client connected: " + sid);
        });

        server.addDisconnectListener(client -> {
            String sid = client.getSessionId().toString();
            clientsBySession.remove(sid);
            logger.info("SOCKET", "🔌 Client disconnected: " + sid);
        });

        server.addEventListener("join-room", String.class, (client, room, ackRequest) -> {
            client.joinRoom(room);
            client.joinRoom(client.getSessionId().toString());
            logger.info("SOCKET", "Client joined room: " + room);
        });

        // Real-time Chat Message over WebSocket
        server.addEventListener("chat-message", Map.class, (client, data, ackRequest) -> {
            String message = (String) data.get("message");
            String sessionId = (String) data.getOrDefault("sessionId", "chat_" + System.currentTimeMillis());
            String requestId = "WS-" + Long.toString(System.currentTimeMillis(), 36).toUpperCase();

            logger.info("SOCKET", "💬 [" + requestId + "] Socket chat message: " + message);

            client.sendEvent("chat-start", Map.of("requestId", requestId, "message", "Processing..."));

            try {
                Map<String, Object> result = orchestrator.processChat(message, Map.of("sessionId", sessionId), progress -> {
                    Map<String, Object> progEvent = new HashMap<>(progress);
                    progEvent.put("requestId", requestId);
                    client.sendEvent("chat-progress", progEvent);
                });

                client.sendEvent("chat-response", Map.of(
                        "requestId", requestId,
                        "sessionId", sessionId,
                        "response", result.get("response"),
                        "intent", result.get("intent"),
                        "complexity", result.get("complexity"),
                        "agentsUsed", result.get("agentsUsed"),
                        "executionTime", result.get("executionTime")
                ));

                client.sendEvent("chat-complete", Map.of("requestId", requestId));

            } catch (Exception e) {
                logger.error("SOCKET", "Error in socket chat: " + e.getMessage(), e);
                client.sendEvent("chat-error", Map.of(
                        "requestId", requestId,
                        "error", e.getMessage(),
                        "response", "I encountered an issue processing your request. Please try again."
                ));
            }
        });

        // Portfolio Analysis over WebSocket
        server.addEventListener("request-analysis", Map.class, (client, data, ackRequest) -> {
            String userId = (String) data.getOrDefault("userId", "demo_user");
            String phone = (String) data.getOrDefault("phoneNumber", "2222222222");

            client.sendEvent("analysis-progress", Map.of("stage", "Connecting to Fi MCP", "progress", 10));

            try {
                Map<String, Object> analysis = orchestrator.analyzePortfolio(userId, phone, progress -> {
                    client.sendEvent("analysis-progress", progress);
                });

                client.sendEvent("analysis-complete", analysis);
            } catch (Exception e) {
                client.sendEvent("analysis-error", Map.of("error", e.getMessage()));
            }
        });

        try {
            server.start();
            logger.success("SOCKET", "Netty-SocketIO server started on port " + server.getConfiguration().getPort());
        } catch (Exception e) {
            logger.warn("SOCKET", "Could not start Netty-SocketIO server: " + e.getMessage());
        }
    }

    public void sendProgressToSocket(String socketId, Map<String, Object> progress) {
        if (socketId == null || socketId.isBlank()) return;

        // Try direct session lookup first
        SocketIOClient client = clientsBySession.get(socketId);
        if (client != null) {
            client.sendEvent("chat-progress", progress);
            return;
        }

        // Try by room broadcast
        try {
            server.getRoomOperations(socketId).sendEvent("chat-progress", progress);
        } catch (Exception ignored) {}
    }

    @PreDestroy
    public void stopServer() {
        try {
            server.stop();
            logger.info("SOCKET", "Netty-SocketIO server stopped");
        } catch (Exception ignored) {}
    }
}
