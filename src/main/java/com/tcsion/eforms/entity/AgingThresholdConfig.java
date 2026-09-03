package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "aging_threshold_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AgingThresholdConfig {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "bucket_code", nullable = false, unique = true, length = 30)
    private String bucketCode;
    @Column(name = "min_days", nullable = false)
    private int minDays;
    @Column(name = "max_days")
    private Integer maxDays;
    @Column(name = "color_code", nullable = false, length = 20)
    private String colorCode;
    @Column(name = "label", nullable = false, length = 60)
    private String label;
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
