package com.aura.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RAGService {

    @Autowired
    private LoggerService logger;

    public static class Document {
        private String id;
        private String category;
        private String title;
        private String content;
        private List<String> keywords;

        public Document(String id, String category, String title, String content, List<String> keywords) {
            this.id = id;
            this.category = category;
            this.title = title;
            this.content = content;
            this.keywords = keywords;
        }

        public String getId() { return id; }
        public String getCategory() { return category; }
        public String getTitle() { return title; }
        public String getContent() { return content; }
        public List<String> getKeywords() { return keywords; }
    }

    private final List<Document> knowledgeBase = new ArrayList<>();

    @PostConstruct
    public void initialize() {
        if (!knowledgeBase.isEmpty()) return;

        // 1. Tax 80C
        knowledgeBase.add(new Document(
                "tax-80c", "taxation", "Section 80C Deductions",
                """
                Section 80C of Income Tax Act allows deductions up to ₹1,50,000 per year. Eligible investments include:
                - ELSS (Equity Linked Savings Scheme) - 3 year lock-in, potential for highest returns (~12-15%)
                - PPF (Public Provident Fund) - 15 year lock-in, tax-free returns at ~7.1%
                - NSC (National Savings Certificate) - 5 year lock-in
                - Tax Saving FDs - 5 year lock-in with guaranteed returns
                - EPF/VPF contributions by employee
                - Life Insurance premiums
                - NPS (additional ₹50,000 deduction under Section 80CCD(1B) beyond 80C limit)
                - Home loan principal repayment
                - Children's school/college tuition fees (up to 2 children)
                """,
                List.of("80c", "tax saving", "elss", "ppf", "nsc", "tax deduction", "section 80c", "tax")
        ));

        // 2. LTCG
        knowledgeBase.add(new Document(
                "tax-ltcg", "taxation", "Long Term Capital Gains Tax (LTCG)",
                """
                Long Term Capital Gains (LTCG) on investments in India:
                - Equity & Equity Mutual Funds: Holding period > 12 months. Tax rate: 12.5% on gains exceeding ₹1,25,000/year (Budget 2024 revised).
                - Debt Funds: Taxed at individual slab rate (for purchases after April 2023).
                - Real Estate: Holding period > 24 months, 12.5% without indexation (or optional 20% with indexation for pre-2024 purchases).
                - Section 54/54F: Exemptions available on capital gains by reinvesting into residential property.
                """,
                List.of("ltcg", "long term capital gains", "equity tax", "capital gains", "holding period", "budget 2024")
        ));

        // 3. STCG
        knowledgeBase.add(new Document(
                "tax-stcg", "taxation", "Short Term Capital Gains Tax (STCG)",
                """
                Short Term Capital Gains (STCG) on equity investments in India:
                - Holding period: Less than 12 months for equity shares and equity mutual funds.
                - Tax rate: 20% flat rate under Section 111A (revised from 15% in Budget 2024).
                - Debt funds (< 36 months): Added to total income and taxed at marginal income slab rate.
                - Tax loss harvesting: STCG can be set off against short-term capital losses.
                """,
                List.of("stcg", "short term capital gains", "short term tax", "20% tax", "capital loss")
        ));

        // 4. Section 80D
        knowledgeBase.add(new Document(
                "tax-80d", "taxation", "Section 80D Health Insurance Deduction",
                """
                Section 80D allows tax deductions for health insurance premiums paid:
                - Self, Spouse, and Dependent Children: Up to ₹25,000 per financial year.
                - Parents (below 60 years): Additional deduction up to ₹25,000.
                - Parents (Senior Citizens 60+): Additional deduction up to ₹50,000.
                - Maximum total deduction possible: Up to ₹1,00,000 if self/family and parents are senior citizens.
                - Preventive health check-up: Up to ₹5,000 within the overall limits.
                """,
                List.of("80d", "health insurance", "medical insurance", "senior citizen", "preventive checkup")
        ));

        // 5. Types of Mutual Funds
        knowledgeBase.add(new Document(
                "mf-types", "investments", "Types of Mutual Funds in India (SEBI Classification)",
                """
                SEBI Mutual Fund Categories:
                EQUITY FUNDS:
                - Large Cap: Top 100 companies by market cap; high stability, steady long-term compounding (~11-13%).
                - Mid Cap: Companies ranked 101-250; faster growth, higher volatility (~13-16%).
                - Small Cap: Companies ranked 251+; aggressive growth potential, high risk/drawdown.
                - Flexi Cap / Multi Cap: Fund manager dynamically invests across market caps.
                - ELSS: Tax saving fund under 80C with 3-year lock-in period.

                DEBT FUNDS:
                - Liquid Funds: Matures up to 91 days; ideal for emergency corpus parking.
                - Ultra Short & Short Duration: 3 months to 3 years; alternatives to bank FDs.
                - Corporate Bond Funds: High safety with AA+ rated papers.

                HYBRID FUNDS:
                - Aggressive Hybrid: 65-80% equity, remainder debt.
                - Balanced Advantage / Dynamic Asset Allocation: Auto-adjusts equity/debt based on market PE.
                - Arbitrage Funds: Low risk with equity taxation.
                """,
                List.of("mutual fund", "mf types", "large cap", "mid cap", "small cap", "debt fund", "hybrid", "elss", "flexi cap")
        ));

        // 6. SIP Investment Strategy
        knowledgeBase.add(new Document(
                "sip-benefits", "investments", "SIP Investment Strategy & Compounding",
                """
                Systematic Investment Plan (SIP) Principles:
                - Rupee Cost Averaging: Automatically buys more units during market dips and fewer at market peaks.
                - Power of Compounding: ₹10,000/month invested at 12% annual CAGR produces ₹1 Crore in ~20 years.
                - Step-up SIP: Increasing your SIP amount by 10% each year with salary hikes dramatically cuts the time to achieve wealth goals by 5-7 years!
                - Discipline: Removes emotional timing of market tops and bottoms.
                - Ideal Allocation: 70% in Broad Market Equity (Nifty 50 Index / Flexi Cap), 20% in Mid/Small Cap, 10% Gold/Debt.
                """,
                List.of("sip", "systematic investment plan", "compounding", "step-up sip", "wealth creation", "rupee cost averaging")
        ));

        // 7. Emergency Fund & Insurance Foundation
        knowledgeBase.add(new Document(
                "foundation-planning", "planning", "Financial Safety Net: Emergency Fund & Insurance",
                """
                The Golden Rules of Financial Stability:
                1. Emergency Fund:
                   - Accumulate 6 to 9 months of mandatory household expenses + EMIs.
                   - Keep 50% in High-Yield Savings Account and 50% in Liquid/Arbitrage Mutual Funds.
                2. Pure Term Life Insurance:
                   - Sum assured should be 10x to 15x of your annual gross income.
                   - Buy plain vanilla term plan up to retirement age (60-65 years). Avoid ULIPs/Endowment plans.
                3. Comprehensive Health Insurance:
                   - Base cover of ₹10 Lakhs to ₹15 Lakhs + Super Top-up cover of ₹50 Lakhs to ₹1 Crore.
                   - Do not rely solely on employer corporate health coverage.
                """,
                List.of("emergency fund", "term insurance", "life insurance", "health insurance", "safety net", "ulip")
        ));

        // 8. Retirement Planning & NPS
        knowledgeBase.add(new Document(
                "retirement-nps", "planning", "Retirement Planning & National Pension System (NPS)",
                """
                Retirement Planning Strategy for India:
                - Corpus Target: 25x to 30x of your anticipated annual post-retirement expenses (4% withdrawal rule).
                - National Pension System (NPS):
                  - Tier 1 account offers extra ₹50,000 tax deduction under Sec 80CCD(1B).
                  - Auto or Active choice allows up to 75% equity allocation (Asset Class E).
                  - At age 60: 60% lump-sum withdrawal is 100% tax-free; remaining 40% purchases monthly annuity.
                - Public Provident Fund (PPF):
                  - Sovereign guarantee, EEE tax status (Exempt on investment, interest, and maturity).
                """,
                List.of("retirement", "nps", "pension", "national pension system", "ppf", "corpus", "80ccd")
        ));

        logger.rag("RAG Knowledge Base initialized with " + knowledgeBase.size() + " documents");
    }

    public List<Document> retrieve(String query, int topK) {
        if (query == null || query.isBlank()) return Collections.emptyList();
        String lowerQuery = query.toLowerCase();
        String[] queryWords = lowerQuery.split("\\W+");

        record ScoredDoc(Document doc, int score) {}

        List<ScoredDoc> scoredDocs = new ArrayList<>();
        for (Document doc : knowledgeBase) {
            int score = 0;

            // Keyword match
            for (String kw : doc.getKeywords()) {
                if (lowerQuery.contains(kw.toLowerCase())) {
                    score += 10;
                }
            }

            // Title match
            if (doc.getTitle().toLowerCase().contains(lowerQuery)) {
                score += 15;
            }
            for (String word : queryWords) {
                if (!word.isBlank()) {
                    if (doc.getTitle().toLowerCase().contains(word)) score += 3;
                    if (doc.getContent().toLowerCase().contains(word)) score += 1;
                }
            }

            if (score > 0) {
                scoredDocs.add(new ScoredDoc(doc, score));
            }
        }

        return scoredDocs.stream()
                .sorted((a, b) -> Integer.compare(b.score(), a.score()))
                .limit(topK)
                .map(ScoredDoc::doc)
                .collect(Collectors.toList());
    }

    public String getContextForQuery(String query) {
        return getContextForQuery(query, 2000);
    }

    public String getContextForQuery(String query, int maxTokens) {
        List<Document> docs = retrieve(query, 3);
        if (docs.isEmpty()) return "";

        StringBuilder sb = new StringBuilder("RELEVANT FINANCIAL KNOWLEDGE:\n\n");
        for (Document d : docs) {
            sb.append("### ").append(d.getTitle()).append("\n")
              .append(d.getContent().strip()).append("\n\n");
        }
        return sb.toString();
    }

    public List<String> getCategories() {
        return knowledgeBase.stream()
                .map(Document::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Document> getKnowledgeBase() {
        return Collections.unmodifiableList(knowledgeBase);
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("initialized", !knowledgeBase.isEmpty());
        status.put("documentCount", knowledgeBase.size());
        status.put("categories", getCategories());
        return status;
    }
}
