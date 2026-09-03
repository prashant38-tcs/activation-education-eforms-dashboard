package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "frontend_changes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FrontendChange {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    @Column(name = "screen_form_name") private String screenFormName;
    @Column(name = "page_name") private String pageName;
    @Column(name = "html_file_name") private String htmlFileName;
    @Column(name = "js_file_name") private String jsFileName;
    @Column(name = "css_file_name") private String cssFileName;
    @Column(name = "function_name") private String functionName;
    @Column(name = "ui_component") private String uiComponent;
    @Column(name = "validation_changed") private String validationChanged;
    @Column(name = "change_description", columnDefinition = "TEXT") private String changeDescription;
    @Column(name = "review_status", length = 40)
    @Builder.Default
    private String reviewStatus = "PENDING";
    @Column(name = "deployment_status", length = 40)
    @Builder.Default
    private String deploymentStatus = "PENDING";
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
