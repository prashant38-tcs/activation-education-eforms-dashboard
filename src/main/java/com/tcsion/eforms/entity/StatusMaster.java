package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "status_master")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StatusMaster {
    public static final String NEW = "NEW";
    public static final String ASSIGNED = "ASSIGNED";
    public static final String WORK_IN_PROGRESS = "WORK_IN_PROGRESS";
    public static final String UAT_IN_PROGRESS = "UAT_IN_PROGRESS";
    public static final String QA_IN_PROGRESS = "QA_IN_PROGRESS";
    public static final String REASSIGNED_TO_FRAMEWORK_TEAM = "REASSIGNED_TO_FRAMEWORK_TEAM";
    public static final String REASSIGNED_TO_OTHER_TEAM = "REASSIGNED_TO_OTHER_TEAM";
    public static final String ON_HOLD = "ON_HOLD";
    public static final String REOPENED = "REOPENED";
    public static final String MOVED_TO_PRODUCTION = "MOVED_TO_PRODUCTION";
    public static final String CLOSED = "CLOSED";
    public static final String CANCELLED = "CANCELLED";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "status_code", nullable = false, unique = true, length = 60)
    private String statusCode;
    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;
    @Column(name = "is_terminal", nullable = false)
    @Builder.Default
    private boolean terminal = false;
    @Column(name = "is_open", nullable = false)
    @Builder.Default
    private boolean open = true;
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
