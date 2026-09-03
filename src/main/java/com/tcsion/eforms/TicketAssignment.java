package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TicketAssignment {
    public static final String INITIAL = "INITIAL";
    public static final String REASSIGNMENT = "REASSIGNMENT";
    public static final String IMPORT = "IMPORT";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_from_user_id")
    private User assignedFromUser;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_to_user_id")
    private User assignedToUser;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_to_team_id")
    private TeamMaster assignedToTeam;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "assigned_by_id", nullable = false)
    private User assignedBy;
    @Column(name = "assignment_type", nullable = false, length = 30)
    private String assignmentType;
    @Column(name = "reason")
    private String reason;
    @Column(name = "assigned_at", nullable = false)
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();
}
