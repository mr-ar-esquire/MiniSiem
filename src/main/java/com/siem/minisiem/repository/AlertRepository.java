package com.siem.minisiem.repository;

import com.siem.minisiem.model.Alert;   // ✅ THIS LINE IS CRITICAL
import org.springframework.data.jpa.repository.JpaRepository;

import com.siem.minisiem.model.Severity;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findBySeverity(Severity severity);
    boolean existsByDescriptionAndTimestampAfter(String description, java.time.LocalDateTime timestamp);
}