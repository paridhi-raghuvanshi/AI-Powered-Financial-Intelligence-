package com.aura.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
public class OpenAIService {

    @Value("${azure.openai.endpoint:}")
    private String azureEndpoint;

    @Value("${azure.openai.key:}")
    private String azureKey;

    @Value("${azure.openai.deployment:gpt-4.1}")
    private String azureDeployment;

    @Value("${azure.openai.api-version:2025-01-01-preview}")
    private String azureApiVersion;

    @Value("${openai.api.key:}")
    private String openaiKey;

    @Value("${openai.model:gpt-4o}")
    private String openaiModel;

    @Autowired
    private LoggerService logger;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public boolean isAvailable() {
        boolean hasAzure = azureEndpoint != null && !azureEndpoint.isBlank() 
                && !azureEndpoint.contains("your-resource")
                && azureKey != null && !azureKey.isBlank() && !azureKey.contains("here");
        boolean hasOpenAI = openaiKey != null && !openaiKey.isBlank() && !openaiKey.contains("here");
        return hasAzure || hasOpenAI;
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("available", isAvailable());
        status.put("azureConfigured", azureEndpoint != null && !azureEndpoint.contains("your-resource"));
        status.put("deployment", azureDeployment);
        status.put("openaiModel", openaiModel);
        return status;
    }

    public String chat(List<Map<String, String>> messages, double temperature, int maxTokens) {
        if (!isAvailable()) {
            return null;
        }

        try {
            boolean useAzure = azureKey != null && !azureKey.isBlank() && !azureKey.contains("here");
            String url;
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder().timeout(Duration.ofSeconds(45));

            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("messages", messages);
            bodyMap.put("temperature", temperature);
            if (maxTokens > 0) bodyMap.put("max_tokens", maxTokens);

            if (useAzure) {
                String cleanEndpoint = azureEndpoint.endsWith("/") ? azureEndpoint.substring(0, azureEndpoint.length() - 1) : azureEndpoint;
                url = String.format("%s/openai/deployments/%s/chat/completions?api-version=%s",
                        cleanEndpoint, azureDeployment, azureApiVersion);
                reqBuilder.uri(URI.create(url))
                        .header("api-key", azureKey)
                        .header("Content-Type", "application/json");
            } else {
                url = "https://api.openai.com/v1/chat/completions";
                bodyMap.put("model", openaiModel);
                reqBuilder.uri(URI.create(url))
                        .header("Authorization", "Bearer " + openaiKey)
                        .header("Content-Type", "application/json");
            }

            String reqJson = objectMapper.writeValueAsString(bodyMap);
            HttpRequest request = reqBuilder.POST(HttpRequest.BodyPublishers.ofString(reqJson)).build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                Map<String, Object> respObj = objectMapper.readValue(response.body(), new TypeReference<>() {});
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> choices = (List<Map<String, Object>>) respObj.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null && message.get("content") != null) {
                        return (String) message.get("content");
                    }
                }
            } else {
                logger.warn("OPENAI", "LLM API returned status " + response.statusCode() + ": " + response.body());
            }
        } catch (Exception e) {
            logger.error("OPENAI", "Failed to call LLM API: " + e.getMessage(), e);
        }

        return null;
    }

    // ==================== FINANCIAL CALCULATOR TOOLS ====================

    public Map<String, Object> calculateCAGR(double initialValue, double finalValue, double years) {
        if (initialValue <= 0 || finalValue <= 0 || years <= 0) {
            return Map.of("error", "Invalid inputs for CAGR calculation");
        }
        double cagr = (Math.pow(finalValue / initialValue, 1.0 / years) - 1.0) * 100.0;
        double absoluteReturn = ((finalValue - initialValue) / initialValue) * 100.0;

        return Map.of(
                "cagr_percent", Math.round(cagr * 100.0) / 100.0,
                "absolute_return_percent", Math.round(absoluteReturn * 100.0) / 100.0,
                "initial_value", initialValue,
                "final_value", finalValue,
                "years", years
        );
    }

    public Map<String, Object> calculateSIPProjection(double monthlyAmount, double expectedReturn, int years, double stepUpPercentage) {
        double totalInvested = 0;
        double futureValue = 0;
        double monthlyRate = expectedReturn / 100.0 / 12.0;
        int totalMonths = years * 12;
        double currentSIP = monthlyAmount;

        for (int month = 0; month < totalMonths; month++) {
            if (stepUpPercentage > 0 && month > 0 && month % 12 == 0) {
                currentSIP = currentSIP * (1.0 + stepUpPercentage / 100.0);
            }
            totalInvested += currentSIP;
            futureValue = (futureValue + currentSIP) * (1.0 + monthlyRate);
        }

        long investedRound = Math.round(totalInvested);
        long futureRound = Math.round(futureValue);
        long wealthGained = futureRound - investedRound;
        double returnPct = investedRound > 0 ? (wealthGained * 100.0 / investedRound) : 0;

        return Map.of(
                "total_invested", investedRound,
                "future_value", futureRound,
                "wealth_gained", wealthGained,
                "returns_percentage", String.format(Locale.US, "%.2f", returnPct),
                "currency", "INR"
        );
    }

    public Map<String, Object> calculateLoanEMI(double principal, double interestRate, int tenureMonths) {
        double monthlyRate = interestRate / 100.0 / 12.0;
        double emi = principal * monthlyRate * Math.pow(1.0 + monthlyRate, tenureMonths)
                / (Math.pow(1.0 + monthlyRate, tenureMonths) - 1.0);
        double totalPayment = emi * tenureMonths;
        double totalInterest = totalPayment - principal;

        return Map.of(
                "emi", Math.round(emi),
                "total_payment", Math.round(totalPayment),
                "total_interest", Math.round(totalInterest),
                "principal", principal,
                "interest_rate", interestRate,
                "tenure_months", tenureMonths,
                "currency", "INR"
        );
    }

    public Map<String, Object> calculatePortfolioRisk(List<Double> returns, double riskFreeRate) {
        if (returns == null || returns.size() < 2) {
            return Map.of("error", "Insufficient data for risk calculation");
        }

        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = returns.stream().mapToDouble(r -> Math.pow(r - mean, 2)).average().orElse(0.0);
        double stdDev = Math.sqrt(variance);
        double annualizedVol = stdDev * Math.sqrt(252) * 100.0;
        double annualizedReturn = mean * 252 * 100.0;
        double sharpe = (annualizedReturn / 100.0 - riskFreeRate) / Math.max(annualizedVol / 100.0, 0.0001);

        return Map.of(
                "volatility", String.format(Locale.US, "%.2f", annualizedVol),
                "sharpe_ratio", String.format(Locale.US, "%.2f", sharpe),
                "annualized_return", String.format(Locale.US, "%.2f", annualizedReturn),
                "risk_level", annualizedVol > 25 ? "High" : annualizedVol > 15 ? "Medium" : "Low"
        );
    }

    public Map<String, Object> analyzeAssetAllocation(Map<String, Object> currentAllocation, String riskProfile, int age, int horizon) {
        Map<String, Integer> recommended = new HashMap<>();
        String profile = riskProfile != null ? riskProfile.toLowerCase() : "moderate";

        switch (profile) {
            case "conservative":
                recommended.put("equity", 30);
                recommended.put("debt", 50);
                recommended.put("gold", 10);
                recommended.put("liquid", 10);
                break;
            case "aggressive":
                recommended.put("equity", 70);
                recommended.put("debt", 20);
                recommended.put("gold", 5);
                recommended.put("liquid", 5);
                break;
            default:
                recommended.put("equity", 50);
                recommended.put("debt", 35);
                recommended.put("gold", 10);
                recommended.put("liquid", 5);
                break;
        }

        int ageAdjustedEquity = Math.min(Math.max(100 - age - 10, 20), 80);
        int finalEquity = Math.round((recommended.get("equity") + ageAdjustedEquity) / 2.0f);
        recommended.put("equity", finalEquity);
        recommended.put("debt", 100 - finalEquity - recommended.get("gold") - recommended.get("liquid"));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("current", currentAllocation != null ? currentAllocation : Map.of());
        result.put("recommended", recommended);
        result.put("risk_profile", profile);
        result.put("age", age);
        result.put("investment_horizon", horizon);
        result.put("rebalancing_needed", true);
        return result;
    }

    public Map<String, Object> analyzeSpendingPatterns(List<Map<String, Object>> transactions) {
        Map<String, Double> categoryTotals = new HashMap<>();
        double totalSpending = 0;

        if (transactions != null) {
            for (Map<String, Object> txn : transactions) {
                Object amtObj = txn.get("amount");
                double amt = amtObj instanceof Number n ? n.doubleValue() : 0.0;
                if (amt < 0) {
                    String cat = txn.getOrDefault("category", "Other").toString();
                    double absAmt = Math.abs(amt);
                    categoryTotals.put(cat, categoryTotals.getOrDefault(cat, 0.0) + absAmt);
                    totalSpending += absAmt;
                }
            }
        }

        List<Map<String, Object>> breakdown = new ArrayList<>();
        final double finalTotal = totalSpending;
        categoryTotals.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .forEach(e -> breakdown.add(Map.of(
                        "category", e.getKey(),
                        "amount", Math.round(e.getValue()),
                        "percentage", finalTotal > 0 ? String.format(Locale.US, "%.1f", (e.getValue() / finalTotal) * 100.0) : "0.0"
                )));

        return Map.of(
                "total_spending", Math.round(totalSpending),
                "breakdown", breakdown,
                "top_category", breakdown.isEmpty() ? "None" : breakdown.get(0).get("category"),
                "currency", "INR"
        );
    }
}
