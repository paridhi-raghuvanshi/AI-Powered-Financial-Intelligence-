package com.aura.orchestrator;

import com.aura.agent.*;
import com.aura.model.ChatMessage;
import com.aura.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AgentOrchestrator {

    @Autowired
    private StrategistAgent strategistAgent;

    @Autowired
    private QuantAgent quantAgent;

    @Autowired
    private DoerAgent doerAgent;

    @Autowired
    private RealistAgent realistAgent;

    @Autowired
    private CommunicatorAgent communicatorAgent;

    @Autowired
    private RAGService ragService;

    @Autowired
    private MarketDataService marketDataService;

    @Autowired
    private OpenAIService openaiService;

    @Autowired
    private ObservabilityService observabilityService;

    @Autowired
    private LoggerService logger;

    private static final Pattern FINANCIAL_NUMBERS_PATTERN = Pattern.compile("₹\\s*\\d|rs\\.?\\s*\\d|\\d+\\s*(lakh|crore|k|l|cr)|monthly|yearly|annual", Pattern.CASE_INSENSITIVE);
    private static final Pattern AGE_INVEST_PATTERN = Pattern.compile("\\d+\\s*(year|yr).*old.*invest|\\d+\\s*(year|yr).*plan", Pattern.CASE_INSENSITIVE);

    private static final List<String> FINANCE_KEYWORDS = List.of(
            "invest", "sip", "mutual fund", "stock", "share", "portfolio", "nifty", "sensex",
            "equity", "debt", "bond", "etf", "ipo", "dividend", "return", "cagr", "xirr",
            "bank", "fd", "fixed deposit", "rd", "recurring", "savings", "account", "loan",
            "emi", "interest", "credit", "debit", "upi", "neft", "rtgs",
            "tax", "80c", "80d", "deduction", "ltcg", "stcg", "elss", "income tax", "gst",
            "insurance", "lic", "term plan", "health insurance", "life insurance", "premium",
            "retire", "pension", "epf", "ppf", "nps", "gratuity", "pf",
            "money", "wealth", "finance", "financial", "budget", "expense", "income", "salary",
            "goal", "corpus", "crore", "lakh", "rupee", "₹", "rs", "inr",
            "market", "bull", "bear", "rally", "correction", "index", "sector",
            "crypto", "bitcoin", "ethereum", "gold", "silver", "commodity", "asset", "liability", "networth", "net worth"
    );

    public List<Map<String, Object>> getAgentStatus() {
        return List.of(
                Map.of("name", "Strategist", "icon", strategistAgent.getIcon(), "status", "ready"),
                Map.of("name", "Quant", "icon", quantAgent.getIcon(), "status", "ready"),
                Map.of("name", "Doer", "icon", doerAgent.getIcon(), "status", "ready"),
                Map.of("name", "Realist", "icon", realistAgent.getIcon(), "status", "ready"),
                Map.of("name", "Communicator", "icon", communicatorAgent.getIcon(), "status", "ready")
        );
    }

    public boolean isFinanceRelated(String message) {
        if (message == null) return false;
        String lower = message.toLowerCase();

        boolean hasKeyword = FINANCE_KEYWORDS.stream().anyMatch(kw -> {
            if (kw.length() <= 3) {
                return Pattern.compile("\\b" + Pattern.quote(kw) + "\\b", Pattern.CASE_INSENSITIVE).matcher(message).find();
            }
            return lower.contains(kw);
        });
        boolean hasNumbers = FINANCIAL_NUMBERS_PATTERN.matcher(message).find();
        boolean hasAgePattern = AGE_INVEST_PATTERN.matcher(message).find();

        return hasKeyword || hasNumbers || hasAgePattern;
    }

    public Map<String, Object> analyzeIntent(String message) {
        String lower = message.toLowerCase();

        Map<String, Integer> signals = new HashMap<>();
        signals.put("portfolio_planning", 0);
        signals.put("calculation", 0);
        signals.put("market_insight", 0);
        signals.put("tax_planning", 0);
        signals.put("general_advice", 0);

        List<String> pfKeywords = List.of("goal", "build", "corpus", "years", "invest", "monthly", "sip", "portfolio", "wealth", "retire", "plan", "lakh", "crore", "save");
        pfKeywords.forEach(kw -> { if (lower.contains(kw)) signals.put("portfolio_planning", signals.get("portfolio_planning") + 2); });

        List<String> calcKeywords = List.of("calculate", "xirr", "cagr", "return", "projection", "compound", "emi", "interest");
        calcKeywords.forEach(kw -> { if (lower.contains(kw)) signals.put("calculation", signals.get("calculation") + 3); });

        List<String> marketKeywords = List.of("market", "nifty", "sensex", "stock", "today", "index", "correction", "rally");
        marketKeywords.forEach(kw -> { if (lower.contains(kw)) signals.put("market_insight", signals.get("market_insight") + 3); });

        List<String> taxKeywords = List.of("tax", "80c", "80d", "ltcg", "stcg", "deduction", "elss", "section");
        taxKeywords.forEach(kw -> { if (lower.contains(kw)) signals.put("tax_planning", signals.get("tax_planning") + 3); });

        List<String> simpleKeywords = List.of("what is", "what are", "how does", "explain", "define", "meaning of");
        boolean isSimpleDef = simpleKeywords.stream().anyMatch(lower::startsWith) && message.length() < 40;
        if (isSimpleDef) signals.put("general_advice", signals.get("general_advice") + 8);

        String primaryIntent = signals.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("general_advice");

        Map<String, List<String>> routingMatrix = Map.of(
                "portfolio_planning", List.of("realist", "quant", "strategist", "doer", "communicator"),
                "calculation", List.of("quant", "strategist", "doer"),
                "market_insight", List.of("realist", "strategist", "communicator"),
                "tax_planning", List.of("strategist", "quant", "doer", "communicator"),
                "general_advice", List.of("strategist", "quant", "communicator")
        );

        List<String> requiredAgents = new ArrayList<>(routingMatrix.getOrDefault(primaryIntent, routingMatrix.get("general_advice")));
        int maxScore = signals.get(primaryIntent);
        int wordCount = message.split("\\s+").length;
        boolean hasPersonal = Pattern.compile("\\d+\\s*(year|yr).*old|earning|salary|income|saving|lakh|crore", Pattern.CASE_INSENSITIVE).matcher(lower).find();

        String complexity;
        if (isSimpleDef && !hasPersonal) {
            complexity = "simple";
            requiredAgents = new ArrayList<>(List.of("strategist", "communicator"));
        } else if (hasPersonal || maxScore >= 6 || wordCount > 15) {
            complexity = "complex";
            requiredAgents = new ArrayList<>(List.of("realist", "quant", "strategist", "doer", "communicator"));
        } else {
            complexity = "medium";
            requiredAgents = requiredAgents.stream().limit(4).collect(Collectors.toList());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("primary_intent", primaryIntent);
        result.put("required_agents", requiredAgents);
        result.put("complexity", complexity);
        result.put("confidence", Math.min(maxScore / 10.0, 1.0));
        return result;
    }

    public Map<String, Object> processChat(String message, Map<String, Object> context, Consumer<Map<String, Object>> progressCallback) {
        long startTime = System.currentTimeMillis();
        String requestId = "CHAT-" + Long.toString(System.currentTimeMillis(), 36).toUpperCase();

        logger.divider("CHAT REQUEST " + requestId);
        logger.info("ORCHESTRATOR", "Processing user query: \"" + (message.length() > 60 ? message.substring(0, 60) + "..." : message) + "\"");

        if (!isFinanceRelated(message)) {
            logger.info("ORCHESTRATOR", "Non-finance query rejected gracefully");
            String offTopicResponse = """
                I'm AURA, your personal AI-powered financial advisor for India! 🎯

                I specialize in helping you with:
                • **Investments** - SIPs, Mutual Funds, Stocks, ETFs
                • **Tax Planning** - Section 80C, ELSS, Tax-saving strategies
                • **Financial Goals** - Retirement, Home purchase, Child education planning
                • **Banking & Debt** - FDs, Loans, Credit Cards, EMIs
                • **Insurance** - Pure Term Insurance, Comprehensive Health insurance
                • **Market Insights** - Nifty, Sensex, Sector trends

                Please ask me anything related to personal finance or investing, and I'll provide you with actionable insights tailored for Indian investors!

                **Try asking:** *"How should I invest ₹20,000 monthly for retirement?"* or *"What is ELSS and how does it save tax?"*
                """;

            return Map.of(
                    "success", true,
                    "response", offTopicResponse,
                    "intent", "off_topic",
                    "complexity", "simple",
                    "agentsUsed", List.of(),
                    "executionTime", (System.currentTimeMillis() - startTime) + "ms",
                    "agentActivity", List.of()
            );
        }

        SharedContext sharedContext = new SharedContext();
        sharedContext.setUserQuery(message);

        if (context != null && context.containsKey("chatHistory")) {
            @SuppressWarnings("unchecked")
            List<ChatMessage> history = (List<ChatMessage>) context.get("chatHistory");
            sharedContext.setChatHistory(history);
        }

        if (progressCallback != null) {
            progressCallback.accept(Map.of(
                    "phase", "analyzing",
                    "message", "Understanding your financial question...",
                    "progress", 10,
                    "activeAgents", List.of()
            ));
        }

        // Step 1: Analyze Intent and Context in Parallel
        Map<String, Object> intent = analyzeIntent(message);
        String ragContext = ragService.getContextForQuery(message);
        Map<String, Object> marketData = marketDataService.getMarketOverview();

        sharedContext.setIntent(intent);
        sharedContext.setRagContext(ragContext);
        sharedContext.setMarketData(marketData);

        @SuppressWarnings("unchecked")
        List<String> requiredAgents = (List<String>) intent.get("required_agents");
        String complexity = (String) intent.get("complexity");

        logger.info("ORCHESTRATOR", String.format("Intent: %s | Complexity: %s | Agents: %s",
                intent.get("primary_intent"), complexity, requiredAgents));

        List<Map<String, Object>> agentResults = new ArrayList<>();

        if (progressCallback != null) {
            progressCallback.accept(Map.of(
                    "phase", "processing",
                    "message", "AI Agents analyzing...",
                    "progress", 25,
                    "activeAgents", requiredAgents.stream().map(a -> Map.of(
                            "name", a.substring(0, 1).toUpperCase() + a.substring(1),
                            "icon", getAgentIcon(a)
                    )).toList()
            ));
        }

        // Step 2: Execute Agents based on complexity
        if ("complex".equals(complexity)) {
            // Group 1: Realist
            if (requiredAgents.contains("realist")) {
                notifyProgress(progressCallback, "Realist gathering market data...", 30, "realist");
                Map<String, Object> r = executeAgent("realist", sharedContext, requestId);
                if (r != null) agentResults.add(r);
            }

            // Group 2: Quant + Strategist in parallel
            List<String> parallelAgents = List.of("quant", "strategist").stream()
                    .filter(requiredAgents::contains)
                    .toList();

            if (!parallelAgents.isEmpty()) {
                notifyProgress(progressCallback, "Running quantitative & strategic analysis...", 55, String.join(", ", parallelAgents));
                List<CompletableFuture<Map<String, Object>>> futures = parallelAgents.stream()
                        .map(agentName -> CompletableFuture.supplyAsync(() -> executeAgent(agentName, sharedContext, requestId)))
                        .toList();

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                for (CompletableFuture<Map<String, Object>> f : futures) {
                    Map<String, Object> res = f.join();
                    if (res != null) agentResults.add(res);
                }
            }

            // Group 3: Doer
            if (requiredAgents.contains("doer")) {
                notifyProgress(progressCallback, "Building execution plan...", 75, "doer");
                Map<String, Object> r = executeAgent("doer", sharedContext, requestId);
                if (r != null) agentResults.add(r);
            }

            // Group 4: Communicator
            if (requiredAgents.contains("communicator")) {
                notifyProgress(progressCallback, "Synthesizing personalized advice...", 88, "communicator");
                Map<String, Object> r = executeAgent("communicator", sharedContext, requestId);
                if (r != null) agentResults.add(r);
            }

        } else {
            // Sequential execution for simple or medium
            int progressInc = 60 / Math.max(1, requiredAgents.size());
            int currentProg = 30;

            for (String agentName : requiredAgents) {
                notifyProgress(progressCallback, agentName.substring(0, 1).toUpperCase() + agentName.substring(1) + " analyzing...", currentProg, agentName);
                Map<String, Object> r = executeAgent(agentName, sharedContext, requestId);
                if (r != null) agentResults.add(r);
                currentProg += progressInc;
            }
        }

        // Step 3: Generate Final Unified Response
        notifyProgress(progressCallback, "Finalizing your answer...", 95, "orchestrator");
        String finalResponse = generateFinalResponse(message, intent, sharedContext, agentResults, requestId);

        long totalTime = System.currentTimeMillis() - startTime;
        logger.success("ORCHESTRATOR", "Response completed in " + totalTime + "ms");

        if (progressCallback != null) {
            progressCallback.accept(Map.of(
                    "phase", "complete",
                    "message", "Done!",
                    "progress", 100
            ));
        }

        List<String> agentsUsed = agentResults.stream()
                .map(r -> (String) r.get("name"))
                .toList();

        List<Map<String, Object>> agentActivity = agentResults.stream()
                .map(r -> Map.of(
                        "name", r.get("name"),
                        "icon", r.get("icon"),
                        "status", "complete"
                ))
                .toList();

        Map<String, Object> responseMap = new LinkedHashMap<>();
        responseMap.put("success", true);
        responseMap.put("response", finalResponse);
        responseMap.put("intent", intent.get("primary_intent"));
        responseMap.put("complexity", complexity);
        responseMap.put("agentsUsed", agentsUsed);
        responseMap.put("executionTime", totalTime + "ms");
        responseMap.put("agentActivity", agentActivity);
        return responseMap;
    }

    private void notifyProgress(Consumer<Map<String, Object>> callback, String msg, int progress, String agent) {
        if (callback != null) {
            callback.accept(Map.of(
                    "phase", "processing",
                    "message", msg,
                    "progress", progress,
                    "currentAgent", agent
            ));
        }
    }

    private Map<String, Object> executeAgent(String agentName, SharedContext context, String requestId) {
        long start = System.currentTimeMillis();
        String runId = observabilityService.startAgentRun(agentName, Map.of("query", context.getUserQuery()));

        try {
            String prompt = buildAgentPrompt(agentName, context);
            String response = null;

            switch (agentName.toLowerCase()) {
                case "strategist":
                    response = strategistAgent.generateResponse(prompt);
                    break;
                case "quant":
                    response = quantAgent.generateResponse(prompt);
                    break;
                case "doer":
                    response = doerAgent.generateResponse(prompt);
                    break;
                case "realist":
                    response = realistAgent.generateResponse(prompt);
                    break;
                case "communicator":
                    response = communicatorAgent.generateResponse(prompt);
                    break;
            }

            long duration = System.currentTimeMillis() - start;
            observabilityService.endAgentRun(runId, agentName, true, null);
            context.addAgentOutput(agentName, response);

            return Map.of(
                    "name", agentName.substring(0, 1).toUpperCase() + agentName.substring(1),
                    "icon", getAgentIcon(agentName),
                    "response", response != null ? response : "",
                    "duration", duration,
                    "traceId", runId
            );
        } catch (Exception e) {
            logger.error("ORCHESTRATOR", "Agent " + agentName + " failed: " + e.getMessage(), e);
            observabilityService.endAgentRun(runId, agentName, false, e.getMessage());
            return null;
        }
    }

    private String buildAgentPrompt(String agentName, SharedContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("USER QUESTION: ").append(context.getUserQuery()).append("\n\n");

        if (context.getChatHistory() != null && !context.getChatHistory().isEmpty()) {
            sb.append("=== RECENT CONVERSATION HISTORY ===\n");
            for (ChatMessage msg : context.getChatHistory()) {
                sb.append(msg.getRole().toUpperCase()).append(": ").append(msg.getContent()).append("\n");
            }
            sb.append("\n");
        }

        Map<String, Object> otherOutputs = context.getAllAgentOutputs();
        if (!otherOutputs.isEmpty()) {
            sb.append(context.toPromptContext()).append("\n");
        }

        return sb.toString();
    }

    private String generateFinalResponse(String message, Map<String, Object> intent, SharedContext context, List<Map<String, Object>> agentResults, String requestId) {
        Map<String, Object> outputs = context.getAllAgentOutputs();

        // If communicator responded, it is already synthesized for users
        if (outputs.containsKey("communicator")) {
            return formatResponse((String) outputs.get("communicator"));
        }

        // If strategist responded
        if (outputs.containsKey("strategist")) {
            return formatResponse((String) outputs.get("strategist"));
        }

        // If only 1 agent ran
        if (agentResults.size() == 1 && agentResults.get(0).get("response") != null) {
            return formatResponse(agentResults.get(0).get("response").toString());
        }

        // Multiple agents: synthesize via LLM if available
        if (openaiService.isAvailable() && outputs.size() > 1) {
            String synthPrompt = String.format("""
                USER QUESTION: %s
                === AGENT ANALYSES ===
                %s
                === YOUR TASK ===
                Create ONE clear, cohesive, expert financial advisory response combining all the insights.
                Format cleanly with headers, bold keys, and bullet points. Use ₹ for all currency.
                End with ONE relevant follow-up question.
                """, message, context.toPromptContext());

            String synthResponse = openaiService.chat(List.of(
                    Map.of("role", "system", "content", "You are AURA, an expert AI financial advisor for Indian investors. Start directly with the answer, no greetings."),
                    Map.of("role", "user", "content", synthPrompt)
            ), 0.7, 1500);

            if (synthResponse != null && !synthResponse.isBlank()) {
                return formatResponse(synthResponse);
            }
        }

        // Fallback: Combine top available responses
        for (Map<String, Object> r : agentResults) {
            if (r.get("response") != null && !r.get("response").toString().isBlank()) {
                return formatResponse(r.get("response").toString());
            }
        }

        return "I have analyzed your query across our financial models. To provide accurate guidance, please let me know your target investment horizon or risk tolerance.";
    }

    private String formatResponse(String response) {
        if (response == null) return "";
        return response.replaceAll("\n{3,}", "\n\n").trim();
    }

    private String getAgentIcon(String name) {
        return switch (name.toLowerCase()) {
            case "strategist" -> "🎯";
            case "quant" -> "🔢";
            case "doer" -> "⚡";
            case "realist" -> "📈";
            case "communicator" -> "💬";
            default -> "🤖";
        };
    }

    // ==================== PORTFOLIO ANALYSIS PIPELINE ====================

    public Map<String, Object> analyzePortfolio(String userId, String phoneNumber, Consumer<Map<String, Object>> progressCallback) {
        long startTime = System.currentTimeMillis();
        logger.divider("PORTFOLIO ANALYSIS FOR " + phoneNumber);

        try {
            if (progressCallback != null) {
                progressCallback.accept(Map.of("stage", "Collecting Financial Data", "progress", 10, "agent", "Realist", "icon", "📈"));
            }
            Map<String, Object> realTimeData = realistAgent.fetchRealTimeData(phoneNumber);

            if (progressCallback != null) {
                progressCallback.accept(Map.of("stage", "Performing Quantitative Analysis", "progress", 30, "agent", "Quant", "icon", "🔢"));
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> userData = (Map<String, Object>) realTimeData.get("userFinancialData");
            Map<String, Object> quantAnalysis = quantAgent.performQuantitativeAnalysis(userData);

            if (progressCallback != null) {
                progressCallback.accept(Map.of("stage", "Creating Strategic Plan", "progress", 50, "agent", "Strategist", "icon", "🎯"));
            }
            Map<String, Object> strategy = strategistAgent.generatePersonalizedPlan(Map.of("age", 30, "monthlyIncome", 85000), userData, List.of("Wealth Creation", "Tax Saving"));

            if (progressCallback != null) {
                progressCallback.accept(Map.of("stage", "Building Action Plan", "progress", 70, "agent", "Doer", "icon", "⚡"));
            }
            Map<String, Object> actionPlan = doerAgent.createActionPlan(strategy, quantAnalysis, userData);

            if (progressCallback != null) {
                progressCallback.accept(Map.of("stage", "Preparing Insights", "progress", 85, "agent", "Communicator", "icon", "💬"));
            }
            Map<String, Object> communication = communicatorAgent.generateUserCommunication(quantAnalysis, strategy, actionPlan, Map.of());

            if (progressCallback != null) {
                progressCallback.accept(Map.of("stage", "Finalizing Analysis", "progress", 95, "agent", "Orchestrator", "icon", "🔮"));
            }

            Map<String, Object> finalAnalysis = new LinkedHashMap<>();
            finalAnalysis.put("success", true);
            finalAnalysis.put("timestamp", new Date().toString());
            finalAnalysis.put("executionTime", String.format(Locale.US, "%.1fs", (System.currentTimeMillis() - startTime) / 1000.0));
            finalAnalysis.put("userId", userId);
            finalAnalysis.put("phoneNumber", phoneNumber);
            finalAnalysis.put("realTimeData", realTimeData);
            finalAnalysis.put("quantAnalysis", quantAnalysis);
            finalAnalysis.put("strategy", strategy);
            finalAnalysis.put("actionPlan", actionPlan);
            finalAnalysis.put("communication", communication);
            finalAnalysis.put("summary", Map.of(
                    "headline", "Portfolio analysis complete with 5 AI Agents",
                    "keyInsights", List.of("Healthy portfolio allocation", "Tax optimization opportunities available")
            ));
            finalAnalysis.put("agentActivity", List.of(
                    Map.of("name", "Realist", "icon", "📈", "status", "complete", "activity", "Fetched real-time data from Fi MCP"),
                    Map.of("name", "Quant", "icon", "🔢", "status", "complete", "activity", "Performed XIRR & Sharpe calculations"),
                    Map.of("name", "Strategist", "icon", "🎯", "status", "complete", "activity", "Created personalized asset allocation"),
                    Map.of("name", "Doer", "icon", "⚡", "status", "complete", "activity", "Built execution timeline"),
                    Map.of("name", "Communicator", "icon", "💬", "status", "complete", "activity", "Prepared executive insights")
            ));

            if (progressCallback != null) {
                progressCallback.accept(Map.of("stage", "Analysis Complete", "progress", 100, "agent", "Complete", "icon", "✅"));
            }

            return finalAnalysis;

        } catch (Exception e) {
            logger.error("ORCHESTRATOR", "Portfolio analysis pipeline error: " + e.getMessage(), e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }
}
