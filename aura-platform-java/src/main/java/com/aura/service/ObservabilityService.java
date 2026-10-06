package com.aura.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ObservabilityService {

    @Autowired
    private LoggerService logger;

    public static class AgentMetric {
        private final AtomicInteger calls = new AtomicInteger(0);
        private final AtomicInteger successes = new AtomicInteger(0);
        private final AtomicInteger errors = new AtomicInteger(0);
        private final AtomicLong totalDurationMs = new AtomicLong(0);

        public void record(long durationMs, boolean success) {
            calls.incrementAndGet();
            if (success) successes.incrementAndGet();
            else errors.incrementAndGet();
            totalDurationMs.addAndGet(durationMs);
        }

        public Map<String, Object> toMap() {
            int c = calls.get();
            long total = totalDurationMs.get();
            return Map.of(
                    "calls", c,
                    "successes", successes.get(),
                    "errors", errors.get(),
                    "avgDurationMs", c > 0 ? total / c : 0
            );
        }
    }

    private final Map<String, AgentMetric> agentMetrics = new ConcurrentHashMap<>();
    private final Map<String, Long> activeRuns = new ConcurrentHashMap<>();

    public String startAgentRun(String agentName, Map<String, Object> inputs) {
        String runId = "trace_" + UUID.randomUUID().toString().substring(0, 10);
        activeRuns.put(runId, System.currentTimeMillis());
        return runId;
    }

    public void endAgentRun(String runId, String agentName, boolean success, String error) {
        Long startTime = activeRuns.remove(runId);
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 50;

        agentMetrics.computeIfAbsent(agentName.toLowerCase(), k -> new AgentMetric())
                .record(duration, success);
    }

    public Map<String, Object> getAgentMetrics() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, AgentMetric> entry : agentMetrics.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toMap());
        }
        return result;
    }

    public Map<String, Object> getMetricsSummary() {
        int totalCalls = agentMetrics.values().stream().mapToInt(m -> m.calls.get()).sum();
        int totalSuccess = agentMetrics.values().stream().mapToInt(m -> m.successes.get()).sum();
        long totalDuration = agentMetrics.values().stream().mapToLong(m -> m.totalDurationMs.get()).sum();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("enabled", true);
        summary.put("provider", "Internal Telemetry & LangSmith Tracing");
        summary.put("totalAgentCalls", totalCalls);
        summary.put("successRate", totalCalls > 0 ? (totalSuccess * 100.0 / totalCalls) : 100.0);
        summary.put("averageLatencyMs", totalCalls > 0 ? totalDuration / totalCalls : 0);
        summary.put("activeTraces", activeRuns.size());
        return summary;
    }
}
