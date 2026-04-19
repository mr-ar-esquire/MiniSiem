package com.siem.minisiem.service;

import com.siem.minisiem.model.LogEntry;
import com.siem.minisiem.repository.LogRepository;
import com.siem.minisiem.service.GeoIpService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@Service
public class LogIngestionService {

    private final LogRepository logRepository;
    private final DetectionService detectionService;
    private final ElasticsearchService elasticsearchService;
    private final GeoIpService geoIpService;

    public LogIngestionService(LogRepository logRepository, DetectionService detectionService, ElasticsearchService elasticsearchService, GeoIpService geoIpService) {
        this.logRepository = logRepository;
        this.detectionService = detectionService;
        this.elasticsearchService = elasticsearchService;
        this.geoIpService = geoIpService;
    }

    public void processBatch(List<LogEntry> logs) {
        for (LogEntry log : logs) {
            log.setLocation(geoIpService.getLocation(log.getSource()));
            logRepository.save(log);
            detectionService.analyze(log);
            elasticsearchService.indexLog(log);
        }
    }

    public void processFile(MultipartFile file) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    LogEntry log = new LogEntry();
                    log.setMessage(line);
                    log.setSource("file-upload");
                    log.setLocation(geoIpService.getLocation("file-upload")); // will yield 'Unknown'
                    logRepository.save(log);
                    detectionService.analyze(log);
                    elasticsearchService.indexLog(log);
                }
            }
        }
    }
}
