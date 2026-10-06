package com.aura.controller;

import com.aura.service.LoggerService;
import com.aura.service.RAGService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/rag")
public class RAGController {

    @Autowired
    private RAGService ragService;

    @Autowired
    private LoggerService logger;

    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestBody Map<String, Object> body) {
        String query = (String) body.getOrDefault("query", "");
        int topK = body.containsKey("topK") && body.get("topK") instanceof Number n ? n.intValue() : 3;

        logger.rag("Search query: " + query);
        List<RAGService.Document> results = ragService.retrieve(query, topK);
        return ResponseEntity.ok(Map.of("success", true, "results", results));
    }

    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCategories() {
        List<String> categories = ragService.getCategories();
        List<Map<String, String>> docs = ragService.getKnowledgeBase().stream()
                .map(d -> Map.of("id", d.getId(), "title", d.getTitle(), "category", d.getCategory()))
                .toList();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "categories", categories,
                "documents", docs
        ));
    }
}
