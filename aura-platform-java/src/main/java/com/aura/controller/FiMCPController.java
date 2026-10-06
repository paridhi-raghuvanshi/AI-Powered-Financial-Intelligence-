package com.aura.controller;

import com.aura.service.FiMCPClient;
import com.aura.service.LoggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/fi-mcp")
public class FiMCPController {

    @Autowired
    private FiMCPClient fiMCPClient;

    @Autowired
    private LoggerService logger;

    @PostMapping("/fetch-net-worth")
    public ResponseEntity<Object> fetchNetWorth(@RequestBody Map<String, Object> body) {
        String phone = (String) body.getOrDefault("phone_number", "2222222222");
        logger.mcp("Fetching net worth for " + phone);
        return ResponseEntity.ok(fiMCPClient.callTool("fetch_net_worth", Map.of("phone_number", phone)));
    }

    @PostMapping("/fetch-bank-transactions")
    public ResponseEntity<Object> fetchBankTransactions(@RequestBody Map<String, Object> body) {
        String phone = (String) body.getOrDefault("phone_number", "2222222222");
        logger.mcp("Fetching bank transactions for " + phone);
        return ResponseEntity.ok(fiMCPClient.callTool("fetch_bank_transactions", Map.of("phone_number", phone)));
    }

    @PostMapping("/fetch-mf-transactions")
    public ResponseEntity<Object> fetchMFTransactions(@RequestBody Map<String, Object> body) {
        String phone = (String) body.getOrDefault("phone_number", "2222222222");
        logger.mcp("Fetching MF transactions for " + phone);
        return ResponseEntity.ok(fiMCPClient.callTool("fetch_mf_transactions", Map.of("phone_number", phone)));
    }

    @PostMapping("/fetch-stock-transactions")
    public ResponseEntity<Object> fetchStockTransactions(@RequestBody Map<String, Object> body) {
        String phone = (String) body.getOrDefault("phone_number", "2222222222");
        logger.mcp("Fetching stock transactions for " + phone);
        return ResponseEntity.ok(fiMCPClient.callTool("fetch_stock_transactions", Map.of("phone_number", phone)));
    }

    @PostMapping("/fetch-credit-report")
    public ResponseEntity<Object> fetchCreditReport(@RequestBody Map<String, Object> body) {
        String phone = (String) body.getOrDefault("phone_number", "2222222222");
        logger.mcp("Fetching credit report for " + phone);
        return ResponseEntity.ok(fiMCPClient.callTool("fetch_credit_report", Map.of("phone_number", phone)));
    }

    @PostMapping("/fetch-epf-details")
    public ResponseEntity<Object> fetchEPFDetails(@RequestBody Map<String, Object> body) {
        String phone = (String) body.getOrDefault("phone_number", "2222222222");
        logger.mcp("Fetching EPF details for " + phone);
        return ResponseEntity.ok(fiMCPClient.callTool("fetch_epf_details", Map.of("phone_number", phone)));
    }

    @PostMapping("/fetch-all")
    public ResponseEntity<Map<String, Object>> fetchAll(@RequestBody Map<String, Object> body) {
        String phone = (String) body.getOrDefault("phone_number", "2222222222");
        logger.mcp("Fetching all financial data for " + phone);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("netWorth", fiMCPClient.callTool("fetch_net_worth", Map.of("phone_number", phone)));
        data.put("transactions", fiMCPClient.callTool("fetch_bank_transactions", Map.of("phone_number", phone)));
        data.put("mutualFunds", fiMCPClient.callTool("fetch_mf_transactions", Map.of("phone_number", phone)));
        data.put("stocks", fiMCPClient.callTool("fetch_stock_transactions", Map.of("phone_number", phone)));
        data.put("creditReport", fiMCPClient.callTool("fetch_credit_report", Map.of("phone_number", phone)));
        data.put("epf", fiMCPClient.callTool("fetch_epf_details", Map.of("phone_number", phone)));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("data", data);
        response.put("timestamp", new Date().toString());
        return ResponseEntity.ok(response);
    }
}
