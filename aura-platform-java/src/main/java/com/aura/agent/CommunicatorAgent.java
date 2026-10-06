package com.aura.agent;

import com.aura.service.LoggerService;
import com.aura.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CommunicatorAgent {

    private final String name = "Communicator";
    private final String icon = "💬";

    @Autowired
    private OpenAIService openaiService;

    @Autowired
    private LoggerService logger;

    public String getName() { return name; }
    public String getIcon() { return icon; }

    public String generateResponse(String prompt) {
        logger.agent("COMMUNICATOR", "Synthesizing user-friendly message...");

        String systemPrompt = """
            You are an expert Financial Communicator AI for Indian users.
            CRITICAL: Never start with "Hey", "Hello", "Hi there". Start DIRECTLY with the answer.
            Your role:
            1. Explain complex financial jargon in simple, crystal-clear terms
            2. Use relatable Indian analogies (SIP, gold, FD, ₹)
            3. Make takeaways immediately actionable
            4. Keep formatting clean with bold key points and bullet lists
            5. End with ONE helpful question
            """;

        if (openaiService.isAvailable()) {
            String llmResponse = openaiService.chat(List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", prompt)
            ), 0.7, 1200);

            if (llmResponse != null && !llmResponse.isBlank()) {
                return llmResponse;
            }
        }

        return getFallbackResponse(prompt);
    }

    public Map<String, Object> generateUserCommunication(Map<String, Object> quant, Map<String, Object> strategy, Map<String, Object> actionPlan, Map<String, Object> profile) {
        logger.agent("COMMUNICATOR", "Generating tailored financial brief...");

        Map<String, Object> brief = new LinkedHashMap<>();
        brief.put("executiveSummary", "Your customized wealth plan is ready! We've balanced high-growth mutual funds with tax-saving instruments.");
        brief.put("keyTakeaway", "By maintaining disciplined monthly investments and stepping up by 10% each year, your portfolio is on track for financial freedom.");
        brief.put("nextImmediateStep", "Set up direct SIP auto-debit on your preferred platform for the 5th of each month.");
        return brief;
    }

    private String getFallbackResponse(String prompt) {
        return """
            **Executive Summary & Key Takeaways:**

            Personal finance in India boils down to three simple, non-negotiable rules:
            1. **Protect your downside**: Ensure your family has term insurance (15x income) and comprehensive health insurance before chasing returns.
            2. **Compounding beats timing**: Starting a ₹10,000 monthly SIP today matters far more than waiting for a 5% market dip.
            3. **Save taxes smartly**: Maximize 80C and 80D via ELSS and health insurance to keep more of your hard-earned money working for you.

            *Would you like to start with investment allocation, or would you like to review tax savings first?*
            """;
    }
}
