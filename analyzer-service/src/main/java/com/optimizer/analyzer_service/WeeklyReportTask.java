package com.optimizer.analyzer_service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WeeklyReportTask {

    private static final Logger log = LoggerFactory.getLogger(WeeklyReportTask.class);

    private final PerformanceAnalyzerTask analyzerTask; // To get the stored insights
    private final EmailService emailService;            // To send the email

    // Constructor injection
    public WeeklyReportTask(PerformanceAnalyzerTask analyzerTask, EmailService emailService) {
        this.analyzerTask = analyzerTask;
        this.emailService = emailService;
    }

    
    @Scheduled(cron = "0 */2 * * * *")
    // --- FOR PRODUCTION: Run every Monday at 9 AM ---
    // @Scheduled(cron = "0 0 9 * * MON")
    public void generateAndSendWeeklyReport() {
        log.info("--- Generating Scheduled Performance Report ({}) ---", LocalDateTime.now());

        // 1. Get all insights collected since the last report
        List<String> insights = analyzerTask.getAndClearInsights();

        // 2. Format the insights into an email body
        String reportBody = buildReportBody(insights);

        // 3. Send the email
        emailService.sendPerformanceReport(reportBody);
    }

    /**
     * Builds the text content for the email body.
     */
    private String buildReportBody(List<String> insights) {
        if (insights.isEmpty()) {
            return "No significant performance issues detected in the past period.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("API Performance Insights Report:\n");
        sb.append("===================================\n\n");

        // Add each insight as a numbered item
        for (int i = 0; i < insights.size(); i++) {
            sb.append(i + 1).append(". ").append(insights.get(i)).append("\n\n");
        }

        sb.append("===================================\n");
        // Add a timestamp
        String generatedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        sb.append("Report generated on: ").append(generatedTime);

        return sb.toString();
    }
}