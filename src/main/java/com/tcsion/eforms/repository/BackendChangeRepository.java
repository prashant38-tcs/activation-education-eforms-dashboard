package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.BackendChange;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BackendChangeRepository extends JpaRepository<BackendChange, Long> {
    List<BackendChange> findByTicket_IdOrderByCreatedAtDesc(Long ticketId);
    List<BackendChange> findByDeploymentStatus(String deploymentStatus);
}
