package com.aura;

import com.aura.service.OpenAIService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class FinancialCalculatorTest {

    private final OpenAIService service = new OpenAIService();

    @Test
    public void testCAGRCalculation() {
        // ₹1,00,000 growing to ₹2,00,000 in 5 years is ~14.87% CAGR
        Map<String, Object> result = service.calculateCAGR(100000, 200000, 5);
        assertNotNull(result);
        assertEquals(14.87, (double) result.get("cagr_percent"), 0.1);
        assertEquals(100.0, (double) result.get("absolute_return_percent"), 0.1);
    }

    @Test
    public void testSIPProjection() {
        // ₹10,000 monthly for 10 years at 12% is ~₹23.23 Lakhs
        Map<String, Object> result = service.calculateSIPProjection(10000, 12.0, 10, 0);
        assertNotNull(result);
        assertEquals(1200000L, result.get("total_invested"));
        long futureValue = (long) result.get("future_value");
        assertTrue(futureValue > 2300000L && futureValue < 2400000L);
    }

    @Test
    public void testLoanEMI() {
        // ₹50,00,000 at 8.5% for 240 months (20 years) is ~₹43,391/month
        Map<String, Object> result = service.calculateLoanEMI(5000000, 8.5, 240);
        assertNotNull(result);
        long emi = (long) result.get("emi");
        assertTrue(emi >= 43000 && emi <= 44000);
    }

    @Test
    public void testPortfolioRisk() {
        List<Double> returns = List.of(0.01, 0.02, -0.015, 0.03, 0.005, -0.008, 0.022);
        Map<String, Object> risk = service.calculatePortfolioRisk(returns, 0.06);
        assertNotNull(risk);
        assertTrue(risk.containsKey("volatility"));
        assertTrue(risk.containsKey("sharpe_ratio"));
    }
}
