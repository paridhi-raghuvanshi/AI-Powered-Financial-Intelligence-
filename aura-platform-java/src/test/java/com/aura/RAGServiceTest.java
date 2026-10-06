package com.aura;

import com.aura.service.LoggerService;
import com.aura.service.RAGService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RAGServiceTest {

    private RAGService ragService;

    @BeforeEach
    public void setup() {
        ragService = new RAGService();
        ReflectionTestUtils.setField(ragService, "logger", new LoggerService());
        ragService.initialize();
    }

    @Test
    public void testRetrieve80C() {
        List<RAGService.Document> docs = ragService.retrieve("How can I save tax using Section 80C and ELSS?", 3);
        assertFalse(docs.isEmpty());
        assertTrue(docs.stream().anyMatch(d -> d.getId().equals("tax-80c")));
    }

    @Test
    public void testRetrieveMutualFunds() {
        List<RAGService.Document> docs = ragService.retrieve("What is difference between large cap and small cap mutual fund?", 3);
        assertFalse(docs.isEmpty());
        assertTrue(docs.stream().anyMatch(d -> d.getId().equals("mf-types")));
    }

    @Test
    public void testRetrieveSIP() {
        List<RAGService.Document> docs = ragService.retrieve("Tell me about power of compounding in SIP", 3);
        assertFalse(docs.isEmpty());
        assertTrue(docs.stream().anyMatch(d -> d.getId().equals("sip-benefits")));
    }
}
