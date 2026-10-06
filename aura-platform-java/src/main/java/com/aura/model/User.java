package com.aura.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Indexed(unique = true, sparse = true)
    private String email;

    @Indexed(unique = true, sparse = true)
    private String phoneNumber;

    private String provider = "email";

    @Indexed(unique = true, sparse = true)
    private String firebaseUid;

    private String displayName;
    private String photoURL;
    private String passwordHash;

    private Boolean onboardingComplete = false;
    private Integer age;
    private Double monthlyIncome;
    private Double monthlyExpenses;
    private Double currentSavings;
    private Double currentInvestments;
    private Double existingLoans;
    private String riskTolerance = "moderate";
    private List<String> investmentGoals;
    private String investmentHorizon;

    private Map<String, Object> financialDataCache;
    private Map<String, Object> preferences;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    private Instant lastLoginAt;
    private Instant onboardingCompletedAt;

    public User() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getPhotoURL() { return photoURL; }
    public void setPhotoURL(String photoURL) { this.photoURL = photoURL; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Boolean getOnboardingComplete() { return onboardingComplete; }
    public void setOnboardingComplete(Boolean onboardingComplete) { this.onboardingComplete = onboardingComplete; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Double getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(Double monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public Double getMonthlyExpenses() { return monthlyExpenses; }
    public void setMonthlyExpenses(Double monthlyExpenses) { this.monthlyExpenses = monthlyExpenses; }

    public Double getCurrentSavings() { return currentSavings; }
    public void setCurrentSavings(Double currentSavings) { this.currentSavings = currentSavings; }

    public Double getCurrentInvestments() { return currentInvestments; }
    public void setCurrentInvestments(Double currentInvestments) { this.currentInvestments = currentInvestments; }

    public Double getExistingLoans() { return existingLoans; }
    public void setExistingLoans(Double existingLoans) { this.existingLoans = existingLoans; }

    public String getRiskTolerance() { return riskTolerance; }
    public void setRiskTolerance(String riskTolerance) { this.riskTolerance = riskTolerance; }

    public List<String> getInvestmentGoals() { return investmentGoals; }
    public void setInvestmentGoals(List<String> investmentGoals) { this.investmentGoals = investmentGoals; }

    public String getInvestmentHorizon() { return investmentHorizon; }
    public void setInvestmentHorizon(String investmentHorizon) { this.investmentHorizon = investmentHorizon; }

    public Map<String, Object> getFinancialDataCache() { return financialDataCache; }
    public void setFinancialDataCache(Map<String, Object> financialDataCache) { this.financialDataCache = financialDataCache; }

    public Map<String, Object> getPreferences() { return preferences; }
    public void setPreferences(Map<String, Object> preferences) { this.preferences = preferences; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public Instant getOnboardingCompletedAt() { return onboardingCompletedAt; }
    public void setOnboardingCompletedAt(Instant onboardingCompletedAt) { this.onboardingCompletedAt = onboardingCompletedAt; }
}
