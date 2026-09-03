package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.AgingThresholdConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AgingThresholdConfigRepository extends JpaRepository<AgingThresholdConfig, Long> {
    List<AgingThresholdConfig> findByActiveTrueOrderByMinDaysAsc();
}
