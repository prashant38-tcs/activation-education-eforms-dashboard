package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "backend_changes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BackendChange {
    public static final String PENDING = "PENDING";
    public static final String DEVELOPMENT_IN_PROGRESS = "DEVELOPMENT_IN_PROGRESS";
    public static final String CODE_REVIEW_PENDING = "CODE_REVIEW_PENDING";
    public static final String CODE_REVIEW_COMPLETED = "CODE_REVIEW_COMPLETED";
    public static final String BUILD_PENDING = "BUILD_PENDING";
    public static final String BUILD_COMPLETED = "BUILD_COMPLETED";
    public static final String READY_FOR_UAT = "READY_FOR_UAT";
    public static final String READY_FOR_QA = "READY_FOR_QA";
    public static final String READY_FOR_PRODUCTION = "READY_FOR_PRODUCTION";
    public static final String DEPLOYED = "DEPLOYED";
    public static final String ROLLBACK_INITIATED = "ROLLBACK_INITIATED";
    public static final String ROLLED_BACK = "ROLLED_BACK";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    @Column(name = "application_module") private String applicationModule;
    @Column(name = "jar_name", nullable = false) private String jarName;
    @Column(name = "jar_version") private String jarVersion;
    @Column(name = "build_number") private String buildNumber;
    @Column(name = "pipeline_name", nullable = false) private String pipelineName;
    @Column(name = "pipeline_reference") private String pipelineReference;
    @Column(name = "package_name") private String packageName;
    @Column(name = "class_name", nullable = false) private String className;
    @Column(name = "method_name", nullable = false) private String methodName;
    @Column(name = "api_service_name") private String apiServiceName;
    @Column(name = "change_description", columnDefinition = "TEXT") private String changeDescription;
    @Column(name = "code_review_status", length = 40)
    @Builder.Default
    private String codeReviewStatus = "PENDING";
    @Column(name = "build_status", length = 40)
    @Builder.Default
    private String buildStatus = "PENDING";
    @Column(name = "deployment_status", nullable = false, length = 40)
    @Builder.Default
    private String deploymentStatus = PENDING;
    @Column(name = "environment", length = 30) private String environment;
    @Column(name = "deployment_date") private LocalDateTime deploymentDate;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "deployment_owner_id")
    private User deploymentOwner;
    @Column(name = "rollback_required")
    @Builder.Default
    private boolean rollbackRequired = false;
    @Column(name = "rollback_status", length = 40) private String rollbackStatus;
    @Column(name = "rollback_remark", length = 500) private String rollbackRemark;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
