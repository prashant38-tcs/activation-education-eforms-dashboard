package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "priority_master")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PriorityMaster {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "priority_code", nullable = false, unique = true, length = 30)
    private String priorityCode;
    @Column(name = "priority_name", nullable = false, length = 60)
    private String priorityName;
    @Column(name = "rank_order", nullable = false)
    @Builder.Default
    private int rankOrder = 0;
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
