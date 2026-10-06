package com.aura.controller;

import com.aura.model.User;
import com.aura.orchestrator.AgentOrchestrator;
import com.aura.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class SystemController {

    @Autowired
    private MongoDbService mongoDbService;

    @Autowired
    private FiMCPClient fiMCPClient;

    @Autowired
    private OpenAIService openaiService;

    @Autowired
    private RAGService ragService;

    @Autowired
    private MarketDataService marketDataService;

    @Autowired
    private ObservabilityService observabilityService;

    @Autowired
    private AgentOrchestrator orchestrator;

    @Autowired
    private LoggerService logger;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> mongoStatus = mongoDbService.healthCheck();

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("status", "healthy");
        status.put("timestamp", Instant.now().toString());
        status.put("version", "2.0.0");
        status.put("platform", "Java (Spring Boot 3 + Java 21)");

        Map<String, Object> services = new LinkedHashMap<>();
        services.put("fiMCP", fiMCPClient.isConnected());
        services.put("openAI", openaiService.isAvailable());
        services.put("rag", ragService.getStatus().get("initialized"));
        services.put("marketData", marketDataService.getStatus().get("marketStatus"));
        services.put("mongodb", mongoStatus.get("connected"));
        services.put("agents", List.of("Strategist", "Quant", "Doer", "Realist", "Communicator"));
        status.put("services", services);

        return ResponseEntity.ok(status);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("platform", "AURA Financial Intelligence");
        resp.put("version", "2.0.0");
        resp.put("stack", "Java 21 / Spring Boot 3.3.4 / Netty-SocketIO");
        resp.put("developer", "Aryan Jaiswal");
        resp.put("website", "https://aryanjaiswal.in");

        Map<String, Object> services = new LinkedHashMap<>();
        services.put("openai", openaiService.getStatus());
        services.put("rag", ragService.getStatus());
        services.put("market", marketDataService.getStatus());
        services.put("mcp", fiMCPClient.getStatus());
        services.put("mongodb", mongoDbService.healthCheck());
        resp.put("services", services);

        resp.put("agents", orchestrator.getAgentStatus());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "langsmith", observabilityService.getMetricsSummary(),
                "agents", observabilityService.getAgentMetrics(),
                "timestamp", Instant.now().toString()
        ));
    }

    // User Profile APIs
    @PostMapping("/user/profile")
    public ResponseEntity<Map<String, Object>> saveUserProfile(@RequestBody User user) {
        User saved = mongoDbService.createUser(user);
        return ResponseEntity.ok(Map.of("success", true, "user", saved));
    }

    @GetMapping("/user/profile/{identifier}")
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable("identifier") String identifier) {
        Optional<User> userOpt = mongoDbService.findUser(identifier);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(Map.of("success", true, "user", userOpt.get()));
        } else {
            return ResponseEntity.status(404).body(Map.of("success", false, "error", "User not found"));
        }
    }

    // Portfolio Analysis REST endpoint (called by frontend app.js performAnalysisViaREST)
    @PostMapping("/analyze-portfolio")
    public ResponseEntity<Map<String, Object>> analyzePortfolioREST(@RequestBody Map<String, Object> body) {
        String userId = (String) body.getOrDefault("userId", "demo_user");
        String phone = (String) body.getOrDefault("phoneNumber", "2222222222");

        logger.info("PORTFOLIO", "REST Portfolio Analysis request for " + phone);
        Map<String, Object> results = orchestrator.analyzePortfolio(userId, phone, null);
        return ResponseEntity.ok(results);
    }
}
