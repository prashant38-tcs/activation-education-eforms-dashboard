package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "team_master")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeamMaster {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "team_code", nullable = false, unique = true, length = 60)
    private String teamCode;
    @Column(name = "team_name", nullable = false, length = 120)
    private String teamName;
    @Column(name = "team_type", nullable = false, length = 30)
    private String teamType;
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
