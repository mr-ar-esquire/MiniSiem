package com.siem.minisiem.repository;

import com.siem.minisiem.model.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<LogEntry, Long> {
    int countBySourceAndMessageContainingIgnoreCaseAndTimestampAfter(String source, String message, java.time.LocalDateTime timestamp);
}