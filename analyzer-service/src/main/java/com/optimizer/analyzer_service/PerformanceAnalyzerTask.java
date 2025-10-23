package com.optimizer.analyzer_service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class PerformanceAnalyzerTask {

    private static final Logger log = LoggerFactory.getLogger(PerformanceAnalyzerTask.class);
    private final WebClient webClient;
    private final AIService aiService; // <-- ADD THIS

    // Spring injects both the WebClient and our new AIService
    public PerformanceAnalyzerTask(WebClient webClient, AIService aiService) { // <-- MODIFY THIS
        this.webClient = webClient;
        this.aiService = aiService; // <-- ADD THIS
    }

    // A PromQL query to find endpoints with p95 latency > 500ms
    private final String P95_LATENCY_QUERY = 
        "histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{application=\"performance-demo\"}[5m])) by (le, uri, method)) > 0.5";
    
    // A query to find endpoints with an error rate over 1% in the last 5 minutes
    private final String ERROR_RATE_QUERY = 
        "(sum(rate(http_server_requests_seconds_count{application=\"performance-demo\",outcome=\"SERVER_ERROR\"}[5m])) by (uri, method) " +
        "/ " +
        "sum(rate(http_server_requests_seconds_count{application=\"performance-demo\"}[5m])) by (uri, method)) " +
        "> 0.01"; // > 1% error rate


    @Scheduled(fixedRate = 30000) // Run every 30 seconds for testing
    public void analyzePerformance() {
        log.info("--- Starting Performance Analysis ---");

        // 1. Query for slow endpoints and send to AI
        queryPrometheus(P95_LATENCY_QUERY)
            .subscribe(jsonResponse -> {
                log.warn("SLOW ENDPOINT DATA: {}", jsonResponse);
                String insight = aiService.getInsightForData(jsonResponse); // <-- SEND TO AI
                if (insight != null) {
                    log.info("\n\n==> 🤖 AI OPTIMIZATION (Latency): {} \n<==\n", insight); // <-- PRINT AI RESPONSE
                }
            });

        // 2. Query for erroring endpoints and send to AI
        queryPrometheus(ERROR_RATE_QUERY)
            .subscribe(jsonResponse -> {
                log.error("ERRORING ENDPOINT DATA: {}", jsonResponse);
                String insight = aiService.getInsightForData(jsonResponse); // <-- SEND TO AI
                if (insight != null) {
                    log.info("\n\n==> 🤖 AI OPTIMIZATION (Errors): {} \n<==\n", insight); // <-- PRINT AI RESPONSE
                }
            });
    }

    private reactor.core.publisher.Mono<String> queryPrometheus(String query) {
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