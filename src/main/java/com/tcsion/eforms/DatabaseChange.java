package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "database_changes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DatabaseChange {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    @Column(name = "database_name") private String databaseName;
    @Column(name = "schema_name") private String schemaName;
    @Column(name = "table_name") private String tableName;
    @Column(name = "column_name") private String columnName;
    @Column(name = "procedure_name") private String procedureName;
    @Column(name = "function_name") private String functionName;
    @Column(name = "trigger_name") private String triggerName;
    @Column(name = "view_name") private String viewName;
    @Column(name = "query_change_description", columnDefinition = "TEXT") private String queryChangeDescription;
    @Column(name = "script_file_name") private String scriptFileName;
    @Column(name = "execution_sequence") private Integer executionSequence;
    @Column(name = "backup_required") @Builder.Default private boolean backupRequired = false;
    @Column(name = "rollback_script_available") @Builder.Default private boolean rollbackScriptAvailable = false;
    @Column(name = "data_migration_required") @Builder.Default private boolean dataMigrationRequired = false;
    @Column(name = "environment", length = 30) private String environment;
    @Column(name = "execution_status", length = 40)
    @Builder.Default
    private String executionStatus = "PENDING";
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "executed_by_id")
    private User executedBy;
    @Column(name = "execution_date") private LocalDateTime executionDate;
    @Column(name = "validation_result") private String validationResult;
    @Column(name = "dba_remark", length = 500) private String dbaRemark;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
