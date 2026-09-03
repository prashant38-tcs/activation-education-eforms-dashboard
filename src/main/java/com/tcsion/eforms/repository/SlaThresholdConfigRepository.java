package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.SlaThresholdConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SlaThresholdConfigRepository extends JpaRepository<SlaThresholdConfig, Long> {
    Optional<SlaThresholdConfig> findByConfigKey(String configKey);
    List<SlaThresholdConfig> findByActiveTrue();
}
