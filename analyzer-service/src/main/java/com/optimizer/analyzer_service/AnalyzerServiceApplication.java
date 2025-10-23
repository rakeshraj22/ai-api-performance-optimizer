package com.optimizer.analyzer_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling 
public class AnalyzerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyzerServiceApplication.class, args);
    }
}