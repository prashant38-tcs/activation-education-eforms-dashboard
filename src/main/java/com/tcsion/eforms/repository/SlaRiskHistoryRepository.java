package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.SlaRiskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SlaRiskHistoryRepository extends JpaRepository<SlaRiskHistory, Long> {
    List<SlaRiskHistory> findByTicket_IdOrderByCalculationDateDesc(Long ticketId);
    Optional<SlaRiskHistory> findFirstByTicket_IdOrderByCalculationDateDesc(Long ticketId);
}
