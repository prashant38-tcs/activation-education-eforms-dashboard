package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.DeploymentEnvironmentMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DeploymentEnvironmentMasterRepository extends JpaRepository<DeploymentEnvironmentMaster, Long> {
    List<DeploymentEnvironmentMaster> findByActiveTrueOrderBySortOrderAsc();
}
