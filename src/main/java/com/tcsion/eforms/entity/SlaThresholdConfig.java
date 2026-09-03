package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "sla_threshold_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SlaThresholdConfig {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "config_key", nullable = false, unique = true, length = 80)
    private String configKey;
    @Column(name = "config_value", nullable = false, length = 255)
    private String configValue;
    @Column(name = "description", length = 255)
    private String description;
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
