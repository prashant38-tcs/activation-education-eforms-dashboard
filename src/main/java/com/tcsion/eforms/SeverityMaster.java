package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "severity_master")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeverityMaster {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "severity_code", nullable = false, unique = true, length = 30)
    private String severityCode;
    @Column(name = "severity_name", nullable = false, length = 60)
    private String severityName;
    @Column(name = "rank_order", nullable = false)
    @Builder.Default
    private int rankOrder = 0;
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
