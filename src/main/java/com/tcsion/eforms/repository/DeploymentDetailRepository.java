package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.DeploymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface DeploymentDetailRepository extends JpaRepository<DeploymentDetail, Long>, JpaSpecificationExecutor<DeploymentDetail> {
    List<DeploymentDetail> findByTicket_IdOrderByCreatedAtDesc(Long ticketId);
    List<DeploymentDetail> findByDeploymentStatusOrderByDeploymentDateDesc(String status);
    List<DeploymentDetail> findByEnvironmentOrderByDeploymentDateDesc(String environment);
}
