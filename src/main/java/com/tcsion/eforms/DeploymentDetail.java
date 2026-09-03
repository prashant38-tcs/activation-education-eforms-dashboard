package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "deployment_details")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeploymentDetail {
    public static final String PENDING = "PENDING";
    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED = "FAILED";
    public static final String ROLLED_BACK = "ROLLED_BACK";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    @Column(name = "environment", nullable = false, length = 30) private String environment;
    @Column(name = "jar_name") private String jarName;
    @Column(name = "jar_version") private String jarVersion;
    @Column(name = "build_number") private String buildNumber;
    @Column(name = "pipeline_name") private String pipelineName;
    @Column(name = "pipeline_reference") private String pipelineReference;
    @Column(name = "deployment_date") private LocalDateTime deploymentDate;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "deployment_owner_id")
    private User deploymentOwner;
    @Column(name = "deployment_status", nullable = false, length = 40)
    @Builder.Default
    private String deploymentStatus = PENDING;
    @Column(name = "validation_status", length = 40)
    @Builder.Default
    private String validationStatus = "PENDING";
    @Column(name = "rollback_required") @Builder.Default private boolean rollbackRequired = false;
    @Column(name = "rollback_status", length = 40) private String rollbackStatus;
    @Column(name = "deployment_remark", length = 500) private String deploymentRemark;
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
