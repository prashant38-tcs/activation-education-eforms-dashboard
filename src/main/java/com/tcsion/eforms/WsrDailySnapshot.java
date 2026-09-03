package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "wsr_daily_snapshot")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WsrDailySnapshot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "developer_id", nullable = false)
    private User developer;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "activity_id", nullable = false)
    private TicketActivity activity;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "customer_id")
    private CustomerMaster customer;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "activity_type_id")
    private ActivityTypeMaster activityType;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "previous_status_id")
    private StatusMaster previousStatus;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "current_status_id")
    private StatusMaster currentStatus;
    @Column(name = "progress_percentage")
    private Integer progressPercentage;
    @Column(name = "hours_spent", precision = 5, scale = 2)
    private BigDecimal hoursSpent;
    @Column(name = "remark", length = 500)
    private String remark;
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
