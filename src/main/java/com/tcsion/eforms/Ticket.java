package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "tickets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ticket extends BaseAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ticket_number", nullable = false, unique = true, length = 60)
    private String ticketNumber;
    @Column(name = "crm_id", length = 60)
    private String crmId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "customer_id")
    private CustomerMaster customer;
    @Column(name = "ticket_title", nullable = false)
    private String ticketTitle;
    @Column(name = "ticket_description", columnDefinition = "TEXT")
    private String ticketDescription;
    @Column(name = "short_planned_milestone")
    private String shortPlannedMilestone;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ticket_type_id")
    private TicketTypeMaster ticketType;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "priority_id")
    private PriorityMaster priority;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "severity_id")
    private SeverityMaster severity;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_user_id")
    private User assignedUser;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "previous_assignee_id")
    private User previousAssignee;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_by_id")
    private User assignedBy;
    @Column(name = "assignment_date")
    private LocalDateTime assignmentDate;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "source_team_id")
    private TeamMaster sourceTeam;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "dependency_team_id")
    private TeamMaster dependencyTeam;
    @Column(name = "created_date", nullable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();
    @Column(name = "expected_closure_date")
    private LocalDate expectedClosureDate;
    @Column(name = "estimated_production_date")
    private LocalDate estimatedProductionDate;
    @Column(name = "actual_production_date")
    private LocalDate actualProductionDate;
    @Column(name = "last_activity_date")
    private LocalDateTime lastActivityDate;
    @Column(name = "last_updated_date")
    private LocalDateTime lastUpdatedDate;
    @Column(name = "closed_date")
    private LocalDateTime closedDate;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "current_status_id", nullable = false)
    private StatusMaster currentStatus;
    @Column(name = "current_stage", length = 60)
    private String currentStage;
    @Column(name = "sla_state", length = 30)
    @Builder.Default
    private String slaState = "MET";
    @Column(name = "sla_risk_score", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal slaRiskScore = BigDecimal.ZERO;
    @Column(name = "sla_risk_category", length = 20)
    @Builder.Default
    private String slaRiskCategory = "LOW";
    @Column(name = "aging_days")
    @Builder.Default
    private int agingDays = 0;
    @Column(name = "on_hold", nullable = false)
    @Builder.Default
    private boolean onHold = false;
    @Column(name = "hold_reason")
    private String holdReason;
    @Column(name = "reassignment_count", nullable = false)
    @Builder.Default
    private int reassignmentCount = 0;
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
    @Column(name = "is_archived", nullable = false)
    @Builder.Default
    private boolean archived = false;
    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Transient
    public boolean isOpen() {
        return currentStatus != null && currentStatus.isOpen();
    }
}
