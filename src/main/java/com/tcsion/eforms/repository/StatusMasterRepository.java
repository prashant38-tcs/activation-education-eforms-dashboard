package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.StatusMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StatusMasterRepository extends JpaRepository<StatusMaster, Long> {
    List<StatusMaster> findByActiveTrueOrderBySortOrderAsc();
    Optional<StatusMaster> findByStatusCode(String statusCode);
}
