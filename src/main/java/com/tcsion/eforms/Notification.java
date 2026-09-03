package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
    public static final String NEW_TICKET_ASSIGNED = "NEW_TICKET_ASSIGNED";
    public static final String TICKET_REASSIGNED = "TICKET_REASSIGNED";
    public static final String STATUS_CHANGED = "STATUS_CHANGED";
    public static final String REMARK_ADDED = "REMARK_ADDED";
    public static final String ON_HOLD = "ON_HOLD";
    public static final String APPROACHING_PRODUCTION_DATE = "APPROACHING_PRODUCTION_DATE";
    public static final String OVERDUE = "OVERDUE";
    public static final String HIGH_SLA_RISK = "HIGH_SLA_RISK";
    public static final String SLA_BREACH = "SLA_BREACH";
    public static final String NO_ACTIVITY = "NO_ACTIVITY";
    public static final String MOVED_TO_PRODUCTION = "MOVED_TO_PRODUCTION";
    public static final String IMPORT_COMPLETED = "IMPORT_COMPLETED";
    public static final String IMPORT_FAILED = "IMPORT_FAILED";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;
    @Column(name = "notification_type", nullable = false, length = 60)
    private String notificationType;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "message", nullable = false, length = 500)
    private String message;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "related_ticket_id")
    private Ticket relatedTicket;
    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;
    @Column(name = "read_at")
    private LocalDateTime readAt;
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
