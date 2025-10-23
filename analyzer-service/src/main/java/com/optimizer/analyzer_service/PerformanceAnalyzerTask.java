package com.optimizer.analyzer_service;

// Add these new imports at the top
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue; // Thread-safe queue for insights

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono; // Make sure Mono is imported

@Component
public class PerformanceAnalyzerTask {

    private static final Logger log = LoggerFactory.getLogger(PerformanceAnalyzerTask.class);
    private final WebClient webClient;
    private final AIService aiService;

    // ADD THIS: A thread-safe queue to store insights as they are generated
    private final ConcurrentLinkedQueue<String> insightsLog = new ConcurrentLinkedQueue<>();

    // Constructor remains the same
    public PerformanceAnalyzerTask(WebClient webClient, AIService aiService) {
        this.webClient = webClient;
        this.aiService = aiService;
    }

   // NEW - Find endpoints with p95 latency > 100ms (much lower threshold)
    private final String P95_LATENCY_QUERY =
        "histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{application=\"performance-demo\"}[1m])) by (le, uri, method)) > 0.1"; // Reduced window to 1m, threshold to 0.1s

    // NEW - Find endpoints with ANY errors in the last 1 minute
    private final String ERROR_RATE_QUERY =
        "sum(rate(http_server_requests_seconds_count{application=\"performance-demo\",outcome=\"SERVER_ERROR\"}[1m])) by (uri, method) > 0"; // Reduced window to 1m, check if > 0 errors

    @Scheduled(fixedRate = 30000) // Keep checking performance frequently
    public void analyzePerformance() {
        log.info("--- Starting Performance Analysis ---");

        // Query for slow endpoints
        queryPrometheus(P95_LATENCY_QUERY)
            .subscribe(jsonResponse -> {
                log.warn("SLOW ENDPOINT DATA: {}", jsonResponse); // Keep logging raw data
                String insight = aiService.getInsightForData(jsonResponse);
                // Check for valid insight before logging and storing
                if (insight != null && !insight.startsWith("Error")) {
                    log.info("\n\n==> 🤖 AI OPTIMIZATION (Latency): {} \n<==\n", insight);
                    // MODIFICATION: Add the insight to our temporary log
                    insightsLog.offer("Latency Issue: " + insight);
                }
            });

        // Query for erroring endpoints
        queryPrometheus(ERROR_RATE_QUERY)
            .subscribe(jsonResponse -> {
                log.error("ERRORING ENDPOINT DATA: {}", jsonResponse); // Keep logging raw data
                String insight = aiService.getInsightForData(jsonResponse);
                 // Check for valid insight before logging and storing
                 if (insight != null && !insight.startsWith("Error")) {
                    log.info("\n\n==> 🤖 AI OPTIMIZATION (Errors): {} \n<==\n", insight);
                    // MODIFICATION: Add the insight to our temporary log
                    insightsLog.offer("Error Issue: " + insight);
                }
            });
    }

    // ADD THIS METHOD: Allows the reporting task to get collected insights
    // ADD THIS METHOD: Allows the reporting task to get collected insights
    public List<String> getAndClearInsights() {
        List<String> currentInsights = new ArrayList<>();
        String insight;
        // CORRECTED: Poll the queue until it's empty
        while ((insight = insightsLog.poll()) != null) {
            currentInsights.add(insight);
        }
        // Return an unmodifiable list so others can't change it
        return Collections.unmodifiableList(currentInsights);
    }

    // queryPrometheus method remains exactly the same
    private Mono<String> queryPrometheus(String query) {
        // We use the WebClient to build and execute the GET request
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/query") // Prometheus query API endpoint
                        .queryParam("query", "{prometheusQuery}") // <-- Use a placeholder
                        .build(query))                         // <-- Pass the query string as the variable
                .retrieve()
                .bodyToMono(String.class) // Convert the response body to a String
                .doOnError(error -> log.error("Failed to query Prometheus: {}", error.getMessage()));
    }
}