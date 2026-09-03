package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sla_risk_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SlaRiskHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    @Column(name = "risk_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal riskScore;
    @Column(name = "risk_category", nullable = false, length = 20)
    private String riskCategory;
    @Column(name = "calculation_date", nullable = false)
    @Builder.Default
    private LocalDateTime calculationDate = LocalDateTime.now();
    @Column(name = "triggered_factors", length = 1000)
    private String triggeredFactors;
    @Column(name = "recommended_action", length = 500)
    private String recommendedAction;
    @Column(name = "recovery_date")
    private LocalDate recoveryDate;
    @Column(name = "risk_override")
    @Builder.Default
    private boolean riskOverride = false;
    @Column(name = "override_reason", length = 500)
    private String overrideReason;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "overridden_by_id")
    private User overriddenBy;
}
