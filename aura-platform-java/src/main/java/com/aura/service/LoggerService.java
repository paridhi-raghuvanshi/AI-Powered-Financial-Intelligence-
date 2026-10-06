package com.aura.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoggerService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    // ANSI Colors
    private static final String RESET = "\u001B[0m";
    private static final String BRIGHT = "\u001B[1m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String BLUE = "\u001B[34m";

    private static final Map<String, String> EMOJIS = Map.ofEntries(
            Map.entry("STRATEGIST", "🎯"),
            Map.entry("QUANT", "🔢"),
            Map.entry("DOER", "⚡"),
            Map.entry("REALIST", "📈"),
            Map.entry("COMMUNICATOR", "💬"),
            Map.entry("ORCHESTRATOR", "🎭"),
            Map.entry("MCP", "🔗"),
            Map.entry("API", "🌐"),
            Map.entry("RAG", "📚"),
            Map.entry("MARKET", "📊"),
            Map.entry("AUTH", "🔐"),
            Map.entry("SYSTEM", "⚙️"),
            Map.entry("OPENAI", "🤖"),
            Map.entry("CHAT", "💭")
    );

    private String getTimestamp() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }

    public void info(String message) {
        info("SYSTEM", message);
    }

    public void info(String component, String message) {
        String emoji = EMOJIS.getOrDefault(component.toUpperCase(), "📝");
        System.out.printf("[%s] %s%s %-12s%s %s%n", getTimestamp(), CYAN, emoji, "[" + component + "]", RESET, message);
    }

    public void success(String message) {
        success("SYSTEM", message);
    }

    public void success(String component, String message) {
        String emoji = EMOJIS.getOrDefault(component.toUpperCase(), "✅");
        System.out.printf("[%s] %s%s %-12s%s %s%s%s%n", getTimestamp(), GREEN, emoji, "[" + component + "]", RESET, GREEN, message, RESET);
    }

    public void warn(String message) {
        warn("SYSTEM", message);
    }

    public void warn(String component, String message) {
        String emoji = EMOJIS.getOrDefault(component.toUpperCase(), "⚠️");
        System.out.printf("[%s] %s%s %-12s%s %s%s%s%n", getTimestamp(), YELLOW, emoji, "[" + component + "]", RESET, YELLOW, message, RESET);
    }

    public void error(String message, Throwable t) {
        error("SYSTEM", message, t);
    }

    public void error(String component, String message, Throwable t) {
        String emoji = EMOJIS.getOrDefault(component.toUpperCase(), "❌");
        System.out.printf("[%s] %s%s %-12s%s %s%s%s%n", getTimestamp(), RED, emoji, "[" + component + "]", RESET, RED, message, RESET);
        if (t != null) {
            t.printStackTrace(System.err);
        }
    }

    public void agent(String agentName, String message) {
        info(agentName.toUpperCase(), message);
    }

    public void mcp(String message) {
        info("MCP", message);
    }

    public void market(String message) {
        info("MARKET", message);
    }

    public void rag(String message) {
        info("RAG", message);
    }

    public void divider() {
        divider("");
    }

    public void divider(String title) {
        int width = 60;
        if (title == null || title.isBlank()) {
            System.out.println(CYAN + "─".repeat(width) + RESET);
        } else {
            int padding = Math.max(2, (width - title.length() - 2) / 2);
            System.out.println(CYAN + "─".repeat(padding) + " " + BRIGHT + title + RESET + CYAN + " " + "─".repeat(padding) + RESET);
        }
    }
}
