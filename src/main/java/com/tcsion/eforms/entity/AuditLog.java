package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "user_name", length = 120)
    private String userName;
    @Column(name = "role_snapshot", length = 120)
    private String roleSnapshot;
    @Column(name = "action", nullable = false, length = 100)
    private String action;
    @Column(name = "entity_type", length = 80)
    private String entityType;
    @Column(name = "entity_id")
    private Long entityId;
    @Column(name = "ticket_number", length = 60)
    private String ticketNumber;
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;
    @Column(name = "ip_address", length = 64)
    private String ipAddress;
    @Column(name = "user_agent", length = 255)
    private String userAgent;
    @Column(name = "source", length = 30)
    @Builder.Default
    private String source = "WEB";
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
