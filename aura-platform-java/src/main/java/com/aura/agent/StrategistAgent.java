package com.aura.agent;

import com.aura.service.LoggerService;
import com.aura.service.OpenAIService;
import com.aura.service.RAGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class StrategistAgent {

    private final String name = "Strategist";
    private final String icon = "🎯";

    @Autowired
    private OpenAIService openaiService;

    @Autowired
    private RAGService ragService;

    @Autowired
    private LoggerService logger;

    public String getName() { return name; }
    public String getIcon() { return icon; }

    public String generateResponse(String prompt) {
        logger.agent("STRATEGIST", "Generating strategic advice...");

        String ragContext = ragService.getContextForQuery(prompt);
        String systemPrompt = String.format("""
            You are a world-class Financial Strategist AI for Indian investors.
            CRITICAL: Never start with greetings like "Hey", "Hello", "Hi". Start directly with the answer.
            Your expertise: Financial planning, Indian tax (80C, 80D), Mutual funds, Portfolio allocation, NPS/PPF/EPF, Insurance.

            KNOWLEDGE BASE:
            %s

            FORMAT RULES:
            - Start DIRECTLY with answer or brief context (no greetings)
            - Use **bold** for section headers
            - Use bullet points for lists
            - Keep response scannable and actionable
            - Use ₹ for currency
            - Be specific with numbers and recommendations
            - End with ONE follow-up question
            """, ragContext);

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

    public Map<String, Object> generatePersonalizedPlan(Map<String, Object> profile, Map<String, Object> financialData, List<String> goals) {
        logger.agent("STRATEGIST", "Creating personalized strategy plan...");

        int age = profile != null && profile.get("age") instanceof Number n ? n.intValue() : 30;
        double income = profile != null && profile.get("monthlyIncome") instanceof Number n ? n.doubleValue() : 75000;
        double netWorth = financialData != null && financialData.get("netWorth") instanceof Map m && m.get("total_net_worth") instanceof Number n ? n.doubleValue() : 1500000;

        int equityPct = Math.max(20, Math.min(80, 100 - age));
        int debtPct = Math.max(15, 90 - equityPct);
        int goldPct = 10;
        int cashPct = 100 - equityPct - debtPct - goldPct;

        Map<String, Object> plan = new LinkedHashMap<>();
        plan.put("summary", String.format("Structured 4-pillar financial strategy targeting long-term compounding with ₹%s emergency backstop.", Math.round(income * 6)));
        plan.put("assetAllocation", Map.of(
                "equity", equityPct,
                "debt", debtPct,
                "gold", goldPct,
                "cash", cashPct
        ));
        plan.put("recommendations", List.of(
                "Parag Parikh Flexi Cap Fund - Direct Growth",
                "Nippon India Nifty 50 Index Fund",
                "Motilal Oswal Midcap Fund",
                "ICICI Prudential All Seasons Bond Fund"
        ));
        plan.put("taxStrategies", List.of(
                "Invest ₹1.5L in ELSS/PPF under Section 80C",
                "Claim up to ₹50,000 additional deduction under Section 80CCD(1B) via NPS Tier 1",
                "Claim up to ₹25,000 for family health insurance under Section 80D"
        ));
        plan.put("confidence", 0.88);
        return plan;
    }

    private String getFallbackResponse(String prompt) {
        String lower = prompt.toLowerCase();
        if (lower.contains("tax") || lower.contains("80c") || lower.contains("save tax")) {
            return """
                **Optimal Tax Saving Strategy for FY 2026-27:**

                **1. Section 80C (Limit: ₹1,50,000)**
                • **ELSS Mutual Funds**: 3-year lock-in (shortest among 80C), ~12-14% historical CAGR.
                • **PPF / EPF**: Sovereign guarantee, tax-free interest under EEE regime.
                • **Children Tuition Fees**: Direct deduction within the 80C umbrella.

                **2. Extra NPS Deduction (Section 80CCD(1B))**
                • Invest additional ₹50,000 exclusively in NPS Tier-1 to reduce taxable income further.

                **3. Health Insurance (Section 80D)**
                • Up to ₹25,000 for self & family + up to ₹50,000 for senior citizen parents.

                *Would you like a tailored allocation between ELSS and guaranteed instruments?*
                """;
        }

        return """
            **Strategic Financial Plan & Portfolio Architecture:**

            **Recommended Asset Allocation:**
            • **Core Equity (60%)**: Nifty 50 Index Fund & Broad-market Flexi Cap Fund
            • **Growth Equity (15%)**: Midcap / Smallcap active funds for higher alpha
            • **Debt & Liquid (15%)**: Arbitrage funds / Short duration funds for liquidity
            • **Hedge (10%)**: Sovereign Gold Bonds (SGB) or Gold ETFs

            **Key Strategic Action Items:**
            1. Maintain an Emergency Fund of 6 months essential expenses in liquid mutual funds.
            2. Automate monthly SIPs right after salary credit day.
            3. Increase SIP investments by 10% annually (Step-up SIP).

            *What is your primary investment goal or target timeframe?*
            """;
    }
}
