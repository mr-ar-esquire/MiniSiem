package com.siem.minisiem.controller;

import com.siem.minisiem.model.LogEntry;
import com.siem.minisiem.model.EsLogEntry;
import com.siem.minisiem.repository.LogRepository;
import com.siem.minisiem.service.DetectionService;
import com.siem.minisiem.service.LogIngestionService;
import com.siem.minisiem.service.ElasticsearchService;
import com.siem.minisiem.service.GeoIpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogController {

    private static final Logger logger = LoggerFactory.getLogger(LogController.class);
    
    private final LogRepository logRepository;
    private final DetectionService detectionService;
    private final LogIngestionService logIngestionService;
    private final ElasticsearchService elasticsearchService;
    private final GeoIpService geoIpService;

    public LogController(LogRepository logRepository,
                         DetectionService detectionService,
                         LogIngestionService logIngestionService,
                         ElasticsearchService elasticsearchService,
                         GeoIpService geoIpService) {
        this.logRepository = logRepository;
        this.detectionService = detectionService;
        this.logIngestionService = logIngestionService;
        this.elasticsearchService = elasticsearchService;
        this.geoIpService = geoIpService;
    }

    // ✅ 1. Add single log
    @PostMapping
    public String addLog(@RequestBody LogEntry log) {
        log.setLocation(geoIpService.getLocation(log.getSource()));
        logRepository.save(log);
        logger.info("Ingesting Log: {}", log.getMessage());
        detectionService.analyze(log);
        elasticsearchService.indexLog(log);
        return "Log processed";
    }

    // ✅ 2. Add batch logs
    @PostMapping("/batch")
    public String addLogBatch(@RequestBody List<LogEntry> logs) {
        logger.info("Ingesting batch of {} logs...", logs.size());
        logIngestionService.processBatch(logs);
        return logs.size() + " logs processed";
    }

    // ✅ 3. Upload log file
    @PostMapping("/upload")
    public String uploadLogFile(@RequestParam("file") MultipartFile file) {
        try {
            logIngestionService.processFile(file);
            return "File processed successfully";
        } catch (Exception e) {
            return "Error processing file: " + e.getMessage();
        }
    }

    // ✅ 4. Fetch all logs (for dashboard)
    @GetMapping
    public List<LogEntry> getLogs() {
        return logRepository.findAll();
    }

    // ✅ 5. Search logs in Elasticsearch
    @GetMapping("/search")
    public List<EsLogEntry> searchLogsEs(@RequestParam("keyword") String keyword) {
        return elasticsearchService.searchLogs(keyword);
    }
}