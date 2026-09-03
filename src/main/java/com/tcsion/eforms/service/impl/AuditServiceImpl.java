package com.tcsion.eforms.service.impl;

import com.tcsion.eforms.entity.AuditLog;
import com.tcsion.eforms.repository.AuditLogRepository;
import com.tcsion.eforms.repository.UserRepository;
import com.tcsion.eforms.security.SecurityUtils;
import com.tcsion.eforms.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    public void log(String action, String entityType, Long entityId, String ticketNumberOrRef,
                     String oldValue, String newValue) {
        log(action, entityType, entityId, ticketNumberOrRef, oldValue, newValue, "WEB");
    }

    @Override
    public void log(String action, String entityType, Long entityId, String ticketNumberOrRef,
                     String oldValue, String newValue, String source) {
        logAs(SecurityUtils.currentUsername(), action, entityType, entityId, ticketNumberOrRef,
                oldValue, newValue, source);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAs(String username, String action, String entityType, Long entityId,
                       String ticketNumberOrRef, String oldValue, String newValue, String source) {
        Long userId = userRepository.findByUsernameIgnoreCase(username).map(u -> u.getId()).orElse(null);
        String roleSnapshot = SecurityUtils.currentUser()
                .map(u -> String.join(",", u.getRoleCodes()))
                .orElse(null);

        AuditLog logEntry = AuditLog.builder()
                .userId(userId)
                .userName(username)
                .roleSnapshot(roleSnapshot)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .ticketNumber(ticketNumberOrRef)
                .oldValue(oldValue)
                .newValue(newValue)
                .ipAddress(SecurityUtils.currentIpAddress())
                .userAgent(SecurityUtils.currentUserAgent())
                .source(source == null ? "WEB" : source)
                .build();
        auditLogRepository.save(logEntry);
    }
}
