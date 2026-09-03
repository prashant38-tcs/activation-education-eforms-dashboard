package com.tcsion.eforms.service;

public interface AuditService {
    void log(String action, String entityType, Long entityId, String ticketNumberOrRef,
              String oldValue, String newValue);
    void log(String action, String entityType, Long entityId, String ticketNumberOrRef,
              String oldValue, String newValue, String source);
    void logAs(String username, String action, String entityType, Long entityId, String ticketNumberOrRef,
                String oldValue, String newValue, String source);
}
