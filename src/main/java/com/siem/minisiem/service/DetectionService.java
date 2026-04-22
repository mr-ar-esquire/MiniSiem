// brute force detection based on failed login attempts
package com.siem.minisiem.service;
import com.siem.minisiem.model.Alert;
import com.siem.minisiem.model.LogEntry;
import com.siem.minisiem.repository.AlertRepository;
import org.springframework.stereotype.Service;

import com.siem.minisiem.model.Severity;
import com.siem.minisiem.repository.LogRepository;
import java.time.LocalDateTime;

@Service
public class DetectionService {

    private final AlertRepository alertRepository;
    private final LogRepository logRepository;

    public DetectionService(AlertRepository alertRepository, LogRepository logRepository) {
        this.alertRepository = alertRepository;
        this.logRepository = logRepository;
    }

    public void analyze(LogEntry log) {
        String message = log.getMessage().toLowerCase();
        String source = log.getSource();
        
        String locContent = log.getLocation() != null && !log.getLocation().equals("Unknown") 
            ? " (" + log.getLocation() + ")" : "";

        // 1. Basic sql injection detection using common patterns (OR, UNION, comments)
        if (message.contains("' or 1=1") || message.contains("--") || message.contains("union select")) {
            createAlertIfNeeded("SQL Injection attempt detected from " + source + locContent, Severity.HIGH);
        }

        // 2. Suspicious Endpoint Access
        if (message.contains("/admin") || message.contains("/etc/passwd") || message.contains("/config")) {
            createAlertIfNeeded("Suspicious endpoint access detected from " + source + locContent, Severity.MEDIUM);
        }

        // 3. Time-based Correlation: Brute Force (>= 3 failed attempts in last 5 mins)
        if (message.contains("failed password")) {
            LocalDateTime fiveMinsAgo = LocalDateTime.now().minusMinutes(5);
            int failedAttempts = logRepository.countBySourceAndMessageContainingIgnoreCaseAndTimestampAfter(
                    source, "failed password", fiveMinsAgo);

            if (failedAttempts >= 3) {
                createAlertIfNeeded("Brute force attack from " + source + locContent, Severity.HIGH);
            } else {
                createAlertIfNeeded("Failed login attempt from " + source + locContent, Severity.LOW);
            }
        }
    }

    private void createAlertIfNeeded(String description, Severity severity) {
        // Deduplication Logic: HIGH alerts only trigger once per source within 5 minutes.
        if (severity == Severity.HIGH) {
            LocalDateTime fiveMinsAgo = LocalDateTime.now().minusMinutes(5);
            if (alertRepository.existsByDescriptionAndTimestampAfter(description, fiveMinsAgo)) {
                return; // Duplicate blocked
            }
        }

        Alert alert = new Alert();
        alert.setDescription(description);
        alert.setSeverity(severity);
        alertRepository.save(alert);
        
        System.out.println("[ALERT GENERATED] " + severity + " - " + description);
    }
}
// TODO: improve detection logic with more advanced rules