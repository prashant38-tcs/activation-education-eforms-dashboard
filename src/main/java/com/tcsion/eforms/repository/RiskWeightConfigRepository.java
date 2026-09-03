package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.RiskWeightConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RiskWeightConfigRepository extends JpaRepository<RiskWeightConfig, Long> {
    List<RiskWeightConfig> findByActiveTrue();
}
