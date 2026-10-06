package com.aura.agent;

import com.aura.service.LoggerService;
import com.aura.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DoerAgent {

    private final String name = "Doer";
    private final String icon = "⚡";

    @Autowired
    private OpenAIService openaiService;

    @Autowired
    private LoggerService logger;

    public String getName() { return name; }
    public String getIcon() { return icon; }

    public String generateResponse(String prompt) {
        logger.agent("DOER", "Building execution plan...");

        String systemPrompt = """
            You are an Action-Oriented Financial Doer AI for Indian investors.
            Your role:
            1. Convert financial strategies into specific, actionable steps
            2. Provide clear implementation guides with platforms/tools (Zerodha, Groww, Kuvera, MF Central)
            3. Set realistic timelines for each action
            4. Include verification steps to track progress
            5. Prioritize actions by impact and urgency

            Always:
            - Start directly with step 1 (no greetings)
            - Use bullet points and checklists
            - Recommend zero-commission Direct Mutual Fund platforms
            - End with ONE follow-up question
            """;

        if (openaiService.isAvailable()) {
            String llmResponse = openaiService.chat(List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", prompt)
            ), 0.4, 1200);

            if (llmResponse != null && !llmResponse.isBlank()) {
                return llmResponse;
            }
        }

        return getFallbackResponse(prompt);
    }

    public Map<String, Object> createActionPlan(Map<String, Object> strategy, Map<String, Object> quantAnalysis, Map<String, Object> financialData) {
        logger.agent("DOER", "Assembling structured action roadmap...");

        List<Map<String, Object>> steps = List.of(
                Map.of(
                        "step", 1,
                        "title", "Automate Emergency Reserve",
                        "platform", "Bank / Liquid Mutual Fund (ICICI Prudential / Nippon Liquid)",
                        "timeline", "Immediate (Within 48 hours)",
                        "action", "Park 3-6 months essential expenses in high-liquidity instrument with instant redemption."
                ),
                Map.of(
                        "step", 2,
                        "title", "Setup Automated Direct SIPs",
                        "platform", "Zerodha Coin, Groww, or Kuvera (Direct Plans only)",
                        "timeline", "Day 3 - 5",
                        "action", "Schedule auto-debit for Nifty 50 Index Fund and Flexi Cap Fund 2 days after salary credit."
                ),
                Map.of(
                        "step", 3,
                        "title", "Secure Pure Term & Health Cover",
                        "platform", "Ditto Insurance / PolicyBazaar",
                        "timeline", "Within 14 days",
                        "action", "Purchase 15x annual salary pure term cover and ₹15L base health insurance with super top-up."
                ),
                Map.of(
                        "step", 4,
                        "title", "Activate NPS Tier-1 for Extra 80CCD Deduction",
                        "platform", "eNPS Portal (NSDL / KFintech)",
                        "timeline", "Before financial year end",
                        "action", "Contribute ₹50,000 to maximize tax savings under 80CCD(1B)."
                )
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("steps", steps);
        result.put("totalTimeline", "30 Days Execution Window");
        result.put("status", "ready_for_execution");
        return result;
    }

    private String getFallbackResponse(String prompt) {
        return """
            **Immediate Action Plan & Implementation Steps:**

            **Phase 1: Setup & Automation (Days 1–7)**
            1. **Select a Platform**: Open a Direct Mutual Fund account on **Groww, Zerodha Coin, or Kuvera** (zero distributor commission, saving ~1-1.5% annually).
            2. **Automate SIPs**: Schedule mandate dates 2-3 days after salary day to avoid accidental overdrafts.
            3. **KYC Verification**: Keep PAN, Aadhaar, and cancelled cheque ready for paperless 10-minute CKYC.

            **Phase 2: Risk Shielding (Days 8–15)**
            • Lock in Pure Term Insurance (e.g., HDFC Life / ICICI Pru iProtect) for 15x income.
            • Set up an Emergency Corpus in an Arbitrage Fund or Liquid Fund.

            **Phase 3: Tax Filing & Optimization (Days 16–30)**
            • Download Consolidated Account Statement (CAS) via **MF Central** or **CAMS/KFintech**.
            • Ensure 80C and 80D investment proofs are submitted to HR for TDS minimization.

            *Which step would you like specific platform instructions for?*
            """;
    }
}
