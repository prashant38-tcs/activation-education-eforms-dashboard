package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_comments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TicketComment {
    public static final String GENERAL = "GENERAL";
    public static final String INTERNAL_NOTE = "INTERNAL_NOTE";
    public static final String BLOCKER = "BLOCKER";
    public static final String ROOT_CAUSE = "ROOT_CAUSE";
    public static final String DEPENDENCY = "DEPENDENCY";
    public static final String ACTION_TAKEN = "ACTION_TAKEN";
    public static final String NEXT_ACTION = "NEXT_ACTION";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    @Column(name = "comment_type", nullable = false, length = 40)
    @Builder.Default
    private String commentType = GENERAL;
    @Column(name = "comment_text", nullable = false, columnDefinition = "TEXT")
    private String commentText;
    @Column(name = "follow_up_date")
    private LocalDate followUpDate;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
