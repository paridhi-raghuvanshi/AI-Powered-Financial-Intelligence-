package com.aura.controller;

import com.aura.service.LoggerService;
import com.aura.service.MarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/market")
public class MarketDataController {

    @Autowired
    private MarketDataService marketDataService;

    @Autowired
    private LoggerService logger;

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getMarketOverview(@RequestParam(value = "refresh", required = false) String refresh) {
        boolean forceRefresh = refresh != null && !refresh.isBlank();
        logger.market("API request: Market overview (forceRefresh=" + forceRefresh + ")");
        Map<String, Object> data = marketDataService.getMarketOverview(forceRefresh);
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @GetMapping("/indices")
    public ResponseEntity<Map<String, Object>> getIndices() {
        return ResponseEntity.ok(Map.of("success", true, "data", marketDataService.fetchIndices()));
    }

    @GetMapping("/currency")
    public ResponseEntity<Map<String, Object>> getCurrency() {
        return ResponseEntity.ok(Map.of("success", true, "data", marketDataService.fetchCurrencyRates()));
    }

    @GetMapping("/news")
    public ResponseEntity<Map<String, Object>> getNews() {
        return ResponseEntity.ok(Map.of("success", true, "data", marketDataService.fetchNews()));
    }

    @GetMapping("/sectors")
    public ResponseEntity<Map<String, Object>> getSectors() {
        return ResponseEntity.ok(Map.of("success", true, "data", marketDataService.fetchSectorPerformance()));
    }
}
