package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "deployment_environment_master")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DeploymentEnvironmentMaster {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "env_code", nullable = false, unique = true, length = 40)
    private String envCode;
    @Column(name = "env_name", nullable = false, length = 80)
    private String envName;
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
