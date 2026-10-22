package com.finshield.audit.service;

import com.finshield.audit.model.AuditLog;
import com.finshield.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String action, String principal, String ip, String details) {
        auditLogRepository.save(new AuditLog(action, principal, ip, details));
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc();
    }
}