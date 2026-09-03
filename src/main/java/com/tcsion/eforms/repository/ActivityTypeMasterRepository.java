package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.ActivityTypeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ActivityTypeMasterRepository extends JpaRepository<ActivityTypeMaster, Long> {
    List<ActivityTypeMaster> findByActiveTrue();
    Optional<ActivityTypeMaster> findByTypeCode(String typeCode);
}
