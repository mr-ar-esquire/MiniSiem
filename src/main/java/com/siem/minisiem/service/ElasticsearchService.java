// index logs into elasticsearch for search and analysis
package com.siem.minisiem.service;

import com.siem.minisiem.model.EsLogEntry;
import com.siem.minisiem.model.LogEntry;
import com.siem.minisiem.repository.EsLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ElasticsearchService {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchService.class);
    private final EsLogRepository esLogRepository;

    public ElasticsearchService(EsLogRepository esLogRepository) {
        this.esLogRepository = esLogRepository;
    }

    public void indexLog(LogEntry log) {
        try {
            EsLogEntry esLog = new EsLogEntry();
            esLog.setMessage(log.getMessage());
            esLog.setSource(log.getSource());
            esLog.setLocation(log.getLocation());
            esLog.setTimestamp(log.getTimestamp());
            esLogRepository.save(esLog);
        } catch (Exception e) {
            logger.warn("Elasticsearch indexing omitted (Check ES connectivity): {}", e.getMessage());
        }
    }

    public List<EsLogEntry> searchLogs(String keyword) {
        try {
            return esLogRepository.findByMessageContainingIgnoreCase(keyword);
        } catch (Exception e) {
            logger.warn("Elasticsearch search failed (Returning blank): {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
