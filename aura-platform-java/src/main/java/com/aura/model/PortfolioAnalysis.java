package com.aura.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "analyses")
public class PortfolioAnalysis {
    @Id
    private String id;
    private String userId;
    private String phoneNumber;

    private Map<String, Object> realTimeData;
    private Map<String, Object> quantAnalysis;
    private Map<String, Object> strategy;
    private Map<String, Object> actionPlan;
    private Map<String, Object> communication;
    private Map<String, Object> summary;

    private Instant createdAt = Instant.now();
    private String executionTime;

    public PortfolioAnalysis() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Map<String, Object> getRealTimeData() { return realTimeData; }
    public void setRealTimeData(Map<String, Object> realTimeData) { this.realTimeData = realTimeData; }

    public Map<String, Object> getQuantAnalysis() { return quantAnalysis; }
    public void setQuantAnalysis(Map<String, Object> quantAnalysis) { this.quantAnalysis = quantAnalysis; }

    public Map<String, Object> getStrategy() { return strategy; }
    public void setStrategy(Map<String, Object> strategy) { this.strategy = strategy; }

    public Map<String, Object> getActionPlan() { return actionPlan; }
    public void setActionPlan(Map<String, Object> actionPlan) { this.actionPlan = actionPlan; }

    public Map<String, Object> getCommunication() { return communication; }
    public void setCommunication(Map<String, Object> communication) { this.communication = communication; }

    public Map<String, Object> getSummary() { return summary; }
    public void setSummary(Map<String, Object> summary) { this.summary = summary; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getExecutionTime() { return executionTime; }
    public void setExecutionTime(String executionTime) { this.executionTime = executionTime; }
}
