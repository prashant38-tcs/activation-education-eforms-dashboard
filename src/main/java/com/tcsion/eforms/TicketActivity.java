package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_activities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TicketActivity {
    public static final String SOURCE_MANUAL = "MANUAL";
    public static final String SOURCE_EXCEL_IMPORT = "EXCEL_IMPORT";
    public static final String SOURCE_SYSTEM = "SYSTEM";
    public static final String SOURCE_API = "API";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    @Column(name = "activity_datetime", nullable = false)
    @Builder.Default
    private LocalDateTime activityDatetime = LocalDateTime.now();
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "previous_status_id")
    private StatusMaster previousStatus;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "new_status_id")
    private StatusMaster newStatus;
    @Column(name = "progress_percentage")
    private Integer progressPercentage;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "activity_type_id")
    private ActivityTypeMaster activityType;
    @Column(name = "work_summary", length = 500)
    private String workSummary;
    @Column(name = "detailed_remark", columnDefinition = "TEXT")
    private String detailedRemark;
    @Column(name = "blocker", length = 500)
    private String blocker;
    @Column(name = "root_cause", length = 500)
    private String rootCause;
    @Column(name = "action_taken", length = 500)
    private String actionTaken;
    @Column(name = "next_action", length = 500)
    private String nextAction;
    @Column(name = "dependency", length = 255)
    private String dependency;
    @Column(name = "estimated_completion_date")
    private LocalDate estimatedCompletionDate;
    @Column(name = "hours_spent", precision = 5, scale = 2)
    private BigDecimal hoursSpent;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "updated_by_id", nullable = false)
    private User updatedBy;
    @Column(name = "source", nullable = false, length = 30)
    @Builder.Default
    private String source = SOURCE_MANUAL;
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
