package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "risk_weight_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RiskWeightConfig {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "factor_code", nullable = false, unique = true, length = 80)
    private String factorCode;
    @Column(name = "factor_label", nullable = false, length = 150)
    private String factorLabel;
    @Column(name = "weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;
    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
