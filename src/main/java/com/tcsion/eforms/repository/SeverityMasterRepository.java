package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.SeverityMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SeverityMasterRepository extends JpaRepository<SeverityMaster, Long> {
    List<SeverityMaster> findByActiveTrueOrderByRankOrderAsc();
    Optional<SeverityMaster> findBySeverityCode(String severityCode);
}
