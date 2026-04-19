package com.siem.minisiem.controller;

import com.siem.minisiem.model.Alert;
import com.siem.minisiem.model.Severity;
import com.siem.minisiem.repository.AlertRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertRepository alertRepository;

    public AlertController(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @GetMapping
    public List<Alert> getAlerts(@RequestParam(required = false) Severity severity) {
        if (severity != null) {
            return alertRepository.findBySeverity(severity);
        }
        return alertRepository.findAll();
    }
}
