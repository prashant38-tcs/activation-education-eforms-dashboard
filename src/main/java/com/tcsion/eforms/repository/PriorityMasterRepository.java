package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.PriorityMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PriorityMasterRepository extends JpaRepository<PriorityMaster, Long> {
    List<PriorityMaster> findByActiveTrueOrderByRankOrderAsc();
    Optional<PriorityMaster> findByPriorityCode(String priorityCode);
}
