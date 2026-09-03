package com.tcsion.eforms.entity;
import lombok.*;
import javax.persistence.*;

@Entity
@Table(name = "report_setting_config")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportSettingConfig {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "setting_key", nullable = false, unique = true, length = 100)
    private String settingKey;
    @Column(name = "setting_value", nullable = false, length = 255)
    private String settingValue;
    @Column(name = "description", length = 255)
    private String description;
}
