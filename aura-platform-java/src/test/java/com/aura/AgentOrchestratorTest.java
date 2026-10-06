package com.aura;

import com.aura.agent.*;
import com.aura.orchestrator.AgentOrchestrator;
import com.aura.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AgentOrchestratorTest {

    private AgentOrchestrator orchestrator;

    @BeforeEach
    public void setup() {
        orchestrator = new AgentOrchestrator();
        LoggerService logger = new LoggerService();
        OpenAIService openAIService = new OpenAIService();
        RAGService ragService = new RAGService();
        ReflectionTestUtils.setField(ragService, "logger", logger);
        ragService.initialize();

        MarketDataService marketDataService = new MarketDataService();
        ReflectionTestUtils.setField(marketDataService, "logger", logger);

        ObservabilityService observabilityService = new ObservabilityService();
        ReflectionTestUtils.setField(observabilityService, "logger", logger);

        FiMCPClient fiMCPClient = new FiMCPClient();
        ReflectionTestUtils.setField(fiMCPClient, "logger", logger);
        ReflectionTestUtils.setField(fiMCPClient, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(fiMCPClient, "silentFallback", true);
        fiMCPClient.initialize();

        StrategistAgent strategist = new StrategistAgent();
        ReflectionTestUtils.setField(strategist, "logger", logger);
        ReflectionTestUtils.setField(strategist, "openaiService", openAIService);
        ReflectionTestUtils.setField(strategist, "ragService", ragService);

        QuantAgent quant = new QuantAgent();
        ReflectionTestUtils.setField(quant, "logger", logger);
        ReflectionTestUtils.setField(quant, "openaiService", openAIService);

        DoerAgent doer = new DoerAgent();
        ReflectionTestUtils.setField(doer, "logger", logger);
        ReflectionTestUtils.setField(doer, "openaiService", openAIService);

        RealistAgent realist = new RealistAgent();
        ReflectionTestUtils.setField(realist, "logger", logger);
        ReflectionTestUtils.setField(realist, "openaiService", openAIService);
        ReflectionTestUtils.setField(realist, "fiMCPClient", fiMCPClient);
        ReflectionTestUtils.setField(realist, "marketDataService", marketDataService);

        CommunicatorAgent communicator = new CommunicatorAgent();
        ReflectionTestUtils.setField(communicator, "logger", logger);
        ReflectionTestUtils.setField(communicator, "openaiService", openAIService);

        ReflectionTestUtils.setField(orchestrator, "logger", logger);
        ReflectionTestUtils.setField(orchestrator, "ragService", ragService);
        ReflectionTestUtils.setField(orchestrator, "marketDataService", marketDataService);
        ReflectionTestUtils.setField(orchestrator, "openaiService", openAIService);
        ReflectionTestUtils.setField(orchestrator, "observabilityService", observabilityService);
        ReflectionTestUtils.setField(orchestrator, "strategistAgent", strategist);
        ReflectionTestUtils.setField(orchestrator, "quantAgent", quant);
        ReflectionTestUtils.setField(orchestrator, "doerAgent", doer);
        ReflectionTestUtils.setField(orchestrator, "realistAgent", realist);
        ReflectionTestUtils.setField(orchestrator, "communicatorAgent", communicator);
    }

    @Test
    public void testFinanceValidation() {
        assertTrue(orchestrator.isFinanceRelated("How should I invest ₹10,000 monthly in mutual funds?"));
        assertTrue(orchestrator.isFinanceRelated("What is Section 80C tax deduction?"));
        assertTrue(orchestrator.isFinanceRelated("Calculate SIP returns for 10 years"));
        assertFalse(orchestrator.isFinanceRelated("What is the recipe for chicken biryani?"));
        assertFalse(orchestrator.isFinanceRelated("Who won the cricket match yesterday?"));
    }

    @Test
    public void testIntentClassification() {
        Map<String, Object> simpleIntent = orchestrator.analyzeIntent("What is an ELSS mutual fund?");
        assertEquals("general_advice", simpleIntent.get("primary_intent"));
        assertEquals("simple", simpleIntent.get("complexity"));

        Map<String, Object> complexIntent = orchestrator.analyzeIntent("I am 32 years old, earning 1.5 lakh monthly. How should I plan retirement corpus?");
        assertEquals("portfolio_planning", complexIntent.get("primary_intent"));
        assertEquals("complex", complexIntent.get("complexity"));
        @SuppressWarnings("unchecked")
        List<String> agents = (List<String>) complexIntent.get("required_agents");
        assertEquals(5, agents.size());
    }

    @Test
    public void testProcessChatOffTopic() {
        Map<String, Object> result = orchestrator.processChat("Tell me a joke about dogs", Map.of(), null);
        assertTrue((Boolean) result.get("success"));
        assertEquals("off_topic", result.get("intent"));
        assertTrue(result.get("response").toString().contains("AURA"));
    }

    @Test
    public void testProcessChatFinancialQuery() {
        Map<String, Object> result = orchestrator.processChat("How should I invest ₹20,000 per month for long-term wealth?", Map.of(), null);
        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("response"));
        assertFalse(result.get("response").toString().isBlank());
        @SuppressWarnings("unchecked")
        List<String> agentsUsed = (List<String>) result.get("agentsUsed");
        assertFalse(agentsUsed.isEmpty());
    }

    @Test
    public void testPortfolioAnalysis() {
        Map<String, Object> analysis = orchestrator.analyzePortfolio("test_user", "2222222222", null);
        assertTrue((Boolean) analysis.get("success"));
        assertNotNull(analysis.get("realTimeData"));
        assertNotNull(analysis.get("quantAnalysis"));
        assertNotNull(analysis.get("strategy"));
        assertNotNull(analysis.get("actionPlan"));
        assertNotNull(analysis.get("communication"));
    }
}
