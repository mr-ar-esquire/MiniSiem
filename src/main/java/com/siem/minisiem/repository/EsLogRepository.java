package com.siem.minisiem.repository;

import com.siem.minisiem.model.EsLogEntry;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface EsLogRepository extends ElasticsearchRepository<EsLogEntry, String> {
    List<EsLogEntry> findByMessageContainingIgnoreCase(String keyword);
}
