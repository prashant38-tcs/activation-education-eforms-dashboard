package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.ReportSettingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReportSettingConfigRepository extends JpaRepository<ReportSettingConfig, Long> {
    Optional<ReportSettingConfig> findBySettingKey(String key);
}
