package com.aura.agent;

import com.aura.service.FiMCPClient;
import com.aura.service.LoggerService;
import com.aura.service.MarketDataService;
import com.aura.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RealistAgent {

    private final String name = "Realist";
    private final String icon = "📈";

    @Autowired
    private FiMCPClient fiMCPClient;

    @Autowired
    private MarketDataService marketDataService;

    @Autowired
    private OpenAIService openaiService;

    @Autowired
    private LoggerService logger;

    public String getName() { return name; }
    public String getIcon() { return icon; }

    public String generateResponse(String prompt) {
        logger.agent("REALIST", "Validating against live market data...");

        Map<String, Object> marketData = marketDataService.getMarketOverview();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> indices = (List<Map<String, Object>>) marketData.get("indices");
        @SuppressWarnings("unchecked")
        Map<String, Object> currencies = (Map<String, Object>) marketData.get("currencies");

        String niftyVal = indices != null && !indices.isEmpty() ? String.valueOf(indices.get(0).get("value")) : "24,800";
        String sensexVal = indices != null && indices.size() > 1 ? String.valueOf(indices.get(1).get("value")) : "81,400";
        String usdInr = currencies != null ? String.valueOf(currencies.get("USDINR")) : "83.5";

        String systemPrompt = String.format("""
            You are a Market Realist AI agent specializing in Indian financial markets.
            CURRENT MARKET DATA:
            - Nifty 50: %s
            - Sensex: %s
            - USD/INR: %s

            Your role:
            1. Validate financial decisions against present economic realities and interest rates
            2. Warn about inflation risks, realistic equity market corrections, and tax drags
            3. Highlight valuation levels (P/E multiples) and market cycles

            Format:
            - Start directly with market reality checks (no greetings)
            - Keep numbers realistic (12-14%% equity CAGR long-term, not 25%%)
            - End with ONE follow-up question
            """, niftyVal, sensexVal, usdInr);

        if (openaiService.isAvailable()) {
            String llmResponse = openaiService.chat(List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", prompt)
            ), 0.3, 1200);

            if (llmResponse != null && !llmResponse.isBlank()) {
                return llmResponse;
            }
        }

        return getFallbackResponse(prompt, niftyVal, sensexVal);
    }

    public Map<String, Object> fetchRealTimeData(String phoneNumber) {
        logger.agent("REALIST", "Fetching real-time account data from Fi MCP for " + phoneNumber);

        String phone = phoneNumber != null ? phoneNumber : "2222222222";
        Map<String, Object> args = Map.of("phone_number", phone);

        Object netWorth = fiMCPClient.callTool("fetch_net_worth", args);
        Object bankTxn = fiMCPClient.callTool("fetch_bank_transactions", args);
        Object mfTxn = fiMCPClient.callTool("fetch_mf_transactions", args);
        Object stockTxn = fiMCPClient.callTool("fetch_stock_transactions", args);
        Object creditReport = fiMCPClient.callTool("fetch_credit_report", args);
        Object epfDetails = fiMCPClient.callTool("fetch_epf_details", args);

        Map<String, Object> userFinancialData = new LinkedHashMap<>();
        userFinancialData.put("netWorth", netWorth);
        userFinancialData.put("bankTransactions", bankTxn);
        userFinancialData.put("mutualFunds", mfTxn);
        userFinancialData.put("stocks", stockTxn);
        userFinancialData.put("creditReport", creditReport);
        userFinancialData.put("epfDetails", epfDetails);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userFinancialData", userFinancialData);
        result.put("marketOverview", marketDataService.getMarketOverview());
        result.put("source", "Fi.Money MCP Engine");
        result.put("timestamp", new Date().toString());
        return result;
    }

    private String getFallbackResponse(String prompt, String nifty, String sensex) {
        return String.format("""
            **Market Reality & Valuation Context:**

            • **Benchmark Levels**: Nifty 50 is trading around **%s**, and Sensex is near **%s**.
            • **Realistic Return Expectation**: While short-term rallies can be aggressive, assume a sustainable long-term equity CAGR of **12%% to 13%%** (accounting for 5-6%% real GDP growth + ~5%% inflation).
            • **Inflation Reality**: Retail inflation (CPI) hovers between 4.5%% to 5.5%% in India. Bank FDs yielding 6.5-7.0%% post-tax barely preserve purchasing power.
            • **Risk Discipline**: Never deploy 100%% of lump sum capital at market highs; stagger deployments over 6 to 12 months using Systematic Transfer Plans (STP).

            *Are you planning to invest a lump sum or invest via monthly SIPs?*
            """, nifty, sensex);
    }
}
