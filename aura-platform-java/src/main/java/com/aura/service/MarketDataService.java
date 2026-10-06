package com.aura.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MarketDataService {

    @Autowired
    private LoggerService logger;

    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final Map<String, Long> lastUpdated = new ConcurrentHashMap<>();

    private static final long CACHE_TTL_INDICES = 60 * 1000L;
    private static final long CACHE_TTL_CURRENCY = 5 * 60 * 1000L;
    private static final long CACHE_TTL_NEWS = 15 * 60 * 1000L;

    private final Random random = new Random();

    private boolean isCacheValid(String key, long ttl) {
        Long updateTime = lastUpdated.get(key);
        if (updateTime == null) return false;
        return (System.currentTimeMillis() - updateTime) < ttl;
    }

    public List<Map<String, Object>> fetchIndices() {
        String cacheKey = "indices";
        if (isCacheValid(cacheKey, CACHE_TTL_INDICES) && cache.containsKey(cacheKey)) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cached = (List<Map<String, Object>>) cache.get(cacheKey);
            return cached;
        }

        Map<String, Double> baseIndices = Map.of(
                "NIFTY 50", 24837.0,
                "SENSEX", 81463.0,
                "NIFTY BANK", 53200.0,
                "NIFTY IT", 42150.0,
                "NIFTY MIDCAP", 58900.0
        );

        List<Map<String, Object>> indices = new ArrayList<>();
        for (Map.Entry<String, Double> entry : baseIndices.entrySet()) {
            double variation = (random.nextDouble() - 0.48) * 0.015; // slight positive bias
            double change = entry.getValue() * variation;
            double newValue = Math.round((entry.getValue() + change) * 100.0) / 100.0;
            double changeRound = Math.round(change * 100.0) / 100.0;
            double changePercent = Math.round(variation * 10000.0) / 100.0;

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", entry.getKey());
            item.put("symbol", entry.getKey().replace(" ", "_"));
            item.put("value", newValue);
            item.put("change", changeRound);
            item.put("changePercent", String.format(Locale.US, "%.2f", changePercent));
            item.put("positive", change >= 0);
            item.put("lastUpdated", LocalDateTime.now(ZoneId.of("Asia/Kolkata")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            indices.add(item);
        }

        cache.put(cacheKey, indices);
        lastUpdated.put(cacheKey, System.currentTimeMillis());
        return indices;
    }

    public Map<String, Object> fetchCurrencyRates() {
        String cacheKey = "currency";
        if (isCacheValid(cacheKey, CACHE_TTL_CURRENCY) && cache.containsKey(cacheKey)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cached = (Map<String, Object>) cache.get(cacheKey);
            return cached;
        }

        Map<String, Object> rates = new LinkedHashMap<>();
        rates.put("USDINR", 83.54 + (random.nextDouble() - 0.5) * 0.2);
        rates.put("EURINR", 90.12 + (random.nextDouble() - 0.5) * 0.25);
        rates.put("GBPINR", 106.35 + (random.nextDouble() - 0.5) * 0.3);
        rates.put("lastUpdated", LocalDateTime.now(ZoneId.of("Asia/Kolkata")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        cache.put(cacheKey, rates);
        lastUpdated.put(cacheKey, System.currentTimeMillis());
        return rates;
    }

    public List<Map<String, Object>> fetchNews() {
        String cacheKey = "news";
        if (isCacheValid(cacheKey, CACHE_TTL_NEWS) && cache.containsKey(cacheKey)) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> cached = (List<Map<String, Object>>) cache.get(cacheKey);
            return cached;
        }

        List<Map<String, Object>> newsList = List.of(
                Map.of(
                        "title", "RBI Keeps Repo Rate Unchanged at 6.5%, Forecasts Sturdy 7.2% GDP Growth",
                        "source", "Economic Times",
                        "time", "1 hour ago",
                        "url", "https://economictimes.indiatimes.com",
                        "category", "Monetary Policy"
                ),
                Map.of(
                        "title", "Nifty 50 Rebounds Supported by Strong Banking & Auto Inflows",
                        "source", "LiveMint",
                        "time", "2 hours ago",
                        "url", "https://livemint.com",
                        "category", "Markets"
                ),
                Map.of(
                        "title", "Mutual Fund SIP Inflows Surge Past Record ₹23,000 Crore in India",
                        "source", "MoneyControl",
                        "time", "4 hours ago",
                        "url", "https://moneycontrol.com",
                        "category", "Mutual Funds"
                ),
                Map.of(
                        "title", "Government Extends Sovereign Gold Bond Scheme Interest Disbursals",
                        "source", "Financial Express",
                        "time", "5 hours ago",
                        "url", "https://financialexpress.com",
                        "category", "Investments"
                )
        );

        cache.put(cacheKey, newsList);
        lastUpdated.put(cacheKey, System.currentTimeMillis());
        return newsList;
    }

    public List<Map<String, Object>> fetchSectorPerformance() {
        return List.of(
                Map.of("sector", "Nifty Bank", "change", "+1.24%", "positive", true),
                Map.of("sector", "Nifty IT", "change", "+0.85%", "positive", true),
                Map.of("sector", "Nifty Auto", "change", "+0.42%", "positive", true),
                Map.of("sector", "Nifty Pharma", "change", "-0.18%", "positive", false),
                Map.of("sector", "Nifty FMCG", "change", "-0.35%", "positive", false),
                Map.of("sector", "Nifty Metal", "change", "+1.56%", "positive", true)
        );
    }

    public Map<String, Object> getMarketStatus() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        DayOfWeek day = now.getDayOfWeek();
        LocalTime time = now.toLocalTime();

        boolean isWeekday = (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY);
        boolean isOpen = isWeekday && (time.isAfter(LocalTime.of(9, 15)) && time.isBefore(LocalTime.of(15, 30)));

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("isOpen", isOpen);
        status.put("status", isOpen ? "Live" : "Closed");
        status.put("exchange", "NSE/BSE (IST)");
        status.put("nextOpen", isOpen ? "Tomorrow 09:15 IST" : "Next session 09:15 IST");
        return status;
    }

    public Map<String, Object> getMarketOverview() {
        return getMarketOverview(false);
    }

    public Map<String, Object> getMarketOverview(boolean forceRefresh) {
        if (forceRefresh) {
            cache.clear();
            lastUpdated.clear();
        }

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("indices", fetchIndices());
        overview.put("currencies", fetchCurrencyRates());
        overview.put("news", fetchNews());
        overview.put("sectors", fetchSectorPerformance());
        overview.put("marketStatus", getMarketStatus());
        overview.put("timestamp", LocalDateTime.now(ZoneId.of("Asia/Kolkata")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return overview;
    }

    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("marketStatus", getMarketStatus().get("status"));
        status.put("cachedIndices", cache.containsKey("indices"));
        status.put("timestamp", LocalDateTime.now().toString());
        return status;
    }
}
