package com.tcsion.eforms.repository;
import com.tcsion.eforms.entity.FrontendChange;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FrontendChangeRepository extends JpaRepository<FrontendChange, Long> {
    List<FrontendChange> findByTicket_IdOrderByCreatedAtDesc(Long ticketId);
}
