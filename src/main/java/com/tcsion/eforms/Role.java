package com.tcsion.eforms.entity;

import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Role {

    public static final String TEAM_LEAD = "TEAM_LEAD";
    public static final String TECHNICAL_LEAD = "TECHNICAL_LEAD";
    public static final String DASHBOARD_HANDLER = "DASHBOARD_HANDLER";
    public static final String DEVELOPER = "DEVELOPER";
    public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_code", nullable = false, unique = true, length = 40)
    private String roleCode;

    @Column(name = "role_name", nullable = false, length = 80)
    private String roleName;

    @Column(name = "description")
    private String description;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
