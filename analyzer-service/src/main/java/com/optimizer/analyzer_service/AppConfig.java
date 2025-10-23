package com.optimizer.analyzer_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Value("${prometheus.server.url}")
    private String prometheusUrl;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        // Configure the WebClient with the base URL for Prometheus
        return builder.baseUrl(prometheusUrl).build();
    }
}