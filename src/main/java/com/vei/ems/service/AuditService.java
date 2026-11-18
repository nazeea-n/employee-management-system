package com.vei.ems.service;

import com.vei.ems.domain.AuditLog;
import com.vei.ems.dto.AuditEntryResponse;
import com.vei.ems.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void log(String username, String action, String details) {
        String safeDetails = details != null && details.length() > 250 ? details.substring(0, 250) : details;
        repository.save(new AuditLog(username, action, safeDetails));
    }

    @Transactional(readOnly = true)
    public List<AuditEntryResponse> latest() {
        return repository.findTop100ByOrderByIdDesc().stream()
                .map(a -> new AuditEntryResponse(a.getLoggedAt(), a.getUsername(), a.getAction(), a.getDetails()))
                .collect(Collectors.toList());
    }
}
